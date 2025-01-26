package com.mozhimen.scank.face.arc.basic.mos

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * @ClassName UnionData
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 22:49
 * @Version 1.0
 */
data class DetectResult(
    var trackId: Int,
    var orgBitmap: Bitmap?,
    var rect: Rect?,
    var detectFaceInfo: CompareInfo? = null
)