package com.mozhimen.scank.face.arc.basic.commons

import com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode
import com.mozhimen.scank.face.arc.basic.annors.ADetectErrorStatus
import com.mozhimen.scank.face.arc.basic.annors.ADetectResCode
import com.mozhimen.scank.face.arc.basic.mos.DetectResult
import com.mozhimen.scank.face.arc.basic.mos.FaceLocation

/**
 * @ClassName IIdentifyCallback
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:23
 * @Version 1.0
 */
interface IBinocularDetectListener {
    fun onInitError(@com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode initErrorCode: Int)//Throw error if have error for identify process.
    fun onFaceStatusChange(@com.mozhimen.scank.face.arc.basic.annors.ADetectErrorStatus detectStatus: Int)//返回阶段性结果
    fun onFaceLocationGet(faceLocation: FaceLocation?)//返回双目识别人脸坐标
    fun onDetectResultGet(@com.mozhimen.scank.face.arc.basic.annors.ADetectResCode code: Int, detectResult: DetectResult)//返回活体结果
}