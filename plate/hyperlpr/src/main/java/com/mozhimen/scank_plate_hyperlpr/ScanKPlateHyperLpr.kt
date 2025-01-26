package com.mozhimen.scank_plate_hyperlpr

import android.graphics.Bitmap
import com.hyperai.hyperlpr3.HyperLPR3
import com.hyperai.hyperlpr3.bean.HyperLPRParameter
import com.hyperai.hyperlpr3.bean.Plate
import com.mozhimen.basick.utilk.bases.BaseUtilK

/**
 * @ClassName ScanKHyperLpr
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/7/21 11:31
 * @Version 1.0
 */
class ScanKPlateHyperLpr : BaseUtilK() {
    companion object {
        val instance = INSTANCE.holder
    }

    ////////////////////////////////////////////////////////////////////

    fun init(parameter: HyperLPRParameter = HyperLPRParameter()) {
        HyperLPR3.getInstance().init(_context, parameter)
    }

    fun plateRecognition(bitmap: Bitmap, rotation: Int = HyperLPR3.CAMERA_ROTATION_0, format: Int = HyperLPR3.STREAM_RGBA): Array<Plate> =
        HyperLPR3.getInstance().plateRecognition(bitmap, rotation, format)

    ////////////////////////////////////////////////////////////////////

    private object INSTANCE {
        val holder = ScanKPlateHyperLpr()
    }
}