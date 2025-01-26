package com.mozhimen.scank.face.arc.test.demo

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.lifecycleScope
import com.mozhimen.bindk.bases.viewdatabinding.activity.BaseActivityVDB
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OFieldCall_Close
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_CAMERA
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.kotlin.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.kotlin.utilk.android.content.UtilKRes
import com.mozhimen.camerak.camerax.annors.ACameraKXFacing
import com.mozhimen.camerak.camerax.annors.ACameraKXFormat
import com.mozhimen.camerak.camerax.commons.ICameraXKFrameListener
import com.mozhimen.camerak.camerax.mos.CameraKXConfig
import com.mozhimen.camerak.camerax.utils.ImageProxyUtil
import com.mozhimen.scank.face.arc.basic.annors.AImageOrientation
import com.mozhimen.scank.face.arc.basic.mos.DetectResult
import com.mozhimen.scank.face.arc.basic.mos.EngineConfig
import com.mozhimen.scank.face.arc.basic.mos.FaceLocation
import com.mozhimen.scank.face.arc.basic.mos.ImageConfig
import com.mozhimen.scank.face.arc.test.R
import com.mozhimen.scank.face.arc.test.databinding.ActivityDemoBinding
import com.mozhimen.scank.face.arc41.TaskKExtract
import com.mozhimen.scank.face.arc41.basic.helpers.ScanKArcFaceMgr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @ClassName DemoActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2022/12/9 16:10
 * @Version 1.0
 */
@SuppressLint("UnsafeOptInUsageError")
@APermissionCheck(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_PHONE_STATE
)
class DemoActivity : BaseActivityVDB<ActivityDemoBinding>() {
    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class)
    private val _taskKGenFaceInfo by lazy { TaskKExtract().apply { bindLifecycle(this@DemoActivity) } }
    private val _engineConfigs = listOf(
        EngineConfig(
            deviceName = "公司的设备OPPO",
            appId = "JBR1d4sJoLBkqrmuruYkHKSEYcU12x7zyZ6myZaujaF2",
            sdkKey = "9LaiybkYsA6YZz9txuV5p7dpUD8bqypEK437rBvqT9JX",
            activeKey = "85F1-11G5-A12L-9PQY"
        ),
        EngineConfig(
            deviceName = "家里的设备XIAOMI",
            appId = "JBR1d4sJoLBkqrmuruYkHKSEYcU12x7zyZ6myZaujaF2",
            sdkKey = "9LaiybkYsA6YZz9txuV5p7dpUD8bqypEK437rBvqT9JX",
            activeKey = "85F1-11G5-A13G-T6PU"
        )
    )

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it) {
                super.initData(savedInstanceState)
            } else {
                Log.d(TAG, "initData: PermissionK denied")
                UtilKLaunchActivity.startSettingAppDetails(this)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initScanKArcFace()
        initCamera()
    }

    private var _currentDetectTime = System.currentTimeMillis()

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class)
    private fun initScanKArcFace() {
        ScanKArcFaceMgr.instance.apply {
            setEngineConfig(_engineConfigs[1])
            setImageConfig(ImageConfig(previewW = 864, previewH = 480, imageOrientation = AImageOrientation.LEFT))
            init(object : com.mozhimen.scank.face.arc.basic.commons.IInitListener {
                override fun onInitResult(isSuccess: Boolean, msg: String) {
                    try {
                        if (isSuccess) {
                            Log.i(TAG, "initScanKArcFace init success")
                            ScanKArcFaceMgr.instance.start()

                            lifecycleScope.launch(Dispatchers.IO) {
                                _taskKGenFaceInfo.analyzePerson(
                                    com.mozhimen.scank.face.arc.basic.db.DBPerson("01", "mozhimen", "", "", ""),
                                    (UtilKRes.gainDrawable(R.mipmap.ic_face) as BitmapDrawable).bitmap!!,
                                    com.mozhimen.scank.face.arc.basic.db.DBMgr.dbPersonDao.selectAllDBPersons() ?: emptyList()
                                )
                            }
                        } else {
                            Log.i(TAG, "initScanKArcFace init fail")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            addListener(object : com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback() {
                override fun onFaceLocationGet(faceLocation: FaceLocation?) {
                    if (faceLocation != null) {
                        Log.d(TAG, "onTrackResult: $faceLocation")
                    }
                }

                override fun onFaceStatusChange(detectStatus: Int) {}

                override fun onDetectResultGet(code: Int, detectResult: DetectResult) {
                    Log.d(TAG, "onDetectResultGet: code $code")
                    ScanKArcFaceMgr.instance.clearFaceRecord(detectResult.trackId)

                    when (code) {
                        com.mozhimen.scank.face.arc.basic.annors.ADetectResCode.RES_OK -> {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val dbPerson = com.mozhimen.scank.face.arc.basic.db.DBMgr.dbPersonDao.selectDBPersonByPersonUuid(detectResult.detectFaceInfo!!.personUuid!!)
                                if (dbPerson == null) {
                                    Log.e(TAG, "onDetectResultGet: no such person")
                                    return@launch
                                }
                                Log.d(TAG, "onDetectResultGet: success person: $dbPerson")
                                ScanKArcFaceMgr.instance.clearFaceRecord(detectResult.trackId)
                            }
                        }
                        com.mozhimen.scank.face.arc.basic.annors.ADetectResCode.RES_HACK -> {
                            Log.i(TAG, "onDetectResultGet: HACK")
                        }
                    }
                }

                override fun onInitError(initErrorCode: Int) {
                    Log.e(TAG, "onInitError: $initErrorCode")
                }
            })
        }
    }

    @OptIn(OPermission_CAMERA::class)
    private fun initCamera() {
        vdb.demoPreview.initCameraKX(this, CameraKXConfig(ACameraKXFormat.YUV_420_888, ACameraKXFacing.FRONT))
        vdb.demoPreview.setCameraXFrameListener(_frameAnalyzer)
    }

    private var _nv21: ByteArray? = null
    private var _bitmap: Bitmap? = null

    @OptIn(OFieldCall_Close::class)
    private val _frameAnalyzer: ICameraXKFrameListener by lazy {
        object :ICameraXKFrameListener{
            override fun invoke(image: ImageProxy) {
                Log.v(TAG, "ImageAnalysis: image w ${image.width} h ${image.height}")
                _nv21 = ImageProxyUtil.imageProxyYuv4208882bytesNv21(image)
                if (System.currentTimeMillis() - _currentDetectTime > 1000) {
                    _currentDetectTime = System.currentTimeMillis()

                    lifecycleScope.launch(Dispatchers.IO) {
                        ScanKArcFaceMgr.instance.input(_nv21!!)

                        _bitmap = ImageProxyUtil.imageProxyYuv4208882bitmapJpeg(image)

                        withContext(Dispatchers.Main) {
                            _bitmap?.let {
                                vdb.demoImg.setImageBitmap(it)
                            }
                        }
                        image.close()
                    }
                } else {
                    image.close()
                }
            }
        }
    }

    override fun onPause() {
        if (ScanKArcFaceMgr.instance.isInitSuccess()) {
            ScanKArcFaceMgr.instance.stop()
        }
        super.onPause()
    }

    override fun onRestart() {
        if (ScanKArcFaceMgr.instance.isInitSuccess()) {
            ScanKArcFaceMgr.instance.reStart()
        }
        super.onRestart()
    }

    override fun onDestroy() {
        ScanKArcFaceMgr.instance.release()
        super.onDestroy()
    }
}