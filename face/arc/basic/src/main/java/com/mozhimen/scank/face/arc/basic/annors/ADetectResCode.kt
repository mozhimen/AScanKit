package com.mozhimen.scank.face.arc.basic.annors

/**
 * @ClassName AResultCode
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:14
 * @Version 1.0
 */
annotation class ADetectResCode {
    companion object {
        var RES_OK = 99//检测通过
        var RES_HACK = 98//Hack
        var RES_TIME_OUT = 104//超时
    }
}
