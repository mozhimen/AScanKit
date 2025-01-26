package com.mozhimen.scank.test

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import androidx.camera.core.ImageProxy
import com.mozhimen.componentk.camerak.camerax.annors.ACameraKXFacing
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.lintk.optin.OptInFieldCall_Close
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.utilk.android.view.UtilKScreen
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.manifestk.cons.CUseFeature
import com.mozhimen.basick.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.basick.utilk.android.graphics.applyBitmapAnyCrop
import com.mozhimen.basick.utilk.android.graphics.applyBitmapAnyRotate
import com.mozhimen.basick.utilk.android.graphics.applyBitmapAnyScaleRatio
import com.mozhimen.componentk.camerak.camerax.commons.ICameraXKFrameListener
import com.mozhimen.componentk.camerak.camerax.helpers.jpegImageProxy2JpegBitmap
import com.mozhimen.componentk.camerak.camerax.helpers.yuv420888ImageProxy2JpegBitmap
import com.mozhimen.componentk.camerak.camerax.mos.MCameraKXConfig
import com.mozhimen.scank.hsv.ScanKHSV
import com.mozhimen.scank.test.databinding.ActivityScankHsvBinding

@AManifestKRequire(CPermission.CAMERA, CUseFeature.CAMERA, CUseFeature.CAMERA_AUTOFOCUS)
@APermissionCheck(CPermission.CAMERA)
class ScanKHSVActivity : BaseActivityVB<ActivityScankHsvBinding>() {

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it) {
                super.initData(savedInstanceState)
            } else {
                UtilKLaunchActivity.startSettingAppDetails(this)
            }
        }
    }

    @Throws(Exception::class)
    override fun initView(savedInstanceState: Bundle?) {
        initCamera()
    }

    private fun initCamera() {
        vb.scankHsvPreview.apply {
            initCameraKX(this@ScanKHSVActivity, MCameraKXConfig(facing = ACameraKXFacing.BACK))
            setCameraXFrameListener(_frameAnalyzer)
        }
    }

    private var _orgBitmap: Bitmap? = null
    private var _lastTime: Long = System.currentTimeMillis()
    private val _ratio: Double by lazy { vb.scankHsvQrscan.getRectSize().toDouble() / UtilKScreen.getWidthOfWindow().toDouble() }

    @OptIn(OptInFieldCall_Close::class)
    private val _frameAnalyzer: ICameraXKFrameListener by lazy {
        object : ICameraXKFrameListener {

            @SuppressLint("UnsafeOptInUsageError")
            override fun invoke(imageProxy: ImageProxy) {
                if (System.currentTimeMillis() - _lastTime >= 1000) {
                    _orgBitmap = if (imageProxy.format == ImageFormat.YUV_420_888) {
                        imageProxy.yuv420888ImageProxy2JpegBitmap()
                    } else {
                        imageProxy.jpegImageProxy2JpegBitmap()
                    }.applyBitmapAnyRotate(90f).let { rotate ->
                        rotate.applyBitmapAnyCrop(
                            (_ratio * rotate.width).toInt(),
                            (_ratio * rotate.width).toInt(),
                            ((1 - _ratio) * rotate.width / 2).toInt(),
                            ((rotate.height - _ratio * rotate.width) / 2).toInt()
                        ).let { crop ->
                            crop.applyBitmapAnyScaleRatio(crop.width / 5f, crop.height / 5f)//降低分辨率提高运算速度
                        }
                    }
                    val results = ScanKHSV.colorAnalyze(_orgBitmap!!)
                    Log.i(TAG, "analyze: $results")
                    _lastTime = System.currentTimeMillis()
                }

                imageProxy.close()
            }
        }
    }
}