package com.mozhimen.scank.face.arc.test

import com.mozhimen.kotlin.elemk.android.app.bases.BaseApplication
import com.mozhimen.kotlin.lintk.optins.OApiMultiDex_InApplication

//import com.mozhimen.underlayk.crashk.CrashKMgr

/**
 * @ClassName MainApplication
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/11 23:59
 * @Version 1.0
 */
@OptIn(OApiMultiDex_InApplication::class)
class MainApplication : BaseApplication() {
    @OptIn(OApiMultiDex_InApplication::class)
    override fun onCreate() {
        super.onCreate()

//        CrashKMgr.instance.init()
    }
}