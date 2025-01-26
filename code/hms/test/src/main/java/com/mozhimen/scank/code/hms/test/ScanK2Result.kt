package com.mozhimen.scank.code.hms.test

import android.graphics.Bitmap
import com.huawei.hms.ml.scan.HmsScan
import java.io.Serializable

/**
 * @ClassName ScanK2Result
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2023/8/7 22:27
 * @Version 1.0
 */
data class ScanK2Result(
    val hmsScan: HmsScan,
    val bitmap: Bitmap,
    val rectSize: Int
) : Serializable