package com.mozhimen.scank.face.arc.basic.annors

/**
 * @ClassName AFaceStatus
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:03
 * @Version 1.0
 */
annotation class ADetectErrorStatus {
    companion object {
        var FAIL_LIVENESS = 2// 活体检查失败
        var FAIL_FACE_FAR = 3// 距离太远
        var FAIL_OCCLUSION = 4// 脸部泽当
        var FAIL_HEAD_POSE = 5// 脸部姿态不正
        var FAIL_QUALITY = 6// 人脸质量不足
    }
}
