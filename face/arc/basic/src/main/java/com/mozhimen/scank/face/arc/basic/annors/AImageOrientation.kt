package com.mozhimen.scank.face.arc.basic.annors

/**
 * @ClassName AImageOrientation
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:06
 * @Version 1.0
 */
annotation class AImageOrientation{
    companion object{
        var UP = 0 ///< 上, 如人脸向上
        var LEFT = 1 ///< 左, 如人脸向左, 即人脸被逆时针旋转了90度
        var DOWN = 2 ///< 下, 如人脸向下
        var RIGHT = 3 ///< 右, 如人脸向右，即人脸被逆时针旋转了270度
    }
}
