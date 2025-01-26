package com.mozhimen.scank.face.arc.basic.mos

import com.mozhimen.scank.face.arc.basic.commons.IDetectConfig

/**
 * @ClassName IdentifyConfig
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:28
 * @Version 1.0
 */
class DetectConfig : com.mozhimen.scank.face.arc.basic.commons.IDetectConfig {
    private var _engineConfig: EngineConfig = EngineConfig()
    private var _imageConfig: ImageConfig = ImageConfig()
    private var _paramConfig: ParamConfig = ParamConfig()
    override fun getEngineConfig(): EngineConfig {
        return _engineConfig
    }

    override fun getImageConfig(): ImageConfig {
        return _imageConfig
    }

    override fun getParamConfig(): ParamConfig {
        return _paramConfig
    }

    fun setImageConfig(imageConfig: ImageConfig) {
        _imageConfig = imageConfig
    }

    fun setParamConfig(paramConfig: ParamConfig) {
        _paramConfig = paramConfig
    }

    fun setEngineConfig(engineConfig: EngineConfig) {
        _engineConfig = engineConfig
    }
}