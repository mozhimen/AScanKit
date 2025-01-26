package com.mozhimen.scank.face.arc.basic.annors

/**
 * @ClassName AErrorCode
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 21:50
 * @Version 1.0
 */
annotation class AInitErrorCode {
    companion object {
        const val ERR_ALREADY_INIT_SUCCESS = -3//已经初始化完成
        const val ERR_LIBRARY_LOADED_FAIL = 0//动态库加载失败，目前仅支持'armeabi-v7a','arm64-v8a'
        const val ERR_INVALID_ARGUMENTS = -2//配置参数异常
        const val ERR_CAPABILITY_DISABLED = -999//未授权该能力
        const val ERR_ENGINE_ACTIVE_FAIL = -1000
        const val ERR_ENGINE_INIT_FAIL = -1001
        const val ERR_ENGINE_IS_NULL = -1002
        const val ERR_ENGINE_BUSY = -1003
        const val ERR_ABIS_NOT_SUPPORTED = -1004

        fun getMessage(code: Int): String {
            return when (code) {
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_ALREADY_INIT_SUCCESS -> "已经初始化完成"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_LIBRARY_LOADED_FAIL -> "动态库加载失败"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_INVALID_ARGUMENTS -> "配置参数异常"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_CAPABILITY_DISABLED -> "未授权该能力"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_ENGINE_ACTIVE_FAIL -> "引擎激活失败"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_ENGINE_INIT_FAIL -> "引擎初始化失败"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_ENGINE_IS_NULL -> "引擎为空"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_ENGINE_BUSY -> "引擎忙碌"
                com.mozhimen.scank.face.arc.basic.annors.AInitErrorCode.Companion.ERR_ABIS_NOT_SUPPORTED -> "架构不支持"
                else -> "其他位置错误"
            }
        }
    }
}
