package com.mozhimen.scank_hyperlpr_platenum_test

import android.graphics.Bitmap
import android.os.Bundle
import com.mozhimen.basick.elemk.androidx.appcompat.bases.BaseActivityVB
import com.mozhimen.basick.imagek.coil.loadImageCircleCoil
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.componentk.cameraxk.commons.ICameraXKCaptureListener
import com.mozhimen.componentk.cameraxk.mos.MCameraXKConfig
import com.hyperai.hyperlpr3.HyperLPR3
import com.hyperai.hyperlpr3.bean.HyperLPRParameter
import com.mozhimen.basick.utilk.android.util.it
import com.mozhimen.basick.utilk.kotlin.joinArray2Str
import com.mozhimen.scank_plate_hyperlpr.ScanKHyperLpr
import com.mozhimen.scank_hyperlpr_platenum_test.databinding.ActivityMainBinding

@APermissionCheck(
    CPermission.WRITE_EXTERNAL_STORAGE,
    CPermission.READ_EXTERNAL_STORAGE,
    CPermission.CAMERA
)
class MainActivity : BaseActivityVB<ActivityMainBinding>() {

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it)
                super.initData(savedInstanceState)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 车牌识别算法配置参数
        val parameter = HyperLPRParameter()
        // 初始化(仅执行一次生效)
        ScanKHyperLpr.instance.init(parameter)

        vb.camera.apply {
            initCamera(this@MainActivity, MCameraXKConfig())
            startCamera()
            setCameraXKCaptureListener(object : ICameraXKCaptureListener {
                override fun onCaptureFail() {

                }

                override fun onCaptureSuccess(bitmap: Bitmap, imageRotation: Int) {
                    vb.img.loadImageCircleCoil(bitmap)
                    val plates = ScanKHyperLpr.instance.plateRecognition(bitmap, HyperLPR3.CAMERA_ROTATION_0, HyperLPR3.STREAM_RGBA)
                    var res = ""
                    for (plate in plates) {
                        var type = "未知车牌"
                        if (plate.type != HyperLPR3.PLATE_TYPE_UNKNOWN) {
                            type = HyperLPR3.PLATE_TYPE_MAPS[plate.type]
                        }
                        val pStr = "[" + type + "]" + plate.code + "\n"
                        res += pStr
                    }
                    plates.joinArray2Str().it(TAG)
                    vb.txtRes.text = res
                }
            })
        }

        vb.btnDetect.setOnClickListener {
            vb.camera.takePicture()
        }
    }
}