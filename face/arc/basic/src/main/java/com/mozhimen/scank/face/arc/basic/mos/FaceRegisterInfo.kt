package com.mozhimen.scank.face.arc.basic.mos

/**
 * @ClassName FaceRegisterInfo
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:18
 * @Version 1.0
 */
class FaceRegisterInfo {
    private var featureData: ByteArray? = null
    private var name: String = ""

    constructor(faceFeature: ByteArray, name: String) {
        featureData = faceFeature
        this.name = name
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getFeatureData(): ByteArray? {
        return featureData
    }

    fun setFeatureData(featureData: ByteArray) {
        this.featureData = featureData
    }

}