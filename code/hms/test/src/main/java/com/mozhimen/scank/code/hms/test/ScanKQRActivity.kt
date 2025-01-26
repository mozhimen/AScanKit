package com.mozhimen.scank.code.hms.test

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.widget.FrameLayout
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVDB
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.basick.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.basick.utilk.android.view.UtilKScreen
import com.mozhimen.basick.utilk.google.gson.t2strJsonGson
import com.mozhimen.scank.code.hms.test.databinding.ScankQrActivityBinding

@APermissionCheck(CPermission.CAMERA, CPermission.READ_EXTERNAL_STORAGE)
class ScanKQRActivity : BaseActivityVDB<ScankQrActivityBinding>() {
    companion object {
        const val SCANK_ACTIVITY_RESULT_PARAM = "SCANK_ACTIVITY_RESULT_PARAM"
    }

    private var _remoteView: RemoteView? = null
    private val _detectRect = Rect()

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it) {
                super.initData(savedInstanceState)
            } else {
                UtilKLaunchActivity.startSettingAppDetails(this)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRect()
        initRemoteView(savedInstanceState)
    }

    private fun initRemoteView(savedInstanceState: Bundle?) {
        _remoteView = RemoteView.Builder()
            .setContext(this)
            .setBoundingBox(_detectRect)
            .setFormat(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE)
            .build()
        _remoteView!!.setOnResultCallback { results ->
            if (results != null && results.isNotEmpty() && results[0] != null && !TextUtils.isEmpty(results[0].originalValue)) {
                onScanResult(results[0])
            }
        }
        _remoteView!!.onCreate(savedInstanceState)
        vdb.scankQrContainer.addView(
            _remoteView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun initRect() {
        val screenWidth = UtilKScreen.getWidth_ofSysMetrics()
        val screenHeight = UtilKScreen.getHeight_ofSysMetrics()
        val rectSize = vdb.scankQrScan.getRectSize()

        _detectRect.apply {
            left = (screenWidth - rectSize) / 2
            right = (screenWidth + rectSize) / 2
            top = (screenHeight - rectSize) / 2
            bottom = (screenHeight + rectSize) / 2
        }
    }

    private fun onScanResult(hmsScan: HmsScan) {
        val intent = Intent()
        intent.putExtra(SCANK_ACTIVITY_RESULT_PARAM, hmsScan.t2strJsonGson())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        _remoteView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        _remoteView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        _remoteView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        _remoteView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _remoteView?.onDestroy()
    }
}