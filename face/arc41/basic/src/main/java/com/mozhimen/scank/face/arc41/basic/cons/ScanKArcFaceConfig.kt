package com.mozhimen.scank.face.arc41.basic.cons

import com.arcsoft.face.enums.DetectFaceOrientPriority
import com.mozhimen.cachek.sharedpreferences.CacheKSP

/**
 * @ClassName ScanKArcFaceConfig
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2022/12/12 16:49
 * @Version 1.0
 */
object ScanKArcFaceConfig {
    private val _scanPro = CacheKSP.instance.with("sp_scank_arc_face")

    /**
     * IR预览数据相对于RGB预览数据的横向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    const val HORIZONTAL_OFFSET = 0

    /**
     * IR预览数据相对于RGB预览数据的纵向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    const val VERTICAL_OFFSET = 0

    /**
     * 入库状态:  0: 特征值提取成功  1: 特征值提取失败
     */
    const val FEATURE_SUCCESS = 0
    const val FEATURE_ERROR = 1

    // DEFAULT VALUES
    const val UP_PITCH_DEFAULT = -15f
    const val DOWN_PITCH_DEFAULT = 15f
    const val RIGHT_ROLL_DEFAULT = 20f
    const val LEFT_ROLL_DEFAULT = -20f
    const val RIGHT_YAW_DEFAULT = 15f
    const val LEFT_YAW_DEFAULT = -15f

    const val LIVENESS_SCORE_DEFAULT = 0.98f
    const val VERIFY_SCORE_DEFAULT = 0.9f
    const val BLUR_DEFAULT = 0.3f
    const val OCCLUSION_LEFTCHEEK = 0f
    const val OCCLUSION_RIGHTCHEEK = 0f
    const val OCCLUSION_LEFTEYE = 0f
    const val OCCLUSION_RIGHTEYE = 0f
    const val OCCLUSION_NOSE = 0f
    const val OCCLUSION_MOUTH = 0f

    const val FACE_SIZE_DEFAULT = 180f    //最小人脸框大小
    const val FACE_MARGIN_PIXEL_DEFAULT = 60    //人脸距边有效像素距离
    const val EXPAND_PIXEL_DEFAULT = 100    //裁剪外扩像素
    const val EXPAND_PIXEL_SHOW_DEFAULT = 50
    const val LICENSE_VERSION_DEFAULT = "1.0.0"
    const val DEFAULT_FACE_ORI = 1

    const val NUM_MAX_DETECT = 10    //最大检测数
    const val NUM_RETRY_MAX = 3        //出错重试最大次数

    const val INTERVAL_RETRY_NORMAL: Long = 500
    const val INTERVAL_RETRY_SUCCESS_: Long = 1000        //失败重试间隔时间（ms）
    const val INTERVAL_RETRY_FAIL: Long = 3000//失败重试间隔时间（ms）

    const val REGISTER_STATUS_READY = 0        //注册人脸状态码，准备注册
    const val REGISTER_STATUS_PROCESSING = 1        //注册人脸状态码，注册中
    const val REGISTER_STATUS_DONE = 2        //注册人脸状态码，注册结束（无论成功失败）

    const val THRESHOLD_SIMILAR = 0.8f    //识别阈值
    const val THRESHOLD_VERIFY_SCORE_DEFAULT = 0.9f //相似度阈值

    //所需的动态库文件
    val LIBRARIES = arrayOf(
        "libarcsoft_face_engine.so",  // 人脸相关
        "libarcsoft_face.so",
        "libarcsoft_image_util.so"
    )

    var trackedFaceCount: Int
        get() = _scanPro.getInt("trackedFaceCount", 0)
        set(value) {
            _scanPro.putInt("trackedFaceCount", value)
        }
    var ftOrientation: DetectFaceOrientPriority
        get() = DetectFaceOrientPriority.valueOf(_scanPro.getString("ftOrientation", DetectFaceOrientPriority.ASF_OP_90_ONLY.name)!!)
        set(value) {
            _scanPro.putString("ftOrientation", value.name)
        }
}