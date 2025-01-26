package com.mozhimen.scank.face.arc42.basic.helpers

import android.util.Log
import android.util.Size
import com.arcsoft.face.*
import com.arcsoft.face.enums.ExtractType
import com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode
import com.mozhimen.scank.face.arc42.basic.commons.IDetectListener
import com.mozhimen.scank.face.arc42.basic.helpers.ScanKArcFaceUtil.keepMaxFace
import com.mozhimen.scank.face.arc42.basic.mos.FacePreviewInfo
import java.util.*
import java.util.concurrent.*


/**
 * @ClassName ScanKArcFaceHelper
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Version 1.0
 */
class ScanKArcFaceHelper(builder: Builder) {

    companion object {
        private const val TAG = "ScanKArcFaceHelper>>>>>"

    }

    private var _faceTraceEngine: FaceEngine? = null    //人脸追踪引擎
    private var _faceFeatureEngine: FaceEngine? = null    //特征提取引擎
    private var _detectListener: IDetectListener? = null
    private var _previewSize: Size? = null
    private var _faceFeatureThreadQueue: LinkedBlockingQueue<Runnable>
    private var _faceFeatureExecutor: ExecutorService

    private var _detectedFaceCount = 0    //上次应用退出时，记录的该App检测过的人脸数了
    private var _currentMaxFaceId = 0    //本次打开引擎后的最大faceId

    private var _faceInfoList: ArrayList<FaceInfo> = ArrayList()
    private val _personMap = ConcurrentHashMap<Int, String>()    //用于存储人脸对应的姓名，KEY为trackId，VALUE为name
    private val _currentTrackIdList: ArrayList<Int> = ArrayList()
    private val _facePreviewInfoList: ArrayList<FacePreviewInfo> = ArrayList()

    /**
     * 释放
     */
    fun release() {
        if (!_faceFeatureExecutor.isShutdown) {
            _faceFeatureExecutor.shutdownNow()
            _faceFeatureThreadQueue.clear()
        }
        _faceInfoList.clear()
        _personMap.clear()
        _detectListener = null
    }

    init {
        _faceTraceEngine = builder.getFaceTraceEngine()
        _faceFeatureEngine = builder.getFaceFeatureEngine()
        _detectListener = builder.getDetectListener()
        _detectedFaceCount = builder.getDetectFaceCount()
        _previewSize = builder.getPreviewSize()

        var faceFeatureQueueSize = 5        //fr 线程队列大小
        if (builder.getFaceFeatureQueueSize() > 0) {
            faceFeatureQueueSize = builder.getFaceFeatureQueueSize()
        } else {
            Log.e(TAG, "init frThread num must > 0,now using default value:$faceFeatureQueueSize")
        }
        _faceFeatureThreadQueue = LinkedBlockingQueue<Runnable>(faceFeatureQueueSize)
        _faceFeatureExecutor = ThreadPoolExecutor(1, faceFeatureQueueSize, 0, TimeUnit.MILLISECONDS, _faceFeatureThreadQueue)
    }

    /**
     * 新增搜索成功的人脸
     * @param trackId Int
     * @param name String
     */
    fun addPerson(trackId: Int, name: String) {
        _personMap[trackId] = name
    }

    /**
     * 获取当前的最大trackID,可用于退出时保存
     * @return Int
     */
    fun getTrackedFaceCount(): Int {
        return _detectedFaceCount + _currentMaxFaceId + 1        // 引擎的人脸下标从0开始，因此需要+1
    }

    /**
     * 处理帧数据
     * @param imageBytes ByteArray 相机预览回传的NV21数据
     * @return List<FacePreviewInfo> 实时人脸处理结果，封装添加了一个trackId，trackId的获取依赖于faceId，用于记录人脸序号并保存
     */
    fun onPreviewFrame(imageBytes: ByteArray): List<FacePreviewInfo> {
        return if (_detectListener != null) {
            if (_faceTraceEngine != null) {
                _faceInfoList.clear()
                val lastTime = System.currentTimeMillis()
                val code: Int = _faceTraceEngine!!.detectFaces(imageBytes, _previewSize!!.width, _previewSize!!.height, FaceEngine.CP_PAF_NV21, _faceInfoList)
                if (code != ErrorInfo.MOK) {
                    Log.v(TAG, "onPreviewFrame: fail code: $code")
                    _detectListener!!.onFail(Exception("onPreviewFrame: _faceTraceEngine failed, code: $code"))
                } else {
                    Log.v(TAG, "onPreviewFrame: detectFaces size ${_faceInfoList.size} [${_previewSize!!.width}x${_previewSize!!.height}] costTime = ${System.currentTimeMillis() - lastTime}ms")
                }
                //若需要多人脸搜索，删除此行代码
                keepMaxFace(_faceInfoList)
                refreshTrackId(_faceInfoList)
            }
            _facePreviewInfoList.clear()
            for (info in _faceInfoList.indices) {
                _facePreviewInfoList.add(FacePreviewInfo(_faceInfoList[info], _currentTrackIdList[info]))
            }
            _facePreviewInfoList
        } else {
            Log.v(TAG, "onPreviewFrame: _detectListener == null")
            _facePreviewInfoList.clear()
            _facePreviewInfoList
        }
    }

    /**
     * 请求获取人脸特征数据
     * @param nv21 ByteArray
     * @param faceInfo FaceInfo
     * @param width Int
     * @param height Int
     * @param format Int
     * @param trackId Int
     */
    fun requestFaceFeature(nv21: ByteArray, faceInfo: FaceInfo, width: Int, height: Int, format: Int, trackId: Int) {
        if (_detectListener != null) {
            if (_faceFeatureEngine != null && _faceFeatureThreadQueue.remainingCapacity() > 0) {
                _faceFeatureExecutor.execute(FaceRecognizeRunnable(nv21, faceInfo, width, height, format, trackId))
            } else {
                _detectListener!!.onFaceFeatureInfoGet(nv21, faceInfo, null, trackId, com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_BUSY)
            }
        }
    }

    inner class FaceRecognizeRunnable(nv21Data: ByteArray, faceInfo: FaceInfo, width: Int, height: Int, format: Int, trackId: Int) : Runnable {
        private val _faceInfo: FaceInfo
        private val _width: Int
        private val _height: Int
        private val _format: Int
        private val _trackId: Int
        private var _nv21Data: ByteArray?

        override fun run() {
            if (_detectListener != null) {
                if (_faceFeatureEngine != null) {
                    val faceFeature = FaceFeature()
                    val lastTime = System.currentTimeMillis()
                    var faceFeatureCode: Int
                    synchronized(_faceFeatureEngine!!) {
                        faceFeatureCode = _faceFeatureEngine!!.extractFaceFeature(_nv21Data, _width, _height, _format, _faceInfo, ExtractType.RECOGNIZE, MaskInfo.NOT_WORN, faceFeature)
                    }
                    if (faceFeatureCode == ErrorInfo.MOK) {
                        Log.d(TAG, "FaceRecognizeRunnable extractFaceFeature success costTime = ${System.currentTimeMillis() - lastTime}ms")
                        _detectListener!!.onFaceFeatureInfoGet(_nv21Data!!, _faceInfo, faceFeature, _trackId, faceFeatureCode)
                    } else {
                        _detectListener!!.onFaceFeatureInfoGet(_nv21Data!!, _faceInfo, null, _trackId, faceFeatureCode)
                        _detectListener!!.onFail(Exception("FaceRecognizeRunnable extractFaceFeature failed errorCode: $faceFeatureCode"))
                    }
                } else {
                    _detectListener!!.onFaceFeatureInfoGet(_nv21Data!!, _faceInfo, null, _trackId, com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_IS_NULL)
                    _detectListener!!.onFail(Exception("FaceRecognizeRunnable extractFaceFeature failed ${com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_IS_NULL)}"))
                }
            }
            _nv21Data = null
        }

        init {
            this._nv21Data = nv21Data.clone()
            this._faceInfo = FaceInfo(faceInfo)
            this._width = width
            this._height = height
            this._format = format
            this._trackId = trackId
        }
    }

    private fun refreshTrackId(faceInfos: List<FaceInfo>) {
        _currentTrackIdList.clear()
        for (faceInfo in faceInfos) {
            _currentTrackIdList.add(faceInfo.faceId + _detectedFaceCount)
        }
        if (faceInfos.isNotEmpty()) {
            _currentMaxFaceId = faceInfos[faceInfos.size - 1].faceId
        }
        clearLastPerson(_currentTrackIdList)        //刷新nameMap
    }

    /**
     * 清除map中已经离开的人脸
     * @param trackIdList List<Int>
     */
    private fun clearLastPerson(trackIdList: List<Int>) {
        val keys: Enumeration<Int> = _personMap.keys()
        while (keys.hasMoreElements()) {
            val value = keys.nextElement()
            if (!trackIdList.contains(value)) {
                _personMap.remove(value)
            }
        }
    }

    class Builder {
        private var _faceTraceEngine: FaceEngine? = null
        private var _faceFeatureEngine: FaceEngine? = null
        private var _previewSize: Size? = null
        private var _faceFeatureQueueSize = 0
        private var _detectFaceCount = 0
        private var _detectListener: IDetectListener? = null

        fun setFaceTraceEngine(value: FaceEngine): Builder {
            _faceTraceEngine = value
            return this
        }

        fun getFaceTraceEngine(): FaceEngine? = _faceTraceEngine

        fun setFaceFeatureEngine(value: FaceEngine): Builder {
            _faceFeatureEngine = value
            return this
        }

        fun getFaceFeatureEngine(): FaceEngine? = _faceFeatureEngine

        fun setPreviewSize(value: Size): Builder {
            _previewSize = value
            return this
        }

        fun getPreviewSize(): Size? = _previewSize

        fun setDetectListener(value: IDetectListener): Builder {
            _detectListener = value
            return this
        }

        fun getDetectListener(): IDetectListener? = _detectListener

        fun setFaceFeatureQueueSize(value: Int): Builder {
            _faceFeatureQueueSize = value
            return this
        }

        fun getFaceFeatureQueueSize(): Int = _faceFeatureQueueSize

        fun setDetectFaceCount(value: Int): Builder {
            _detectFaceCount = value
            return this
        }

        fun getDetectFaceCount(): Int = _detectFaceCount

        fun build(): ScanKArcFaceHelper {
            return ScanKArcFaceHelper(this)
        }
    }
}