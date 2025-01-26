package com.mozhimen.scank.face.arc.basic.mos

import com.mozhimen.scank.face.arc.basic.annors.AImageOrientation
import com.mozhimen.scank.face.arc.basic.annors.AImagePixelFormat

/**
 * @ClassName ImageConfig
 * @Description Image config for the binocular identify.
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:24
 * @Version 1.0
 */
data class ImageConfig(
    var previewW: Int = 1280,
    var previewH: Int = 720,
    var imagePixelFormat: Int = com.mozhimen.scank.face.arc.basic.annors.AImagePixelFormat.NV21,
    var imageOrientation: Int = com.mozhimen.scank.face.arc.basic.annors.AImageOrientation.RIGHT,
)