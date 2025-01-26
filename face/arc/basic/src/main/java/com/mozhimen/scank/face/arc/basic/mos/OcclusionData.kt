package com.mozhimen.scank.face.arc.basic.mos

import com.mozhimen.serialk.gson.UtilKGsonFormat
import com.mozhimen.serialk.gson.t2strJson_ofGson

/**
 * @ClassName OcclusionData
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:14
 * @Version 1.0
 */
class OcclusionData {
    private var leftCheek = 0f
    private var rightCheek = 0f
    private var leftEye = 0f
    private var rightEye = 0f
    private var nose = 0f
    private var mouth = 0f

    constructor(leftCheek: Float, rightCheek: Float, leftEye: Float, rightEye: Float, nose: Float, mouth: Float) {
        this.leftCheek = leftCheek
        this.rightCheek = rightCheek
        this.leftEye = leftEye
        this.rightEye = rightEye
        this.nose = nose
        this.mouth = mouth
    }

    fun getLeftCheek(): Float {
        return leftCheek
    }

    fun getRightCheek(): Float {
        return rightCheek
    }

    fun getLeftEye(): Float {
        return leftEye
    }

    fun getRightEye(): Float {
        return rightEye
    }

    fun getNose(): Float {
        return nose
    }

    fun getMouth(): Float {
        return mouth
    }

    fun getJson(): String {
        return UtilKGsonFormat.t2strJson_ofGson<OcclusionData>(this)//GsonUtils.toJson(this)
    }
}