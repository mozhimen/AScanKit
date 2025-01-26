package com.mozhimen.scank.face.arc42.basic.mos

import android.graphics.Bitmap
import android.graphics.Rect
import com.arcsoft.face.FaceInfo
import com.mozhimen.scank.face.arc.basic.mos.CompareInfo

/**
 * @ClassName BinocularResult
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:02
 * @Version 1.0
 */
data class LivenessResult(
    var trackId: Int,
    var orgBitmap: Bitmap?,
    var rect: Rect?,
    var livenessFaceInfo: CompareInfo? = null,
    var faceInfo: FaceInfo? = null,
)