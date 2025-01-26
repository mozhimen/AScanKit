package com.mozhimen.scank.code.hms.test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.huawei.hms.ml.scan.HmsScan
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVDB
import com.mozhimen.basick.utilk.google.gson.strJson2tGson
import com.mozhimen.scank.code.hms.test.databinding.ActivityMainBinding

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        val activityResultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hmsScan: HmsScan? = result.data?.getStringExtra(ScanKQRActivity.SCANK_ACTIVITY_RESULT_PARAM)?.strJson2tGson() ?: kotlin.run {
                    Log.e(TAG, "initView: loss scanActivity params")
                    null
                }
                hmsScan?.let {
                    vdb.scankDemoTxt.text = it.originalValue
                    vdb.scankDemoImg.setImageBitmap(it.originalBitmap)
                }
            }
        }
        val activityResultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanKQR2Result: ScanK2Result? =
                    result.data?.getStringExtra(ScanKQR2Activity.SCANK2_ACTIVITY_RESULT_PARAM)?.strJson2tGson<ScanK2Result>() ?: kotlin.run {
                        Log.e(TAG, "initView: loss scanActivity2 params")
                        null
                    }
                scanKQR2Result?.let {
                    vdb.scankDemoTxt.text = it.hmsScan.originalValue
                    vdb.scankDemoImg.setImageBitmap(it.bitmap)
                }
            }
        }

        vdb.scankDemoBtnScan.setOnClickListener {
            activityResultLauncher1.launch(Intent(this, ScanKQRActivity::class.java))
        }

        vdb.scankDemoBtnScan2.setOnClickListener {
            activityResultLauncher2.launch(Intent(this, ScanKQR2Activity::class.java))
        }
    }
}