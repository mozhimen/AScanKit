package com.mozhimen.scank.face.arc.test

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.arcsoft.face.ErrorInfo
import com.arcsoft.face.FaceEngine
import com.arcsoft.face.model.ActiveDeviceInfo
import com.mozhimen.bindk.bases.viewdatabinding.activity.BaseActivityVDB
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.kotlin.utilk.android.app.UtilKPermission
import com.mozhimen.kotlin.utilk.kotlin.UtilKStringFormat
import com.mozhimen.scank.face.arc.test.databinding.ActivityGetDeviceInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @ClassName GenDeviceActivity
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/27 22:06
 * @Version 1.0
 */
@APermissionCheck(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.READ_PHONE_STATE
)
class GetDeviceInfoActivity : BaseActivityVDB<ActivityGetDeviceInfoBinding>() {
    private val DEVICE_INFO_NAME = "deviceInfo.txt"
    private val _deviceInfoSavePathWithName by lazy { "${this.filesDir.absolutePath}/scank_arc_face/" + DEVICE_INFO_NAME }

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            super.initData(savedInstanceState)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        vdb.btnDeviceInfo.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                createDeviceInfoFile()
            }
        }
    }

    private suspend fun createDeviceInfoFile() {
        if (UtilKPermission.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            val activeDeviceInfo = ActiveDeviceInfo()
            val code = FaceEngine.getActiveDeviceInfo(this, activeDeviceInfo)
            if (code == ErrorInfo.MOK) {
                withContext(Dispatchers.Main) { vdb.txtDeviceInfo.text = activeDeviceInfo.deviceInfo }
                UtilKStringFormat.str2file(activeDeviceInfo.deviceInfo, _deviceInfoSavePathWithName)
            } else {
                Log.e(TAG, "createDeviceInfoFile failed, code is $code")
            }
        } else {
            ManifestKPermission.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}