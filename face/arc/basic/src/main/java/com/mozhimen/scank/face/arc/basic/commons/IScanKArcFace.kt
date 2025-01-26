package com.mozhimen.scank.face.arc.basic.commons

import com.mozhimen.scank.face.arc.basic.mos.EngineConfig
import com.mozhimen.scank.face.arc.basic.mos.ImageConfig
import com.mozhimen.scank.face.arc.basic.mos.ParamConfig

/**
 * @ClassName IFace
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/29 22:31
 * @Version 1.0
 */
interface IScanKArcFace<CONFIG : com.mozhimen.scank.face.arc.basic.commons.IDetectConfig, LISTENER : com.mozhimen.scank.face.arc.basic.commons.IBinocularDetectListener> {

    fun setEngineConfig(engineConfig: EngineConfig)//设置配置参数
    fun setImageConfig(imageConfig: ImageConfig)//设置图像参数
    fun setParamConfig(paramConfig: ParamConfig)//设置阀值参数

    fun init(initListener: com.mozhimen.scank.face.arc.basic.commons.IInitListener?)//init engine
    fun isInitSuccess(): Boolean

    fun input(imageBytes: ByteArray)//Input rgb and nir for nv21

    /**
     * Add Identify CallBack
     * Binocular Face Identify callback result for every phase.
     */
    fun addListener(listener: LISTENER)

    /**
     * Remove Identify CallBack
     */
    fun removeListener(listener: LISTENER)
    fun removeAllListeners()

    fun start()
    fun reStart()
    fun stop()
    fun release()

//    /**
//     * 删除全部缓存人脸库
//     */
//    fun deleteAllCache()

    //    /**
    //     * 比较人脸特征值
    //     *
    //     * @param f1 特征1
    //     * @param f2 特征2
    //     * @return isSuccess 是否比对通过
    //     */
    //    boolean compareFeature(Object f1, Object f2) throws FaceXException;
    //    /**
    //     * 提取人脸特征值
    //     *
    //     * @param image {@code STImage}
    //     * @return 特征值
    //     */
    //    byte[] extractFeature(Bitmap image, @ImageOrientation int orientation) throws FaceXException;
    //    void insert(String id, Bitmap bitmap);
    //
    //    FaceSearchResult search(Object feature);
}