package com.mozhimen.scank.face.arc.basic.commons

import com.mozhimen.scank.face.arc.basic.mos.EngineConfig
import com.mozhimen.scank.face.arc.basic.mos.ImageConfig
import com.mozhimen.scank.face.arc.basic.mos.ParamConfig

/**
 * @ClassName IIdentifyConfig
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:26
 * @Version 1.0
 */
interface IDetectConfig {
    fun getEngineConfig(): EngineConfig

    /**
     * 获取Image相关配置.
     * @return
     */
    fun getImageConfig(): ImageConfig

    /**
     * 获取阈值配置.
     * @return
     */
    fun getParamConfig(): ParamConfig
}