package com.mozhimen.scank.face.arc.basic.commons

/**
 * @ClassName IInitListener
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:19
 * @Version 1.0
 */
interface IInitListener {
    /**
     * 双目初始化结果回调.
     *
     * @param isSuccess `true` if Binocular Init success, otherwise
     */
    fun onInitResult(isSuccess: Boolean, msg: String)
}