package com.mozhimen.scank.face.arc42.basic.mos

import com.arcsoft.face.FaceInfo


/**
 * @ClassName FacePreviewInfo
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:17
 * @Version 1.0
 */
class FacePreviewInfo {
    private var faceInfo: FaceInfo? = null
    private var trackId = 0

    constructor(faceInfo: FaceInfo, trackId: Int) {
        this.faceInfo = faceInfo
        this.trackId = trackId
    }

    fun getFaceInfo(): FaceInfo? {
        return faceInfo
    }

    fun setFaceInfo(faceInfo: FaceInfo?) {
        this.faceInfo = faceInfo
    }


    fun getTrackId(): Int {
        return trackId
    }

    fun setTrackId(trackId: Int) {
        this.trackId = trackId
    }
}