package com.mozhimen.scank.opencv.test

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.scank.edge.opencv.activity.ScanActivity
import com.mozhimen.scank.opencv.edge.constants.ScanConstants
import com.mozhimen.scank.opencv.edge.util.ScanUtils

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE = 101
    }

    private var scannedImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scannedImageView = findViewById(R.id.scanned_image)
        startScan()
    }

    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (null != data && null != data.extras) {
                    val filePath = data.extras!!.getString(ScanConstants.SCANNED_RESULT)
                    val baseBitmap: Bitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                    scannedImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
                    scannedImageView!!.setImageBitmap(baseBitmap)
                }
            } else if (resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }
}