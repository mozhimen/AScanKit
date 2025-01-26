package com.mozhimen.scank.face.arc41.basic.helpers

import android.graphics.Rect
import com.mozhimen.scank.face.arc41.basic.commons.BinocularLivenessCallback
import com.mozhimen.scank.face.arc41.basic.mos.LivenessResult
import com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback
import com.mozhimen.scank.face.arc.basic.mos.CompareInfo
import com.mozhimen.scank.face.arc.basic.mos.DetectConfig
import com.mozhimen.scank.face.arc.basic.mos.DetectResult
import com.mozhimen.scank.face.arc.basic.mos.FaceLocation

/**
 * @ClassName DefaultAdapter
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:55
 * @Version 1.0
 */
class BinocularLivenessImpl : BinocularLivenessCallback<BinocularDetectCallback, DetectConfig>() {
    private val _binocularDetectCallbacks: MutableList<com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback> = ArrayList()
    private var _detectConfig: DetectConfig = DetectConfig()

    private val _binocularDetectCallback: com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback = object : com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback() {
        override fun onInitError(initErrorCode: Int) {
            for (callback in _binocularDetectCallbacks) {
                callback.onInitError(initErrorCode)
            }
        }

        override fun onFaceStatusChange(detectStatus: Int) {
            for (callback in _binocularDetectCallbacks) {
                callback.onFaceStatusChange(detectStatus)
            }
        }

        override fun onFaceLocationGet(faceLocation: FaceLocation?) {
            for (callback in _binocularDetectCallbacks) {
                callback.onFaceLocationGet(faceLocation)
            }
        }

        override fun onDetectResultGet(code: Int, detectResult: DetectResult) {
            for (callback in _binocularDetectCallbacks) {
                callback.onDetectResultGet(code, detectResult)
            }
        }
    }

    override fun setConfig(config: DetectConfig) {
        _detectConfig = config
    }

    override fun getConfig(): DetectConfig {
        return _detectConfig
    }

    override fun addListener(identifyCallback: com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback?) {
        if (identifyCallback != null) {
            _binocularDetectCallbacks.add(identifyCallback)
        }
    }

    override fun removeListener(identifyCallback: com.mozhimen.scank.face.arc.basic.commons.BinocularDetectCallback?) {
        if (identifyCallback != null) {
            _binocularDetectCallbacks.remove(identifyCallback)
        }
    }

    override fun removeListeners() {
        _binocularDetectCallbacks.clear()
    }

    override fun onInitError(errorCode: Int) {
        _binocularDetectCallback.onInitError(errorCode)
    }

    override fun onFaceStatusChange(detectStatus: Int) {
        _binocularDetectCallback.onFaceStatusChange(detectStatus)
    }

    override fun onFaceLocationGet(faceLocation: FaceLocation?) {
        _binocularDetectCallback.onFaceLocationGet(faceLocation)
    }

    override fun onLivenessResultGet(@com.mozhimen.scank.face.arc.basic.annors.ADetectResCode code: Int, livenessResult: LivenessResult) {
        if (livenessResult.livenessFaceInfo != null && livenessResult.faceInfo != null) {
            _binocularDetectCallback.onDetectResultGet(
                code,
                DetectResult(
                    livenessResult.trackId,
                    livenessResult.orgBitmap,
                    livenessResult.rect ?: Rect(
                        livenessResult.faceInfo!!.rect!!.left,
                        livenessResult.faceInfo!!.rect!!.top,
                        livenessResult.faceInfo!!.rect!!.right,
                        livenessResult.faceInfo!!.rect!!.bottom
                    ),
                    CompareInfo(
                        livenessResult.trackId,
                        livenessResult.livenessFaceInfo!!.personUuid,
                        livenessResult.livenessFaceInfo!!.similar,
                    )
                )
            )
        } else if (livenessResult.faceInfo != null) {
            _binocularDetectCallback.onDetectResultGet(
                code,
                DetectResult(
                    livenessResult.trackId,
                    livenessResult.orgBitmap,
                    livenessResult.rect ?: Rect(
                        livenessResult.faceInfo!!.rect!!.left,
                        livenessResult.faceInfo!!.rect!!.top,
                        livenessResult.faceInfo!!.rect!!.right,
                        livenessResult.faceInfo!!.rect!!.bottom
                    ),
                    null
                )
            )
        } else {
            _binocularDetectCallback.onDetectResultGet(
                code,
                DetectResult(
                    livenessResult.trackId,
                    livenessResult.orgBitmap,
                    livenessResult.rect,
                    null
                )
            )
        }
    }
}
