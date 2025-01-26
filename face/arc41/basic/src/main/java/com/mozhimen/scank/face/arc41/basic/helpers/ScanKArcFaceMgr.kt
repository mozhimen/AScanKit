package com.mozhimen.scank.face.arc41.basic.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import com.arcsoft.face.*
import com.arcsoft.face.FaceInfo
import com.arcsoft.face.enums.DetectFaceOrientPriority
import com.arcsoft.face.enums.DetectMode
import com.mozhimen.scank.face.arc41.basic.commons.IDetectListener
import com.mozhimen.scank.face.arc41.basic.cons.ScanKArcFaceConfig
import com.mozhimen.scank.face.arc41.basic.mos.FacePreviewInfo
import com.mozhimen.scank.face.arc41.basic.mos.LivenessResult
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationReflect
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrFile
import com.mozhimen.scank.face.arc.basic.mos.CompareInfo
import com.mozhimen.scank.face.arc.basic.mos.DetectConfig
import com.mozhimen.scank.face.arc.basic.mos.EngineConfig
import com.mozhimen.scank.face.arc.basic.mos.FaceLocation
import com.mozhimen.scank.face.arc.basic.mos.FaceRegisterInfo
import com.mozhimen.scank.face.arc.basic.mos.ImageConfig
import com.mozhimen.scank.face.arc.basic.mos.ParamConfig
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 * @ClassName ScanKArcFaceMrg
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2022/12/12 16:26
 * @Version 1.0
 */
class ScanKArcFaceMgr : com.mozhimen.scank.face.arc.basic.commons.IScanKArcFace<DetectConfig, com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback> {
    companion object {
        private const val TAG = "ScanKArcFaceMgr>>>>>"

        @SuppressLint("StaticFieldLeak")
        @JvmStatic//单例内部静态类,线程安全
        val instance = ScanKArcFaceMgrProvider.holder
    }

    private object ScanKArcFaceMgrProvider {
        @SuppressLint("StaticFieldLeak")
        val holder = ScanKArcFaceMgr()
    }

    private val _context: Context = UtilKApplicationReflect.instance.get()

    private var _isLibraryExists = true    //库是否存在
    private var _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.IDLE

    private var _isRegister = false //是否注册成功
    private var _isDetectStart = false //是否开始识别

    private var _binocularLivenessImpl: BinocularLivenessImpl = BinocularLivenessImpl()

    private var _trackHandlerThread: HandlerThread? = null
    private var _trackHandler: Handler? = null

    private var _faceTraceEngine: FaceEngine? = null    //人脸追踪引擎
    private var _faceFeatureEngine: FaceEngine? = null    //特征提取引擎

    private var _ftInitCode = -1    //活体检测引擎
    private var _frInitCode = -1

    private val _compareResultList: MutableList<CompareInfo> = ArrayList()
    private var _faceHelper: ScanKArcFaceHelper? = null

    private val _requestFeatureStatusMap = ConcurrentHashMap<Int, Int>()    //用于记录人脸识别相关状态
    private val _extractErrorRetryMap = ConcurrentHashMap<Int, Int>()    //用于记录人脸特征提取出错重试次数
    private val _delayFaceTaskCompositeDisposable = CompositeDisposable()
    private val _getFeatureDelayedDisposables: CompositeDisposable = CompositeDisposable()

    @Transient
    private var _imageBytes: ByteArray? = null

    private val _faceListener: IDetectListener = object : IDetectListener {
        override fun onFaceLivenessInfoGet(livenessInfo: LivenessInfo?, trackId: Int?, errorCode: Int?) {}

        override fun onFail(e: Exception?) {}

        override fun onFaceFeatureInfoGet(nv21: ByteArray, faceInfo: FaceInfo, faceFeature: FaceFeature?, trackId: Int, errorCode: Int) {
            if (faceFeature != null) {
                searchPerson(nv21, faceInfo, faceFeature, trackId)
            } else {
                if (increaseAndGetValue(_extractErrorRetryMap, trackId) > ScanKArcFaceConfig.NUM_RETRY_MAX) {
                    _extractErrorRetryMap[trackId] = 0
                    // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                    val msg: String =
                        if (errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) "人脸置信度低" else "ExtractCode: $errorCode"
                    _faceHelper!!.addPerson(trackId, "未通过: $msg")
                    // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                    _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RES_FAILED
                    retryRecognizeDelayed(trackId)
                } else {
                    _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RETRY
                }
            }
        }
    }

    override fun init(initListener: com.mozhimen.scank.face.arc.basic.commons.IInitListener?) {
        if (_isRegister || _status == com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED) {
            initListener?.onInitResult(false, "already init")
            return
        }

        _trackHandlerThread = HandlerThread("_trackHandlerThread")
        _trackHandlerThread!!.start()
        _trackHandler = Handler(_trackHandlerThread!!.looper)

        kotlin.runCatching {
            //搜库
            _isLibraryExists = ScanKArcFaceUtil.checkSoLibrary(ScanKArcFaceConfig.LIBRARIES)
            if (_isLibraryExists) {
                val versionInfo = VersionInfo()
                val code = FaceEngine.getVersion(versionInfo)
                Log.d(TAG, "init: getVersion $code")
            } else {
                Log.e(TAG, "init: native library load fail")
                throw Exception(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_LIBRARY_LOADED_FAIL))
            }

            //激活引擎
            val activeFileInfo = ActiveFileInfo()
            val res = FaceEngine.getActiveFileInfo(_context, activeFileInfo)
            Log.d(TAG, "init: getActiveFileInfo $res")
            if (res == ErrorInfo.MOK) {
                Log.d(TAG, "init: start initEngine")
                initEngine(initListener)
            } else {
                Log.d(TAG, "init: start activeEngine")
                activeEngine(initListener)
            }
        }.onFailure {
            _isRegister = false
            _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.IDLE
            initListener?.onInitResult(false, it.message ?: "init fail")
        }
    }

    /**
     * 激活引擎
     * @param initListener IInitListener?
     * @throws Exception
     */
    @Throws(Exception::class)
    fun activeEngine(initListener: com.mozhimen.scank.face.arc.basic.commons.IInitListener?) {
        if (!_isLibraryExists) {
            throw Exception(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_LIBRARY_LOADED_FAIL))
        }

        val runtimeABI = FaceEngine.getRuntimeABI()
        val activeCode = if (!_binocularLivenessImpl.getConfig().getEngineConfig().isActiveOffline) {
            Log.d(
                TAG,
                "activeEngine: activeOnline activeKey ${_binocularLivenessImpl.getConfig().getEngineConfig().activeKey} appId ${
                    _binocularLivenessImpl.getConfig().getEngineConfig().appId
                } sdkKey ${_binocularLivenessImpl.getConfig().getEngineConfig().sdkKey}"
            )
            FaceEngine.activeOnline(
                _context,
                _binocularLivenessImpl.getConfig().getEngineConfig().activeKey,
                _binocularLivenessImpl.getConfig().getEngineConfig().appId,
                _binocularLivenessImpl.getConfig().getEngineConfig().sdkKey
            )
        } else {
            Log.d(TAG, "activeEngine: activeOffline activeOfflineFilePath ${_binocularLivenessImpl.getConfig().getEngineConfig().activeOfflineFilePath}")
            FaceEngine.activeOffline(
                _context, _binocularLivenessImpl.getConfig().getEngineConfig().activeOfflineFilePath
            )
        }
        Log.d(TAG, "activeEngine: runtimeABI $runtimeABI activeCode $activeCode")

        //激活文件信息
        val activeFileInfo = ActiveFileInfo()
        val res = FaceEngine.getActiveFileInfo(_context, activeFileInfo)
        Log.d(TAG, "activeEngine: getActiveFileInfo $res")

        when (activeCode) {
            ErrorInfo.MOK -> {
                Log.d(TAG, "activeEngine: res success")
                initEngine(initListener)
            }
            ErrorInfo.MERR_ASF_ALREADY_ACTIVATED -> {
                Log.d(TAG, "activeEngine: engine already active start init")
                initEngine(initListener)
            }
            else -> {
                throw Exception(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_ACTIVE_FAIL))
            }
        }
    }

    @Throws(Exception::class)
    private fun initEngine(initListener: com.mozhimen.scank.face.arc.basic.commons.IInitListener?) {
        //faceFeatureEngine
        _faceTraceEngine = FaceEngine()
        var detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_ALL_OUT
        when (_binocularLivenessImpl.getConfig().getImageConfig().imageOrientation) {
            com.mozhimen.scank.face.arc.basic.annors.AImageOrientation.UP -> detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_0_ONLY
            com.mozhimen.scank.face.arc.basic.annors.AImageOrientation.RIGHT -> detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_90_ONLY
            com.mozhimen.scank.face.arc.basic.annors.AImageOrientation.DOWN -> detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_180_ONLY
            com.mozhimen.scank.face.arc.basic.annors.AImageOrientation.LEFT -> detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_270_ONLY
        }
        _ftInitCode = _faceTraceEngine!!.init(
            _context,
            DetectMode.ASF_DETECT_MODE_VIDEO,
            detectFaceOrientPriority,
            ScanKArcFaceConfig.NUM_MAX_DETECT,
            FaceEngine.ASF_FACE_DETECT
        )

        //faceFeatureEngine
        _faceFeatureEngine = FaceEngine()
        _frInitCode = _faceFeatureEngine!!.init(
            _context,
            DetectMode.ASF_DETECT_MODE_IMAGE,
            DetectFaceOrientPriority.ASF_OP_0_ONLY,
            ScanKArcFaceConfig.NUM_MAX_DETECT,
            FaceEngine.ASF_FACE_RECOGNITION
        )

        if (_ftInitCode != ErrorInfo.MOK) {
            Log.e(TAG, "initEngine: faceTraceEngine fail code:$_ftInitCode")
            throw Exception(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_INIT_FAIL))
        } else if (_frInitCode != ErrorInfo.MOK) {
            Log.e(TAG, "initEngine: faceFeatureEngine fail code:$_ftInitCode")
            throw Exception(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_INIT_FAIL))
        } else {
            Log.w(TAG, "initEngine: success")
            FaceServer.getInstance().init(_context)
            _isRegister = true
            _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED
            initListener?.onInitResult(true, "face init success")
        }
    }

    override fun setEngineConfig(engineConfig: EngineConfig) {
        _binocularLivenessImpl.getConfig().setEngineConfig(engineConfig)
    }

    override fun setImageConfig(imageConfig: ImageConfig) {
        _binocularLivenessImpl.getConfig().setImageConfig(imageConfig)
    }

    override fun setParamConfig(paramConfig: ParamConfig) {
        _binocularLivenessImpl.getConfig().setParamConfig(paramConfig)
    }

    override fun isInitSuccess(): Boolean {
        return _isRegister || _status >= com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED
    }

    override fun addListener(listener: com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback) {
        _binocularLivenessImpl.addListener(listener)
    }

    override fun removeListener(listener: com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback) {
        _binocularLivenessImpl.removeListener(listener)
    }

    @Synchronized
    fun deleteAllCache() {
        FaceServer.getInstance().clearAllFaces(_context)
    }

    @Synchronized
    fun deletePersonFeatureById(personUuid: String) {
        FaceServer.getInstance().deleteFeatureForById(_context, personUuid)
    }

    /**
     * 根据注册人脸personUuid获取Bitmap
     * @param personUuid String
     * @return Bitmap
     */
    fun getPersonBitmapById(personUuid: String): Bitmap? {
        val path = FaceServer.getInstance().getFaceImagePathForByName(personUuid)
        return UtilKStrFile.strFilePath2bitmapAny(path)
    }

    @Synchronized
    fun isPersonFeatureExistById(personUuid: String): Boolean {
        return FaceServer.getInstance().isExistFeatureForById(_context, personUuid)
    }

    /**
     * 特征值比对
     * @param f1 FaceFeature
     * @param f2 FaceFeature
     * @return Boolean
     */
    fun compareFeature(f1: FaceFeature, f2: FaceFeature): Boolean {
        return FaceServer.getInstance().compareFeature(f1, f2) > ScanKArcFaceConfig.THRESHOLD_VERIFY_SCORE_DEFAULT
    }

    /**
     * 特征值查询
     * @param feature ByteArray
     * @return CompareInfo
     */
    fun searchFeature(feature: ByteArray): CompareInfo {
        return FaceServer.getInstance().getTopOfFaceLib(FaceFeature(feature))
    }

    /**
     * 搜寻人脸
     * @param nv21 ByteArray
     * @param faceInfo FaceInfo
     * @param frFace FaceFeature
     * @param trackId Int
     */
    fun searchPerson(nv21: ByteArray, faceInfo: FaceInfo, frFace: FaceFeature, trackId: Int) {
        Observable
            .create { emitter ->
                val compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace)
                if (compareResult == null) {
                    emitter.onError(Throwable("no such person"))
                } else {
                    emitter.onNext(compareResult)
                }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CompareInfo> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(compareResult: CompareInfo) {
                    if (compareResult.personUuid == null || (compareResult.personUuid != null && compareResult.personUuid!!.isEmpty())) {
                        _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RES_FAILED

                        _faceHelper!!.addPerson(trackId, "VISITOR $trackId")
                        retryFailRecognizeDelayed(trackId)

                        val bitmap = FaceServer.getInstance().getHeadBitmap(
                            nv21,
                            _binocularLivenessImpl.getConfig().getImageConfig().previewW,
                            _binocularLivenessImpl.getConfig().getImageConfig().previewH, faceInfo
                        )
                        val rect = Rect(faceInfo.rect.left, faceInfo.rect.top, faceInfo.rect.right, faceInfo.rect.bottom)
                        _binocularLivenessImpl.onLivenessResultGet(
                            com.mozhimen.scank.face.arc.basic.annors.ADetectResCode.RES_HACK,
                            LivenessResult(trackId, bitmap, rect, null, faceInfo)
                        )
                        return
                    }

                    if (compareResult.similar > ScanKArcFaceConfig.THRESHOLD_SIMILAR) {
                        Log.i(TAG, "searchPerson: similar success")
                        var isAdded = false

                        for (cr in _compareResultList) {
                            if (cr.trackId == trackId) {
                                isAdded = true
                                break
                            }
                        }
                        if (!isAdded) {
                            //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                            if (_compareResultList.size >= ScanKArcFaceConfig.NUM_MAX_DETECT) {
                                _compareResultList.removeAt(0)
                            }
                            //添加显示人员时，保存其trackId
                            compareResult.trackId = trackId
                            _compareResultList.add(compareResult)

                            val bitmap =
                                FaceServer.getInstance().getHeadBitmap(
                                    nv21, _binocularLivenessImpl.getConfig().getImageConfig().previewW,
                                    _binocularLivenessImpl.getConfig().getImageConfig().previewH, faceInfo
                                )
                            val rect = Rect(faceInfo.rect.left, faceInfo.rect.top, faceInfo.rect.right, faceInfo.rect.bottom)
                            _binocularLivenessImpl.onLivenessResultGet(
                                com.mozhimen.scank.face.arc.basic.annors.ADetectResCode.RES_OK,
                                LivenessResult(compareResult.trackId, bitmap, rect, compareResult, faceInfo)
                            )
                        }
                        _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RES_SUCCEED
                        _faceHelper!!.addPerson(trackId, "通过: ${compareResult.personUuid}")
                    } else {
                        Log.i(TAG, "searchPerson: similar fail compareResult $compareResult")
                        _faceHelper!!.addPerson(trackId, "未通过: NOT_REGISTERED")
                        retryFailRecognizeDelayed(trackId)

                        val bitmap = FaceServer.getInstance().getHeadBitmap(
                            nv21, _binocularLivenessImpl.getConfig().getImageConfig().previewW,
                            _binocularLivenessImpl.getConfig().getImageConfig().previewH, faceInfo
                        )
                        val rect = Rect(faceInfo.rect.left, faceInfo.rect.top, faceInfo.rect.right, faceInfo.rect.bottom)
                        _binocularLivenessImpl.onLivenessResultGet(
                            com.mozhimen.scank.face.arc.basic.annors.ADetectResCode.RES_HACK,
                            LivenessResult(compareResult.trackId, bitmap, rect, compareResult, faceInfo)
                        )
                    }
                }

                override fun onError(e: Throwable) {
                    _faceHelper!!.addPerson(trackId, "未通过: NOT_REGISTERED")
                    retryFailRecognizeDelayed(trackId)

                    val bitmap: Bitmap? = try {
                        FaceServer.getInstance().getHeadBitmap(
                            nv21, _binocularLivenessImpl.getConfig().getImageConfig().previewW,
                            _binocularLivenessImpl.getConfig().getImageConfig().previewH, faceInfo
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    val rect = Rect(faceInfo.rect.left, faceInfo.rect.top, faceInfo.rect.right, faceInfo.rect.bottom)
                    _binocularLivenessImpl.onLivenessResultGet(
                        com.mozhimen.scank.face.arc.basic.annors.ADetectResCode.RES_HACK,
                        LivenessResult(trackId, bitmap, rect, null, faceInfo)
                    )
                }

                override fun onComplete() {}
            })
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     * @param trackId Int
     */
    fun retryFailRecognizeDelayed(trackId: Int) {
        _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RES_FAILED
        Observable.timer(ScanKArcFaceConfig.INTERVAL_RETRY_FAIL, TimeUnit.MILLISECONDS)
            .subscribe(object : Observer<Long> {
                var disposable: Disposable? = null
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                    _delayFaceTaskCompositeDisposable.add(disposable!!)
                }

                override fun onNext(aLong: Long) {}
                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {
                    // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                    _faceHelper!!.addPerson(trackId, trackId.toString())
                    _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RETRY
                    _delayFaceTaskCompositeDisposable.remove(disposable!!)
                }
            })
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     * @param trackId Int
     */
    fun retryRecognizeDelayed(trackId: Int) {
        _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RES_FAILED
        Observable.timer(ScanKArcFaceConfig.INTERVAL_RETRY_NORMAL, TimeUnit.MILLISECONDS)
            .subscribe(object : Observer<Long> {
                var disposable: Disposable? = null
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                    _delayFaceTaskCompositeDisposable.add(disposable!!)
                }

                override fun onNext(aLong: Long) {}
                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {
                    // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                    _faceHelper!!.addPerson(trackId, trackId.toString())
                    _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RETRY
                    _delayFaceTaskCompositeDisposable.remove(disposable!!)
                }
            })
    }

    /**
     * 获取注册信息并且保存特征数据库
     * @param personUuid String
     * @param bitmap Bitmap
     * @return FaceRegisterInfo
     */
    @Synchronized
    fun extractFeatureAndInsert(personUuid: String, bitmap: Bitmap): FaceRegisterInfo? {
        return FaceServer.getInstance().getFaceFeatureForBitmap(_context, bitmap, personUuid)
    }

    /**
     * 删除已经离开的人脸
     * @param facePreviewInfoList List<FacePreviewInfo>
     */
    private fun clearLastFace(facePreviewInfoList: List<FacePreviewInfo>?) { //人脸和trackId列表
        for (i in _compareResultList.indices.reversed()) {
            if (!_requestFeatureStatusMap.containsKey(_compareResultList[i].trackId)) {
                _compareResultList.removeAt(i)
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.isEmpty()) {
            _requestFeatureStatusMap.clear()
            _extractErrorRetryMap.clear()
            _getFeatureDelayedDisposables.clear()
            return
        }
        val keys = _requestFeatureStatusMap.keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            var contained = false
            for (facePreviewInfo in facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true
                    break
                }
            }
            if (!contained) {
                _requestFeatureStatusMap.remove(key)
                _extractErrorRetryMap.remove(key)
            }
        }
    }

    fun insertPersonFeature(userName: String, bytes: ByteArray) {
        FaceServer.getInstance().insert(userName, bytes)
    }

    fun clearFaceRecord(trackId: Int) {
        _compareResultList.clear()
        retrySuccessRecognizeDelayed(trackId)
    }

    override fun input(imageBytes: ByteArray) {
        if (imageBytes.isNotEmpty() && _isRegister && _isDetectStart) {
            _imageBytes = imageBytes
            processPreviewData()
            _imageBytes = null
        }
    }

    override fun removeAllListeners() {
        _binocularLivenessImpl.removeListeners()
    }

    @Synchronized
    private fun processPreviewData() {
        if (_imageBytes != null) {
            val cloneImageBytes = _imageBytes!!.clone()
            //刷新人脸框
            val facePreviewInfoList = _faceHelper!!.onPreviewFrame(cloneImageBytes)
            //刷新人脸框 只显示第一个脸
            Log.v(TAG, "processPreviewData: facePreviewInfoList $facePreviewInfoList")
            if (facePreviewInfoList.isNotEmpty()) {
                _binocularLivenessImpl.onFaceLocationGet(
                    FaceLocation(
                        facePreviewInfoList[0].getFaceInfo()!!.rect.left, facePreviewInfoList[0].getFaceInfo()!!.rect.top,
                        facePreviewInfoList[0].getFaceInfo()!!.rect.right, facePreviewInfoList[0].getFaceInfo()!!.rect.bottom
                    )
                )
                if (facePreviewInfoList[0].getFaceInfo()!!.rect.bottom - facePreviewInfoList[0].getFaceInfo()!!.rect.top < 80 || facePreviewInfoList[0].getFaceInfo()!!.rect.right - facePreviewInfoList[0].getFaceInfo()!!.rect.left < 80) {
                    clearLastFace(facePreviewInfoList)
                    return
                }
            } else {
                _binocularLivenessImpl.onFaceLocationGet(null)
            }

            clearLastFace(facePreviewInfoList)

            if (facePreviewInfoList.isNotEmpty()) {
                for (i in facePreviewInfoList.indices) {
                    // 注意：这里虽然使用的是IR画面活体检测，RGB画面特征提取，但是考虑到成像接近，所以只用了RGB画面的图像质量检测
                    val status = _requestFeatureStatusMap[facePreviewInfoList[i].getTrackId()]
                    // 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                    // 特征提取回传的人脸特征结果在[IFaceListener.onFaceFeatureInfoGet]中回传
                    if (status == null || status == com.mozhimen.scank.face.arc.basic.annors.AProcess.RETRY) {
                        _requestFeatureStatusMap[facePreviewInfoList[i].getTrackId()] = com.mozhimen.scank.face.arc.basic.annors.AProcess.DETECTING
                        _faceHelper!!.requestFaceFeature(
                            cloneImageBytes,
                            facePreviewInfoList[i].getFaceInfo()!!,
                            _binocularLivenessImpl.getConfig().getImageConfig().previewW,
                            _binocularLivenessImpl.getConfig().getImageConfig().previewH,
                            FaceEngine.CP_PAF_NV21,
                            facePreviewInfoList[i].getTrackId()
                        )
                    }
                }
            }
            _imageBytes = null
        }
    }

    @Throws(Exception::class)
    override fun start() {
        if (!_isRegister || _status < com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED) {
            throw Exception(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.getMessage(com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.ERR_ENGINE_INIT_FAIL))
        }
        if (_faceHelper == null) {
            _faceHelper = ScanKArcFaceHelper.Builder()
                .setFaceTraceEngine(_faceTraceEngine!!)
                .setFaceFeatureEngine(_faceFeatureEngine!!)
                .setFaceFeatureQueueSize(ScanKArcFaceConfig.NUM_MAX_DETECT)
                .setPreviewSize(
                    Size(
                        _binocularLivenessImpl.getConfig().getImageConfig().previewW,
                        _binocularLivenessImpl.getConfig().getImageConfig().previewH
                    )
                )
                .setDetectListener(_faceListener)
                .setDetectFaceCount(ScanKArcFaceConfig.trackedFaceCount)
                .build()
        }
        _isDetectStart = true
        _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.DETECTING
    }

    override fun reStart() {
        if (!_isRegister || _status < com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED) {
            Log.e(TAG, "reStart: engine has not init")
            return
        }
        _isDetectStart = true
        _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED
    }

    override fun stop() {
        if (!_isRegister || _status < com.mozhimen.scank.face.arc.basic.annors.AProcess.INITED) {
            Log.e(TAG, "stop: engine has not init")
            return
        }
        _isDetectStart = false
        _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.STOPED
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     * @param trackId Int
     */
    private fun retrySuccessRecognizeDelayed(trackId: Int) {
        _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RES_FAILED
        Observable.timer(ScanKArcFaceConfig.INTERVAL_RETRY_SUCCESS_, TimeUnit.MILLISECONDS)
            .subscribe(object : Observer<Long?> {
                var disposable: Disposable? = null
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                    _delayFaceTaskCompositeDisposable.add(disposable!!)
                }

                override fun onNext(t: Long) {}
                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {
                    // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                    _faceHelper!!.addPerson(trackId, trackId.toString())
                    _requestFeatureStatusMap[trackId] = com.mozhimen.scank.face.arc.basic.annors.AProcess.RETRY
                    _delayFaceTaskCompositeDisposable.remove(disposable!!)
                }
            })
    }

    override fun release() {
        unInitEngine()
        FaceServer.getInstance().unInit()
        if (_faceHelper != null) {
            ScanKArcFaceConfig.trackedFaceCount = _faceHelper!!.getTrackedFaceCount()
            _faceHelper!!.release()
            _faceHelper = null
        }
        _binocularLivenessImpl.onDestroy()

        _trackHandler?.removeCallbacksAndMessages(null)
        _trackHandler = null
        _trackHandlerThread?.quit()
        _trackHandlerThread?.interrupt()
        _trackHandlerThread = null

        _isDetectStart = false
        _isRegister = false
        _status = com.mozhimen.scank.face.arc.basic.annors.AProcess.IDLE
    }

    /**
     * 销毁引擎，faceHelperIr中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private fun unInitEngine() {
        if (_ftInitCode == ErrorInfo.MOK && _faceTraceEngine != null) {
            synchronized(_faceTraceEngine!!) {
                val ftUnInitCode = _faceTraceEngine!!.unInit()
                Log.d(TAG, "unInitEngine: ftUnInitCode: $ftUnInitCode")
            }
        }
        if (_frInitCode == ErrorInfo.MOK && _faceFeatureEngine != null) {
            synchronized(_faceFeatureEngine!!) {
                val frUnInitCode = _faceFeatureEngine!!.unInit()
                Log.d(TAG, "unInitEngine: frUnInitCode: $frUnInitCode")
            }
        }
    }

    /**
     * 将map中key对应的value增1回传
     * @param countMap MutableMap<Int, Int>
     * @param key Int
     * @return Int
     */
    fun increaseAndGetValue(countMap: MutableMap<Int, Int>, key: Int): Int {
        var value = countMap[key]
        if (value == null) {
            value = 0
        }
        countMap[key] = ++value
        return value
    }

}
