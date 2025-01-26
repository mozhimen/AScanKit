package com.mozhimen.scank.face.arc42.basic.commons

import com.arcsoft.face.FaceFeature
import com.arcsoft.face.FaceInfo
import com.arcsoft.face.LivenessInfo


/**
 * @ClassName IFaceListener
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:17
 * @Version 1.0
 */
interface IDetectListener {
    /**
     * 当出现异常时执行
     */
    fun onFail(e: Exception?)

    /**
     * 请求人脸特征后的回调
     */
    fun onFaceFeatureInfoGet(nv21: ByteArray, faceInfo: FaceInfo, faceFeature: FaceFeature?, trackId: Int, errorCode: Int)

    /**
     * 请求活体检测后的回调
     */
    fun onFaceLivenessInfoGet(livenessInfo: LivenessInfo?, trackId: Int?, errorCode: Int?)
}