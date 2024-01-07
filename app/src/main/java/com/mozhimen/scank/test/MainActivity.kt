package com.mozhimen.scank.test

import android.view.View
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.utilk.android.content.startContext
import com.mozhimen.scank.test.databinding.ActivityMainBinding

class MainActivity : BaseActivityVB<ActivityMainBinding>() {

    fun goScanKHSV(view: View) {
        startContext<ScanKHSVActivity>()
    }

    fun goScanKFace(view: View) {
        startContext<ScanKFaceActivity>()
    }
}