package com.mozhimen.scank.face.arc41.basic.commons

import com.mozhimen.scank.face.arc.basic.mos.FaceLocation
import com.mozhimen.scank.face.arc41.basic.mos.LivenessResult

/**
 * @ClassName IBinocularLivenessListener
 * @Description 双目活体
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:27
 * @Version 1.0
 */
interface IBinocularLivenessListener {
    fun onInitError(@com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode errorCode: Int)//初始化出现错误
    fun onFaceStatusChange(@com.mozhimen.scank.face.arc.basic.annors.ADetectErrorStatus detectStatus: Int)//更新对准状态
    fun onFaceLocationGet(faceLocation: FaceLocation?)//实时返回人脸实时位置（相对于input图片）
    fun onLivenessResultGet(@com.mozhimen.scank.face.arc.basic.annors.ADetectResCode code: Int, livenessResult: LivenessResult)//活体检测结束
}