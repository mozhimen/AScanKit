package com.mozhimen.scank.text.mlkit

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy

/**
 * @ClassName TextRecognizerProxy
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/2/13 22:57
 * @Version 1.0
 */
@OApiInit_ByLazy
class TextRecognizerProxy {
    private val _textRecognizer: TextRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private var _textRecognizeListener: IA_Listener<Text?>? = null

    fun init(listener: IA_Listener<Text?>) {
        _textRecognizeListener = listener
    }

    fun textRecognize(bitmap: Bitmap, degree: Int) {
        val taskText: Task<Text> = _textRecognizer.process(InputImage.fromBitmap(bitmap, degree))
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // ...
                _textRecognizeListener?.invoke(visionText)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                _textRecognizeListener?.invoke(null)
            }
    }
}