package com.mozhimen.scank.face.arc.basic.mos

/**
 * @ClassName ParamConfig
 * @Description Binocular engine threshold config about the quality, liveness and identify.
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:34
 * @Version 1.0
 */
data class ParamConfig(
    var rgb_liveness_threshold: Float = 0.5f,//rgb防伪阈值
    var ir_liveness_threshold: Float = 0.4f,//ir防伪阈值
    var min_roll: Float = -30.0f,//围绕Z轴旋转角度（翻滚角）的下限
    var max_roll: Float = 30.0f, ///< 围绕Z轴旋转角度（翻滚角）的上限
    var min_yaw: Float = -30.0f,///< 围绕Y轴旋转角度（偏航角）的下限
    var max_yaw: Float = 30.0f, ///< 围绕Y轴旋转角度（偏航角）的上限
    var min_pitch: Float = -30.0f, ///< 围绕X轴旋转角度（俯仰角）的下限
    var max_pitch: Float = 30.0f,///< 围绕X轴旋转角度（俯仰角）的上限
    var blur_threshold: Float = 0.3f,///< 模糊度阈值
    var cheek_occlusion_threshold: Float = 0.5f, ///< 面部遮挡阈值
    var mouth_occlusion_threshold: Float = 0.5f, ///< 嘴部遮挡阈值
    var nose_occlusion_threshold: Float = 0.5f, ///< 鼻子遮挡阈值
    var left_eye_occlusion_threshold: Float = 0.5f,///< 左眼遮挡阈值
    var right_eye_occlusion_threshold: Float = 0.5f, ///< 右眼遮挡阈值
    var left_eye_open_threshold: Float = 0.5f, ///< 左眼睁开阈值
    var right_eye_open_threshold: Float = 0.5f,///< 右眼睁开阈值
    var mouth_open_threshold: Float = 0.5f, ///< 张嘴阈值
    var over_dark_threshold: Float = 0.1f,///< 过暗阈值
    var over_glare_threshold: Float = 0.1f,///< 过亮阈值
    var verifyScore: Float = 0.9f // 比对阀值
)