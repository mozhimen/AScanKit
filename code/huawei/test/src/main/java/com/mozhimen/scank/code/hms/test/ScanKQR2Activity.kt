package com.mozhimen.scank.code.hms.test

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import androidx.camera.core.ImageProxy
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVDB
import com.mozhimen.basick.lintk.optins.OFieldCall_Close
import com.mozhimen.basick.lintk.optins.permission.OPermission_CAMERA
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.basick.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.basick.utilk.android.graphics.applyBitmapAnyCrop
import com.mozhimen.basick.utilk.android.graphics.applyBitmapAnyRotate
import com.mozhimen.basick.utilk.android.graphics.compressBitmapAnyScaled
import com.mozhimen.basick.utilk.android.view.UtilKScreen
import com.mozhimen.basick.utilk.google.gson.t2strJsonGson
import com.mozhimen.camerak.camerax.commons.ICameraXKFrameListener
import com.mozhimen.camerak.camerax.utils.imageProxyJpeg2bitmapJpeg
import com.mozhimen.camerak.camerax.utils.imageProxyYuv4208882bitmapJpeg
import com.mozhimen.scank.code.hms.ScanKCodeHms
import com.mozhimen.scank.code.hms.test.databinding.ScankQr2ActivityBinding

@APermissionCheck(CPermission.CAMERA, CPermission.READ_EXTERNAL_STORAGE)
class ScanKQR2Activity : BaseActivityVDB<ScankQr2ActivityBinding>() {

    companion object {
        const val SCANK2_ACTIVITY_RESULT_PARAM = "SCANK2_ACTIVITY_RESULT_PARAM"
    }

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it) {
                super.initData(savedInstanceState)
            } else {
                UtilKLaunchActivity.startSettingAppDetails(this)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initCamera()
    }

    @OptIn(OPermission_CAMERA::class)
    private fun initCamera() {
        vdb.scankQr2Preview.apply {
            initCameraKX(this@ScanKQR2Activity)
            setCameraXFrameListener(_frameAnalyzer)
        }
    }

    private fun onScanResult(scanK2Result: ScanK2Result) {
        Log.d(TAG, "onScanResult: scanK2Result $scanK2Result")
        val intent = Intent()
        intent.putExtra(SCANK2_ACTIVITY_RESULT_PARAM, scanK2Result.t2strJsonGson())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private val _ratio by lazy {
        vdb.scankQr2Qrscan.getRectSize().toDouble() / UtilKScreen.getWidth_ofSysMetrics().toDouble()
    }
    private var _bitmap: Bitmap? = null
    private var _lastTime = System.currentTimeMillis()

    @OptIn(OFieldCall_Close::class)
    private val _frameAnalyzer = object : ICameraXKFrameListener {

        @SuppressLint("UnsafeOptInUsageError")
        override fun invoke(a: ImageProxy) {
            a.use { imageProxy ->
                if (System.currentTimeMillis() - _lastTime >= 2000) {
                    _bitmap = if (imageProxy.format == ImageFormat.YUV_420_888) {
                        imageProxy.imageProxyYuv4208882bitmapJpeg()
                    } else {
                        imageProxy.imageProxyJpeg2bitmapJpeg()
                    }.applyBitmapAnyRotate(90f).apply {
                        applyBitmapAnyCrop(
                                (_ratio * this.width).toInt(),
                                (_ratio * this.width).toInt(),
                                ((1 - _ratio) * this.width / 2).toInt(),
                                ((this.height - _ratio * this.width) / 2).toInt()
                        )
                    }

                    //detect
                    _bitmap?.let {
                        val result = ScanKCodeHms.instance.codeRecognition(it)
                        if (result != null) {
                            onScanResult(ScanK2Result(result, it.compressBitmapAnyScaled(50), vdb.scankQr2Qrscan.getRectSize()))
                        }
                    }

                    //////////////////////////////////////////////////
                    _lastTime = System.currentTimeMillis()
                }
            }
        }
    }
}