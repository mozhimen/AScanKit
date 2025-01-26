package com.mozhimen.scank.face.arc.basic.mos

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * @ClassName FaceData
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 22:56
 * @Version 1.0
 */
data class FaceInfo(
    var trackId: Int,
    var rect: Rect?,
    var orgBitmap: Bitmap?
)