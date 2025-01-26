package com.mozhimen.scank.face.arc42.basic.helpers;

import android.util.Log;
import android.util.Size;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.ExtractType;
import com.mozhimen.scank.face.arc42.basic.mos.FacePreviewInfo;
import com.mozhimen.scank.face.arc42.basic.commons.IDetectListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName FaceHelper
 * @Description 人脸操作辅助类
 * @Author Kolin Zhao / Mozhimen
 * @Version 1.0
 */
public class FaceHelper {
    //region #常量
    private static final String TAG = "FaceHelper>>>>>";

    //线程池正在处理任务
    private static final int ERROR_BUSY = -1;

    //特征提取引擎为空
    private static final int ERROR_FR_ENGINE_IS_NULL = -2;

    //活体检测引擎为空
    private static final int ERROR_FL_ENGINE_IS_NULL = -3;
    //endregion


    //人脸追踪引擎
    private FaceEngine faceTraceEngine;

    //特征提取引擎
    private FaceEngine faceFeatureEngine;

    //活体检测引擎
    //private FaceEngine faceLiveEngine;

    private IDetectListener faceListener;

    //上次应用退出时，记录的该App检测过的人脸数了
    private int detectedFaceCount = 0;

    private Size previewSize;

    //特征提取线程池
    private ExecutorService faceFeatureExecutor;

    //活体检测线程池
    //private ExecutorService faceLiveExecutor;

    //特征提取线程队列
    private LinkedBlockingQueue<Runnable> faceFeatureThreadQueue = null;

    //活体检测线程队列
    //private LinkedBlockingQueue<Runnable> faceLiveThreadQueue = null;

    private FaceHelper(Builder builder) {
        faceTraceEngine = builder.faceTraceEngine;
        faceFeatureEngine = builder.faceFeatureEngine;
        //faceLiveEngine = builder.faceLiveEngine;

        faceListener = builder.faceListener;
        detectedFaceCount = builder.detectedFaceCount;
        previewSize = builder.previewSize;


        //fr 线程队列大小
        int faceFeatureQueueSize = 5;
        if (builder.faceFeatureQueueSize > 0) {
            faceFeatureQueueSize = builder.faceFeatureQueueSize;
        } else {
            Log.e(TAG, "frThread num must > 0,now using default value:" + faceFeatureQueueSize);
        }
        faceFeatureThreadQueue = new LinkedBlockingQueue<>(faceFeatureQueueSize);
        faceFeatureExecutor = new ThreadPoolExecutor(1, faceFeatureQueueSize, 0, TimeUnit.MILLISECONDS, faceFeatureThreadQueue);


        //fl 线程队列大小
        /*int faceLiveQueueSize = 5;
        if (builder.faceLiveQueueSize > 0) {
            faceLiveQueueSize = builder.faceLiveQueueSize;
        } else {
            Log.e(TAG, "flThread num must > 0,now using default value:" + faceLiveQueueSize);
        }
        faceLiveThreadQueue = new LinkedBlockingQueue<Runnable>(faceLiveQueueSize);
        faceLiveExecutor = new ThreadPoolExecutor(1, faceLiveQueueSize, 0, TimeUnit.MILLISECONDS, faceLiveThreadQueue);
        if (previewSize == null) {
            throw new RuntimeException("previewSize must be specified!");
        }*/
    }

    /**
     * 请求获取人脸特征数据
     *
     * @param nv21     图像数据
     * @param faceInfo 人脸信息
     * @param width    图像宽度
     * @param height   图像高度
     * @param format   图像格式
     * @param trackId  请求人脸特征的唯一请求码，一般使用trackId
     */
    public void requestFaceFeature(byte[] nv21, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
        if (faceListener != null) {
            if (faceFeatureEngine != null /*&& faceFeatureThreadQueue.remainingCapacity() > 0*/) {
                Log.i(TAG, "人脸特征引擎");
                FaceRecognize faceRecognize = new FaceRecognize(nv21, faceInfo, width, height, format, trackId);
                faceRecognize.getFaceFeature();
            } else {
                faceListener.onFaceFeatureInfoGet(nv21, faceInfo, null, trackId, ERROR_BUSY);
            }
        }
    }

    /**
     * 请求获取活体检测结果，需要传入活体的参数，以下参数同
     *
     * @param nv21         NV21格式的图像数据
     * @param faceInfo     人脸信息
     * @param width        图像宽度
     * @param height       图像高度
     * @param format       图像格式
     * @param trackId      请求人脸特征的唯一请求码，一般使用trackId
     * @param livenessType 活体检测类型
     */
    /*public void requestFaceLiveness(byte[] nv21, FaceInfo faceInfo, int width, int height, int format, Integer trackId, LivenessType livenessType) {
        if (faceListener != null) {
            if (faceLiveEngine != null && faceLiveThreadQueue.remainingCapacity() > 0) {
                faceLiveExecutor.execute(new FaceLivenessDetectRunnable(nv21, faceInfo, width, height, format, trackId, livenessType));
            } else {
                faceListener.onFaceLivenessInfoGet(null, trackId, ERROR_BUSY);
            }
        }
    }*/

    private List<FaceInfo> faceInfoList = new ArrayList<>();

    /**
     * 释放对象
     */
    public void release() {
        /*if (!faceFeatureExecutor.isShutdown()) {
            faceFeatureExecutor.shutdownNow();
            faceFeatureThreadQueue.clear();
        }*/
        /*if (!faceLiveExecutor.isShutdown()) {
            faceLiveExecutor.shutdownNow();
            faceLiveThreadQueue.clear();
        }*/
        if (faceInfoList != null) {
            faceInfoList.clear();
        }
        /*if (faceFeatureThreadQueue != null) {
            faceFeatureThreadQueue.clear();
            faceFeatureThreadQueue = null;
        }*/
        /*if (faceLiveThreadQueue != null) {
            faceLiveThreadQueue.clear();
            faceLiveThreadQueue = null;
        }*/
        if (nameMap != null) {
            nameMap.clear();
        }
        nameMap = null;
        faceListener = null;
        faceInfoList = null;
    }

    private List<Integer> currentTrackIdList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();

    /**
     * 处理帧数据
     *
     * @param nv21 相机预览回传的NV21数据
     * @return 实时人脸处理结果，封装添加了一个trackId，trackId的获取依赖于faceId，用于记录人脸序号并保存
     */
    public List<FacePreviewInfo> onPreviewFrame(byte[] nv21) {
        final byte[] byteData = nv21.clone();
        if (faceListener != null) {
            if (faceTraceEngine != null) {
                faceInfoList.clear();
                long ftStartTime = System.currentTimeMillis();
                int code = faceTraceEngine.detectFaces(byteData, 1280, 720, FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code != ErrorInfo.MOK) {
                    faceListener.onFail(new Exception("ft failed,code is " + code));
                } else {
//                    Log.i(TAG, "onPreviewFrame: ft costTime = " + (System.currentTimeMillis() - ftStartTime) + "ms");
                }
                /*
                 * 若需要多人脸搜索，删除此行代码
                 */
                ScanKArcFaceUtil.keepMaxFace(faceInfoList);
                refreshTrackId(faceInfoList);
            }
            facePreviewInfoList.clear();
            for (int i = 0; i < faceInfoList.size(); i++) {
                facePreviewInfoList.add(new FacePreviewInfo(faceInfoList.get(i), currentTrackIdList.get(i)));
            }

            return facePreviewInfoList;
        } else {
            facePreviewInfoList.clear();
            return facePreviewInfoList;
        }
    }

    /**
     * 人脸特征提取线程
     */
    public class FaceRecognize {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;

        private FaceRecognize(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo);
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = trackId;
        }

        public void getFaceFeature() {
            if (faceListener != null && nv21Data != null) {
//                Bitmap bimatp = FileUtil.nv21ToBitmap(nv21Data,width,height);
                if (faceFeatureEngine != null) {

                    FaceFeature faceFeature = new FaceFeature();
                    long frStartTime = System.currentTimeMillis();
                    int frCode;
                    synchronized (faceFeatureEngine) {
                        frCode = faceFeatureEngine.extractFaceFeature(nv21Data, width, height, format, faceInfo, ExtractType.RECOGNIZE, MaskInfo.NOT_WORN, faceFeature);
                    }
                    if (frCode == ErrorInfo.MOK) {
                        Log.i(TAG, "run: fr costTime = " + (System.currentTimeMillis() - frStartTime) + "ms");
                        faceListener.onFaceFeatureInfoGet(nv21Data, faceInfo, faceFeature, trackId, frCode);
                    } else {
                        faceListener.onFaceFeatureInfoGet(nv21Data, faceInfo, null, trackId, frCode);
                        faceListener.onFail(new Exception("fr failed errorCode is " + frCode));
                    }
                } else {
                    faceListener.onFaceFeatureInfoGet(nv21Data, faceInfo, null, trackId, ERROR_FR_ENGINE_IS_NULL);
                    faceListener.onFail(new Exception("fr failed ,frEngine is null"));
                }
            }
            nv21Data = null;
        }
    }

    /**
     * 活体检测的线程
     */
    /*public class FaceLivenessDetectRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;
        private LivenessType livenessType;

        private FaceLivenessDetectRunnable(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format, Integer trackId, LivenessType livenessType) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo);
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = trackId;
            this.livenessType = livenessType;
        }

        @Override
        public void run() {
            if (faceListener != null && nv21Data != null) {
                //Bitmap bimatp = FileUtil.nv21ToBitmap(nv21Data,width,height);
//                if (faceInfo.getRect().bottom - faceInfo.getRect().top < 500 ||
//                        faceInfo.getRect().right - faceInfo.getRect().left < 500) {
//                    faceListener.onFaceLivenessInfoGet(null, trackId, ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL);
//                    faceListener.onFail(new Exception("fl failed errorCode is face width min is" + ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL));
//                } else
                if (faceLiveEngine != null) {
                    List<LivenessInfo> livenessInfoList = new ArrayList<>();
                    int flCode;
                    synchronized (faceLiveEngine) {
                        if (livenessType == LivenessType.RGB) {
                            flCode = faceLiveEngine.process(nv21Data, width, height, format, Arrays.asList(faceInfo), FaceEngine.ASF_LIVENESS);
                        } else {
                            flCode = faceLiveEngine.processIr(nv21Data, width, height, format, Arrays.asList(faceInfo), FaceEngine.ASF_IR_LIVENESS);
                        }
                    }
                    if (flCode == ErrorInfo.MOK) {
                        if (livenessType == LivenessType.RGB) {
                            flCode = faceLiveEngine.getLiveness(livenessInfoList);
                        } else {
                            flCode = faceLiveEngine.getIrLiveness(livenessInfoList);
                        }
                    }

                    if (flCode == ErrorInfo.MOK && livenessInfoList.size() > 0) {
                        faceListener.onFaceLivenessInfoGet(livenessInfoList.get(0), trackId, flCode);
                    } else {
                        faceListener.onFaceLivenessInfoGet(null, trackId, flCode);
                        faceListener.onFail(new Exception("fl failed errorCode is " + flCode));
                    }
                } else {
                    faceListener.onFaceLivenessInfoGet(null, trackId, ERROR_FL_ENGINE_IS_NULL);
                    faceListener.onFail(new Exception("fl failed ,frEngine is null"));
                }
            }
            nv21Data = null;
        }
    }*/

    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
    private void refreshTrackId(List<FaceInfo> ftFaceList) {
        currentTrackIdList.clear();

        for (FaceInfo faceInfo : ftFaceList) {
            currentTrackIdList.add(faceInfo.getFaceId() + detectedFaceCount);
        }
        if (ftFaceList.size() > 0) {
            currentMaxFaceId = ftFaceList.get(ftFaceList.size() - 1).getFaceId();
        }

        //刷新nameMap
        clearLastName(currentTrackIdList);
    }

    //本次打开引擎后的最大faceId
    private int currentMaxFaceId = 0;

    /**
     * 获取当前的最大trackID,可用于退出时保存
     *
     * @return 当前trackId
     */
    public int getTrackedFaceCount() {
        // 引擎的人脸下标从0开始，因此需要+1
        return detectedFaceCount + currentMaxFaceId + 1;
    }

    //用于存储人脸对应的姓名，KEY为trackId，VALUE为name
    private ConcurrentHashMap<Integer, String> nameMap = new ConcurrentHashMap<>();

    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    public void setName(int trackId, String name) {
        if (nameMap != null) {
            nameMap.put(trackId, name);
        }
    }

    public String getName(int trackId) {
        return nameMap == null ? null : nameMap.get(trackId);
    }

    /**
     * 清除map中已经离开的人脸
     *
     * @param trackIdList 最新的trackIdList
     */
    private void clearLastName(List<Integer> trackIdList) {
        Enumeration<Integer> keys = nameMap.keys();
        while (keys.hasMoreElements()) {
            int value = keys.nextElement();
            if (!trackIdList.contains(value)) {
                nameMap.remove(value);
            }
        }
    }

    public static final class Builder {
        private FaceEngine faceTraceEngine;
        private FaceEngine faceFeatureEngine;
        private Size previewSize;
        private int faceFeatureQueueSize;
        private IDetectListener faceListener;
        private int detectedFaceCount;

        public Builder() {
        }

        public Builder faceTraceEngine(FaceEngine val) {
            faceTraceEngine = val;
            return this;
        }

        public Builder faceFeatureEngine(FaceEngine val) {
            faceFeatureEngine = val;
            return this;
        }

        /*public Builder faceLiveEngine(FaceEngine val) {
            faceLiveEngine = val;
            return this;
        }*/

        public Builder previewSize(Size val) {
            previewSize = val;
            return this;
        }

        public Builder faceListener(IDetectListener val) {
            faceListener = val;
            return this;
        }

        public Builder faceFeatureQueueSize(int val) {
            faceFeatureQueueSize = val;
            return this;
        }

        /*public Builder faceLiveQueueSize(int val) {
            faceLiveQueueSize = val;
            return this;
        }*/

        public Builder detectedFaceCount(int val) {
            detectedFaceCount = val;
            return this;
        }

        public FaceHelper build() {
            return new FaceHelper(this);
        }
    }
}
