package com.mozhimen.scank.face.arc.basic.mos

/**
 * @ClassName FaceConfig
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/11 22:28
 * @Version 1.0
 */
data class EngineConfig(
    var deviceName: String = "",
    var appId: String = "",
    var sdkKey: String = "",
    var activeKey: String = "",
    var isActiveOffline: Boolean = false,
    var activeOfflineFilePath: String = ""
)