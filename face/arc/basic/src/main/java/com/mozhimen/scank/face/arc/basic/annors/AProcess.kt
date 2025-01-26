package com.mozhimen.scank.face.arc.basic.annors

/**
 * @ClassName ARequestFeatureStatus
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:09
 * @Version 1.0
 */
annotation class AProcess {
    companion object {
        const val IDLE = -1//默认状态
        const val INITED = -2//初始化
        const val DETECTING = 0//处理中
        const val RETRY = 2//待重试
        const val RES_SUCCEED = 1//识别成功
        const val RES_FAILED = 3//识别失败
        const val STOPED = 4
    }
}
