package com.mozhimen.scank.face.arc41.basic.commons

import com.mozhimen.scank.face.arc.basic.commons.IBinocularDetectListener
import com.mozhimen.scank.face.arc.basic.commons.IDetectConfig

/**
 * @ClassName AbstractAdapter
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:54
 * @Version 1.0
 */
abstract class BinocularLivenessCallback<LISTENER : com.mozhimen.scank.face.arc.basic.commons.IBinocularDetectListener, CONFIG : com.mozhimen.scank.face.arc.basic.commons.IDetectConfig> :
    IBinocularLivenessListener {
    abstract fun setConfig(config: CONFIG)//设置双目引擎配置文件.

    abstract fun getConfig(): CONFIG?//获取双目引擎配置文件

    abstract fun addListener(identifyCallback: LISTENER?)//添加识别回调.Binocular Face Identify callback result for every phase.

    abstract fun removeListener(identifyCallback: LISTENER?)//移除识别回调

    abstract fun removeListeners()//移除识别回调

    fun onDestroy() {}//销毁
}
