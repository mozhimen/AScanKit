package com.mozhimen.scank_plate_hyperlpr.helpers

import com.hyperai.hyperlpr3.HyperLPR3
import com.hyperai.hyperlpr3.bean.Plate
import java.lang.StringBuilder


/**
 * @ClassName ScanKHyperLprHelper
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/7/21 17:11
 * @Version 1.0
 */
fun Array<Plate>.plates2str(): String =
    ScanKPlateHyperLpr.plates2str(this)

object ScanKPlateHyperLpr {
    fun plates2str(plates: Array<Plate>): String =
        if (plates.isNotEmpty()) {
            val stringBuilder = StringBuilder()
            for (plate in plates) {
                var type = "未知车牌"
                if (plate.type != HyperLPR3.PLATE_TYPE_UNKNOWN) type = HyperLPR3.PLATE_TYPE_MAPS[plate.type]
                stringBuilder.append("[" + type + "]" + plate.code + "\n")
            }
            stringBuilder.toString()
        } else ""

}