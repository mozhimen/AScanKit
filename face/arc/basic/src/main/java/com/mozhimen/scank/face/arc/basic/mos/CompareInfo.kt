package com.mozhimen.scank.face.arc.basic.mos


/**
 * @ClassName CompareInfo
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Version 1.0
 */
data class CompareInfo(
    var trackId: Int,
    var personUuid: String?,
    var similar: Float,
)