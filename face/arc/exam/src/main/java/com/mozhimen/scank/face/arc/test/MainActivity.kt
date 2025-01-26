package com.mozhimen.scank.face.arc.test

import android.os.Bundle
import android.view.View
import com.mozhimen.bindk.bases.viewdatabinding.activity.BaseActivityVDB
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.scank.face.arc.test.databinding.ActivityMainBinding
import com.mozhimen.scank.face.arc.test.demo.DemoActivity

@APermissionCheck(CPermission.READ_EXTERNAL_STORAGE, CPermission.WRITE_EXTERNAL_STORAGE)
class MainActivity : BaseActivityVDB<ActivityMainBinding>() {

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it) super.initData(savedInstanceState)
        }
    }

    fun goGetDeviceInfo(view: View) {
        startContext<GetDeviceInfoActivity>()
    }

    fun goDemo(view: View) {
        startContext<DemoActivity>()
    }
}