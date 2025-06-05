package com.mozhimen.scank.code.hms

import android.graphics.Bitmap
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.kotlin.utilk.bases.BaseUtilK

/**
 * @ClassName ScanKCodeHms
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/14 9:51
 * @Version 1.0
 */
class ScanKCodeHms : BaseUtilK() {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    ////////////////////////////////////////////////////////////////////

    private var _hmsScanAnalyzerOptions: HmsScanAnalyzerOptions = HmsScanAnalyzerOptions.Creator()
        .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
        .setPhotoMode(true)
        .create()

    fun init(options: HmsScanAnalyzerOptions) {
        _hmsScanAnalyzerOptions = options
    }

    fun codeRecognition(bitmap: Bitmap): HmsScan? {
        val results = ScanUtil.decodeWithBitmap(_context, bitmap, _hmsScanAnalyzerOptions)
        return if (results != null && results.isNotEmpty() && results.getOrNull(0) != null && results[0]!!.originalValue.isNotEmpty())
            results[0]
        else null
    }

    ////////////////////////////////////////////////////////////////////

    private object INSTANCE {
        val holder = ScanKCodeHms()
    }
}