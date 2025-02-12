package com.mozhimen.scank.face.arc42.test.util.debug.face;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mozhimen.scank.face.arc42.test.util.debug.DebugInfoCallback;
import com.mozhimen.scank.face.arc42.test.util.debug.model.DebugRecognizeInfo;
import com.mozhimen.scank.face.arc42.test.facedb.entity.FaceEntity;
import com.mozhimen.scank.face.arc42.test.faceserver.FaceServer;
import com.mozhimen.scank.face.arc42.test.ui.model.CompareResult;
import com.mozhimen.scank.face.arc42.test.util.FaceRectTransformer;
import com.mozhimen.scank.face.arc42.test.util.debug.DebugInfoDumper;
import com.mozhimen.scank.face.arc42.test.util.debug.DumpConfig;
import com.mozhimen.scank.face.arc42.test.util.face.IDualCameraFaceInfoTransformer;
import com.mozhimen.scank.face.arc42.test.util.face.RecognizeCallback;
import com.mozhimen.scank.face.arc42.test.util.face.constants.LivenessType;
import com.mozhimen.scank.face.arc42.test.util.face.constants.RequestFeatureStatus;
import com.mozhimen.scank.face.arc42.test.util.face.constants.RequestLivenessStatus;
import com.mozhimen.scank.face.arc42.test.util.face.facefilter.FaceMoveFilter;
import com.mozhimen.scank.face.arc42.test.util.face.facefilter.FaceRecognizeAreaFilter;
import com.mozhimen.scank.face.arc42.test.util.face.facefilter.FaceRecognizeFilter;
import com.mozhimen.scank.face.arc42.test.util.face.facefilter.FaceSizeFilter;
import com.mozhimen.scank.face.arc42.test.util.face.model.FacePreviewInfo;
import com.mozhimen.scank.face.arc42.test.util.face.model.RecognizeConfiguration;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.ImageQualitySimilar;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.ExtractType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 人脸操作辅助类(for debug)，代码和
 */
public class DebugFaceHelper implements DebugFaceListener {
    private static final String TAG = "FaceHelper";

    /**
     * 识别结果的回调
     */
    private RecognizeCallback recognizeCallback;


    /**
     * 用于记录人脸识别过程信息
     */
    private ConcurrentHashMap<Integer, DebugRecognizeInfo> recognizeInfoMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
    /**
     * 转换方式，用于IR活体检测
     */
    private IDualCameraFaceInfoTransformer dualCameraFaceInfoTransformer;

    /**
     * 线程池正在处理任务
     */
    private static final int ERROR_BUSY = -1;
    /**
     * 特征提取引擎为空
     */
    private static final int ERROR_FR_ENGINE_IS_NULL = -2;
    /**
     * 活体检测引擎为空
     */
    private static final int ERROR_FL_ENGINE_IS_NULL = -3;
    /**
     * 人脸追踪引擎
     */
    private FaceEngine ftEngine;
    /**
     * 特征提取引擎
     */
    private FaceEngine frEngine;
    /**
     * 活体检测引擎
     */
    private FaceEngine flEngine;

    private Camera.Size previewSize;

    private List<FaceInfo> faceInfoList = new CopyOnWriteArrayList<>();
    private List<MaskInfo> maskInfoList = new CopyOnWriteArrayList<>();
    /**
     * 特征提取线程池
     */
    private ExecutorService frExecutor;
    /**
     * 活体检测线程池
     */
    private ExecutorService flExecutor;
    /**
     * 特征提取线程队列
     */
    private LinkedBlockingQueue<Runnable> frThreadQueue;
    /**
     * 活体检测线程队列
     */
    private LinkedBlockingQueue<Runnable> flThreadQueue;

    private FaceRectTransformer rgbFaceRectTransformer;
    private FaceRectTransformer irFaceRectTransformer;
    /**
     * 控制可识别区域（相对于View），若未设置，则是全部区域
     */
    private Rect recognizeArea = new Rect(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private List<FaceRecognizeFilter> faceRecognizeFilterList = new ArrayList<>();
    /**
     * 上次应用退出时，记录的该App检测过的人脸数了
     */
    private int trackedFaceCount = 0;
    /**
     * 本次打开引擎后的最大faceId
     */
    private int currentMaxFaceId = 0;

    /**
     * 识别的配置项
     */
    private RecognizeConfiguration recognizeConfiguration;

    private List<Integer> currentTrackIdList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();

    private DebugInfoCallback errorCallback;
    private DumpConfig errorDumpConfig = new DumpConfig();
    private boolean needUpdateFaceData;
    private Disposable timerDisposable;

    public void setErrorDumpConfig(DumpConfig errorDumpConfig) {
        this.errorDumpConfig = errorDumpConfig;
    }

    public void setErrorCallback(DebugInfoCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    private DebugFaceHelper(Builder builder) {
        needUpdateFaceData = builder.needUpdateFaceData;
        ftEngine = builder.ftEngine;
        trackedFaceCount = builder.trackedFaceCount;
        previewSize = builder.previewSize;
        frEngine = builder.frEngine;
        flEngine = builder.flEngine;
        recognizeCallback = builder.recognizeCallback;
        recognizeConfiguration = builder.recognizeConfiguration;
        dualCameraFaceInfoTransformer = builder.dualCameraFaceInfoTransformer;

        /*
         * fr 线程队列大小
         */
        int frQueueSize = recognizeConfiguration.getMaxDetectFaces();
        if (builder.frQueueSize > 0) {
            frQueueSize = builder.frQueueSize;
        } else {
            Log.e(TAG, "frThread num must > 0, now using default value:" + frQueueSize);
        }
        frThreadQueue = new LinkedBlockingQueue<>(frQueueSize);
        frExecutor = new ThreadPoolExecutor(1, frQueueSize, 0, TimeUnit.MILLISECONDS, frThreadQueue, r -> {
            Thread t = new Thread(r);
            t.setName("frThread-" + t.getId());
            return t;
        });

        /*
         * fl 线程队列大小
         */
        int flQueueSize = recognizeConfiguration.getMaxDetectFaces();
        if (builder.flQueueSize > 0) {
            flQueueSize = builder.flQueueSize;
        } else {
            Log.e(TAG, "flThread num must > 0, now using default value:" + flQueueSize);
        }
        flThreadQueue = new LinkedBlockingQueue<>(flQueueSize);
        flExecutor = new ThreadPoolExecutor(1, flQueueSize, 0, TimeUnit.MILLISECONDS, flThreadQueue, r -> {
            Thread t = new Thread(r);
            t.setName("flThread-" + t.getId());
            return t;
        });
        if (previewSize == null) {
            throw new RuntimeException("previewSize must be specified!");
        }
        if (recognizeConfiguration.isEnableFaceSizeLimit()) {
            faceRecognizeFilterList.add(new FaceSizeFilter(recognizeConfiguration.getFaceSizeLimit(), recognizeConfiguration.getFaceSizeLimit()));
        }
        if (recognizeConfiguration.isEnableFaceMoveLimit()) {
            faceRecognizeFilterList.add(new FaceMoveFilter(recognizeConfiguration.getFaceMoveLimit()));
        }
        if (recognizeConfiguration.isEnableFaceAreaLimit()) {
            faceRecognizeFilterList.add(new FaceRecognizeAreaFilter(recognizeArea));
        }
    }

    /**
     * 请求获取人脸特征数据
     *
     * @param nv21            图像数据
     * @param facePreviewInfo 人脸信息
     * @param width           图像宽度
     * @param height          图像高度
     * @param format          图像格式
     */
    public void requestFaceFeature(byte[] nv21, FacePreviewInfo facePreviewInfo, int width, int height, int format) {
        if (frEngine != null && frThreadQueue.remainingCapacity() > 0) {
            frExecutor.execute(new FaceRecognizeRunnable(nv21, facePreviewInfo, width, height, format));
        } else {
            onFaceFeatureInfoGet(nv21, null, facePreviewInfo.getTrackId(), facePreviewInfo.getFaceInfoRgb(), -1, -1, ERROR_BUSY);
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
     * @param livenessType 活体检测类型
     * @param waitLock
     */
    public void requestFaceLiveness(byte[] nv21, FacePreviewInfo faceInfo, int width, int height, int format, LivenessType livenessType, Object waitLock) {
        if (flEngine != null && flThreadQueue.remainingCapacity() > 0) {
            flExecutor.execute(new FaceLivenessDetectRunnable(nv21, faceInfo, width, height, format, livenessType, waitLock));
        } else {
            onFaceLivenessInfoGet(nv21, null, faceInfo.getTrackId(), faceInfo.getFaceInfoRgb(), 0, ERROR_BUSY, livenessType);
        }
    }

    /**
     * 释放对象
     */
    public void release() {
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (!frExecutor.isShutdown()) {
            frExecutor.shutdownNow();
            frThreadQueue.clear();
        }
        if (!flExecutor.isShutdown()) {
            flExecutor.shutdownNow();
            flThreadQueue.clear();
        }
        if (faceInfoList != null) {
            faceInfoList.clear();
        }
        if (frThreadQueue != null) {
            frThreadQueue.clear();
            frThreadQueue = null;
        }
        if (flThreadQueue != null) {
            flThreadQueue.clear();
            flThreadQueue = null;
        }
        faceInfoList = null;
    }

    /**
     * 处理帧数据
     *
     * @param rgbNv21     可见光相机预览回传的NV21数据
     * @param irNv21      红外相机预览回传的NV21数据
     * @param doRecognize 是否进行识别
     * @return 实时人脸处理结果，封装添加了一个trackId，trackId的获取依赖于faceId，用于记录人脸序号并保存
     */
    public List<FacePreviewInfo> onPreviewFrame(@NonNull byte[] rgbNv21, @Nullable byte[] irNv21, boolean doRecognize) {
        if (ftEngine != null) {
            faceInfoList.clear();
            maskInfoList.clear();
            facePreviewInfoList.clear();
            long ftStartTime = System.currentTimeMillis();
            int code = ftEngine.detectFaces(rgbNv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
            long ftTime = System.currentTimeMillis() - ftStartTime;
            if (code != ErrorInfo.MOK) {
                onFail(new Exception("detectFaces failed,code is " + code));
                return facePreviewInfoList;
            }
            if (errorCallback != null && errorDumpConfig.isDumpFaceTrackError() && faceInfoList.isEmpty()) {
                errorCallback.onNormalErrorOccurred(DebugInfoDumper.ERROR_TYPE_FACE_TRACK, rgbNv21,
                        DebugInfoDumper.getFaceTrackErrorFileName(previewSize.width, previewSize.height, code));
            }

            if (recognizeConfiguration.isKeepMaxFace()) {
                keepMaxFace(faceInfoList);
            }
            refreshTrackId(faceInfoList);
            if (faceInfoList.isEmpty()) {
                return facePreviewInfoList;
            }
            long maskStartTime = System.currentTimeMillis();
            code = ftEngine.process(rgbNv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList,
                    FaceEngine.ASF_MASK_DETECT);
            long maskTime = System.currentTimeMillis() - maskStartTime;
            if (code == ErrorInfo.MOK) {
                code = ftEngine.getMask(maskInfoList);
                if (code != ErrorInfo.MOK) {
                    onFail(new Exception("getMask failed,code is " + code));
                    return facePreviewInfoList;
                }
            } else {
                onFail(new Exception("process mask failed,code is " + code));
                return facePreviewInfoList;
            }
            for (int i = 0; i < faceInfoList.size(); i++) {
                FacePreviewInfo facePreviewInfo = new FacePreviewInfo(faceInfoList.get(i), currentTrackIdList.get(i));
                if (!maskInfoList.isEmpty()) {
                    MaskInfo maskInfo = maskInfoList.get(i);
                    facePreviewInfo.setMask(maskInfo.getMask());
                }
                if (rgbFaceRectTransformer != null && recognizeArea != null) {
                    Rect rect = rgbFaceRectTransformer.adjustRect(faceInfoList.get(i).getRect());
                    facePreviewInfo.setRgbTransformedRect(rect);
                }
                if (irFaceRectTransformer != null) {
                    FaceInfo faceInfo = faceInfoList.get(i);
                    if (dualCameraFaceInfoTransformer != null) {
                        faceInfo = dualCameraFaceInfoTransformer.transformFaceInfo(faceInfo);
                    }
                    facePreviewInfo.setFaceInfoIr(faceInfo);
                    facePreviewInfo.setIrTransformedRect(irFaceRectTransformer.adjustRect(faceInfo.getRect()));
                }
                facePreviewInfoList.add(facePreviewInfo);
            }
            clearLeftFace(facePreviewInfoList);
            if (doRecognize) {
                doRecognize(rgbNv21, irNv21, facePreviewInfoList, ftTime, maskTime);
            }
        } else {
            facePreviewInfoList.clear();
        }
        return facePreviewInfoList;
    }

    public void setRgbFaceRectTransformer(FaceRectTransformer rgbFaceRectTransformer) {
        this.rgbFaceRectTransformer = rgbFaceRectTransformer;
    }

    public void setIrFaceRectTransformer(FaceRectTransformer irFaceRectTransformer) {
        this.irFaceRectTransformer = irFaceRectTransformer;
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
        }
        Enumeration<Integer> keys = recognizeInfoMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                DebugRecognizeInfo recognizeInfo = recognizeInfoMap.remove(key);
                if (recognizeInfo != null) {
                    recognizeCallback.onNoticeChanged("");
                    // 人脸离开时，通知特征提取线程，避免一直等待活体结果
                    synchronized (recognizeInfo.getWaitLock()) {
                        recognizeInfo.getWaitLock().notifyAll();
                    }
                }
            }
        }
    }

    private void doRecognize(byte[] rgbNv21, byte[] irNv21, List<FacePreviewInfo> facePreviewInfoList, long ftTime, long maskTime) {
        if (facePreviewInfoList != null && !facePreviewInfoList.isEmpty() && previewSize != null) {
            for (FaceRecognizeFilter faceRecognizeFilter : faceRecognizeFilterList) {
                faceRecognizeFilter.filter(facePreviewInfoList);
            }
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
                FacePreviewInfo facePreviewInfo = facePreviewInfoList.get(i);
                if (!facePreviewInfo.isQualityPass()) {
                    continue;
                }
                //跳过mask值为MaskInfo.UNKNOWN的人脸
                if (facePreviewInfo.getMask() == MaskInfo.UNKNOWN) {
                    continue;
                }
                DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, facePreviewInfo.getTrackId());
                recognizeInfo.setFtCost(ftTime);
                recognizeInfo.setMaskCost(maskTime);
                int status = recognizeInfo.getRecognizeStatus();
                /*
                 * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                 */
                if (recognizeConfiguration.isEnableLiveness() && status != RequestFeatureStatus.SUCCEED) {
                    int liveness = recognizeInfo.getLiveness();
                    if (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING
                            || status == RequestFeatureStatus.FAILED) {
                        changeLiveness(facePreviewInfo.getTrackId(), RequestLivenessStatus.ANALYZING);
                        requestFaceLiveness(
                                irNv21 == null ? rgbNv21 : irNv21,
                                facePreviewInfo,
                                previewSize.width,
                                previewSize.height,
                                FaceEngine.CP_PAF_NV21,
                                irNv21 == null ? LivenessType.RGB : LivenessType.IR,
                                recognizeInfo.getWaitLock()
                        );
                    }
                }
                /*
                 * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                 * 特征提取回传的人脸特征结果在{@link DebugFaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                 */
                if (status == RequestFeatureStatus.TO_RETRY) {
                    changeRecognizeStatus(facePreviewInfo.getTrackId(), RequestFeatureStatus.SEARCHING);
                    recognizeInfo.setEnterTime(System.currentTimeMillis());
                    requestFaceFeature(
                            rgbNv21, facePreviewInfo,
                            previewSize.width,
                            previewSize.height,
                            FaceEngine.CP_PAF_NV21
                    );
                }
            }
        }
    }

    @Override
    public void onFail(Exception e) {
        Log.e(TAG, "onFail:" + e.getMessage());
    }

    /**
     * 获取识别信息，识别信息为空则创建一个新的
     *
     * @param recognizeInfoMap 存放识别信息的map
     * @param trackId          人脸唯一标识
     * @return 识别信息
     */
    private DebugRecognizeInfo getRecognizeInfo(Map<Integer, DebugRecognizeInfo> recognizeInfoMap, int trackId) {
        DebugRecognizeInfo recognizeInfo = recognizeInfoMap.get(trackId);
        if (recognizeInfo == null) {
            recognizeInfo = new DebugRecognizeInfo();
            recognizeInfoMap.put(trackId, recognizeInfo);
        }
        return recognizeInfo;
    }

    @Override
    public void onFaceFeatureInfoGet(byte[] nv21, @Nullable FaceFeature faceFeature, Integer trackId, FaceInfo faceInfo,
                                     long frCost, long fqCost, Integer errorCode) {
        //FR成功
        DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
        if (faceFeature != null) {
            //不做活体检测的情况，直接搜索
            if (!recognizeConfiguration.isEnableLiveness()) {
                searchFace(nv21, faceFeature, trackId, faceInfo);
            }
            //活体检测通过，搜索特征
            else if (recognizeInfo.getLiveness() == LivenessInfo.ALIVE) {
                searchFace(nv21, faceFeature, trackId, faceInfo);
            }
            //活体检测未出结果，或者非活体，等待
            else {
                synchronized (recognizeInfo.getWaitLock()) {
                    try {
                        recognizeInfo.getWaitLock().wait();
                        if (recognizeInfoMap.containsKey(trackId)) {
                            onFaceFeatureInfoGet(nv21, faceFeature, trackId, faceInfo, frCost, fqCost, errorCode);
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "onFaceFeatureInfoGet: 等待活体结果时退出界面会执行，正常现象，可注释异常代码块");
                        e.printStackTrace();
                    }
                }
            }
        }
        //特征提取失败时，为了及时提示做个UI反馈，将name修改为"ExtractCode:${errorCode}"，再重置状态
        else {
            // ERROR_CALLBACK: 特征提取失败回调
            if (errorCallback != null && errorDumpConfig.isDumpExtractError() && errorCode != null) {
                errorCallback.onNormalErrorOccurred(DebugInfoDumper.ERROR_TYPE_FEATURE_EXTRACT, nv21,
                        DebugInfoDumper.getExtractFailedFileName(previewSize.width, previewSize.height, trackId, errorCode, faceInfo, frCost, fqCost));
            }

            if (recognizeInfo.increaseAndGetExtractErrorRetryCount() > recognizeConfiguration.getExtractRetryCount()) {
                // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                savePerformanceInfo(trackId, 0, RequestFeatureStatus.FAILED);
                recognizeInfo.setExtractErrorRetryCount(0);
                retryRecognizeDelayed(trackId);
            } else {
                savePerformanceInfo(trackId, 0, RequestFeatureStatus.TO_RETRY);
                changeRecognizeStatus(trackId, RequestFeatureStatus.TO_RETRY);
            }
        }
    }

    /**
     * 延迟 {@link RecognizeConfiguration#getRecognizeFailedRetryInterval()}后，重新进行人脸识别
     *
     * @param trackId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer trackId) {
        changeRecognizeStatus(trackId, RequestFeatureStatus.FAILED);
        Observable.timer(recognizeConfiguration.getRecognizeFailedRetryInterval(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        changeRecognizeStatus(trackId, RequestFeatureStatus.TO_RETRY);
                        getRecognizeInfo(recognizeInfoMap, trackId).setExtractErrorRetryCount(0);
                        getRecognizeInfo(recognizeInfoMap, trackId).setLivenessErrorRetryCount(0);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    @Override
    public void onFaceLivenessInfoGet(byte[] nv21, @Nullable LivenessInfo livenessInfo, Integer trackId, FaceInfo faceInfo, long cost, Integer errorCode, LivenessType livenessType) {

        // ERROR_CALLBACK: 活体检测结果回调
        if (errorCallback != null && errorDumpConfig.isDumpLivenessDetectResult() && errorCode != null) {
            int liveness = livenessInfo == null ? LivenessInfo.UNKNOWN : livenessInfo.getLiveness();
            errorCallback.onNormalErrorOccurred(DebugInfoDumper.ERROR_TYPE_FACE_LIVENESS, nv21,
                    DebugInfoDumper.getFaceLivenessFileName(previewSize.width, previewSize.height, trackId, errorCode, faceInfo, livenessType.ordinal(), liveness, cost));
        }

        DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
        if (livenessInfo != null) {
            int liveness = livenessInfo.getLiveness();
            changeLiveness(trackId, liveness);
            // 非活体，重试
            if (liveness == LivenessInfo.NOT_ALIVE) {
                noticeCurrentStatus("活体检测未通过");
                // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                retryLivenessDetectDelayed(trackId);
            }
            if (liveness == LivenessInfo.ALIVE) {
                Log.i(TAG, "fl success");
            }
            if (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE) {
                onFail(new Exception("fl failed liveness is " + liveness));
            }
        } else {
            // 连续多次活体检测失败（接口调用回传值非0），将活体检测值重置为未知，会在帧回调中重新进行活体检测
            if (recognizeInfo.increaseAndGetLivenessErrorRetryCount() > recognizeConfiguration.getLivenessRetryCount()) {
                recognizeInfo.setLivenessErrorRetryCount(0);
                retryLivenessDetectDelayed(trackId);
            } else {
                changeLiveness(trackId, LivenessInfo.UNKNOWN);
            }
        }
    }

    /**
     * 延迟 {@link RecognizeConfiguration#getLivenessFailedRetryInterval()}后，重新进行活体检测
     *
     * @param trackId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer trackId) {
        Observable.timer(recognizeConfiguration.getLivenessFailedRetryInterval(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        changeLiveness(trackId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    private void noticeCurrentStatus(String notice) {
        if (recognizeCallback != null) {
            recognizeCallback.onNoticeChanged(notice);
        }
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
        }
        timerDisposable = Observable.timer(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (recognizeCallback != null) {
                        recognizeCallback.onNoticeChanged("");
                    }
                });
    }

    private void searchFace(final byte[] nv21, final FaceFeature faceFeature, final Integer trackId, FaceInfo faceInfo) {
        CompareResult compareResult = FaceServer.getInstance().searchFaceFeature(faceFeature, frEngine);
        if (compareResult == null || compareResult.getFaceEntity() == null) {
            savePerformanceInfo(trackId, 0, RequestFeatureStatus.FAILED);
            getRecognizeInfo(recognizeInfoMap, trackId).setExtractErrorRetryCount(0);
            retryRecognizeDelayed(trackId);
            return;
        }
        compareResult.setTrackId(trackId);
        boolean pass = compareResult.getSimilar() > recognizeConfiguration.getSimilarThreshold();
        recognizeCallback.onRecognized(compareResult, getRecognizeInfo(recognizeInfoMap, trackId).getLiveness(), pass);
        if (pass) {
            savePerformanceInfo(trackId, compareResult.getCost(), RequestFeatureStatus.SUCCEED);
            setName(trackId, "识别通过");
            noticeCurrentStatus("识别通过");
            changeRecognizeStatus(trackId, RequestFeatureStatus.SUCCEED);
        } else {
            // ERROR_CALLBACK: 比对失败的回调
            if (errorCallback != null && errorDumpConfig.isDumpCompareFailedError()) {
                FaceEntity faceEntity = compareResult.getFaceEntity();
                String recognizeFeatureFileName = trackId + "-" + System.currentTimeMillis();
                String registerFeatureFileName = faceEntity.getFaceId() + "-" + faceEntity.getUserName();
                String fileName = DebugInfoDumper.getCompareFailedFileName(previewSize.width, previewSize.height, trackId,
                        compareResult.getCompareCode(), faceInfo, compareResult.getSimilar(),
                        recognizeFeatureFileName, registerFeatureFileName);
                errorCallback.onCompareFailed(nv21, fileName, recognizeFeatureFileName, registerFeatureFileName, faceFeature.getFeatureData(), faceEntity);
            }
            noticeCurrentStatus("识别未通过");

            savePerformanceInfo(trackId, compareResult.getCost(), RequestFeatureStatus.FAILED);
            getRecognizeInfo(recognizeInfoMap, trackId).setExtractErrorRetryCount(0);
            retryRecognizeDelayed(trackId);
        }
    }

    /**
     * 人脸特征提取线程
     */
    public class FaceRecognizeRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;
        private int isMask;

        private FaceRecognizeRunnable(byte[] nv21Data, FacePreviewInfo facePreviewInfo, int width, int height, int format) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(facePreviewInfo.getFaceInfoRgb());
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = facePreviewInfo.getTrackId();
            this.isMask = facePreviewInfo.getMask();
        }

        @Override
        public void run() {
            if (nv21Data != null) {
                if (frEngine != null) {
                    if (recognizeConfiguration.isEnableImageQuality()) {
                        /*
                         * 开启人脸质量检测
                         */
                        ImageQualitySimilar qualitySimilar = new ImageQualitySimilar();
                        long iqStartTime = System.currentTimeMillis();
                        int iqCode;
                        synchronized (frEngine) {
                            iqCode = frEngine.imageQualityDetect(nv21Data, width, height, format, faceInfo, isMask, qualitySimilar);
                        }
                        long fqCost = System.currentTimeMillis() - iqStartTime;
                        DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
                        recognizeInfo.setFrQualityCost(fqCost);
                        if (iqCode == ErrorInfo.MOK) {
                            float quality = qualitySimilar.getScore();
                            float destQuality = isMask == MaskInfo.WORN ? recognizeConfiguration.getImageQualityMaskRecognizeThreshold() :
                                    recognizeConfiguration.getImageQualityNoMaskRecognizeThreshold();
                            if (quality >= destQuality) {
                                extractFace(fqCost);
                            } else {
                                onFaceFail(iqCode, "fr imageQuality score invalid", -1, fqCost);
                            }
                        } else {
                            onFaceFail(iqCode, "fr imageQuality failed errorCode is " + iqCode, -1, fqCost);
                        }
                    } else {
                        extractFace(-1);
                    }
                } else {
                    onFaceFail(ERROR_FR_ENGINE_IS_NULL, "fr failed errorCode is null", -1, -1);
                }
            }
            nv21Data = null;
        }

        private void extractFace(long fqCost) {
            FaceFeature faceFeature = new FaceFeature();
            long frStartTime = System.currentTimeMillis();
            int frCode;
            synchronized (frEngine) {
                /*
                 * 该场景为识别场景，所以参数“ExtractType”值为ExtractType.RECOGNIZE，且参数“mask”值为实际检测到的值，即isMask
                 */
                frCode = frEngine.extractFaceFeature(nv21Data, width, height, format, faceInfo, ExtractType.RECOGNIZE, isMask, faceFeature);
            }
            long frTime = System.currentTimeMillis() - frStartTime;
            DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
            recognizeInfo.setExtractCost(frTime);
            if (frCode == ErrorInfo.MOK) {
                recognizeInfo.increaseAndGetExtractErrorRetryCount();
                onFaceFeatureInfoGet(nv21Data, faceFeature, trackId, faceInfo, frTime, fqCost, frCode);
            } else {
                onFaceFail(frCode, "fr failed errorCode is " + frCode, frTime, fqCost);
            }
        }

        private void onFaceFail(int frCode, String errorMsg, long frCost, long fqCost) {
            onFail(new Exception(errorMsg));
            onFaceFeatureInfoGet(nv21Data, null, trackId, faceInfo, frCost, fqCost, frCode);
        }
    }

    /**
     * 活体检测的线程
     */
    public class FaceLivenessDetectRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;
        private LivenessType livenessType;
        private Object waitLock;

        private FaceLivenessDetectRunnable(byte[] nv21Data, FacePreviewInfo faceInfo, int width, int height, int format, LivenessType livenessType, Object waitLock) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo.getFaceInfoRgb());
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = faceInfo.getTrackId();
            this.livenessType = livenessType;
            this.waitLock = waitLock;
        }

        @Override
        public void run() {
            if (nv21Data != null) {
                if (flEngine != null) {
                    processLiveness();
                } else {
                    onProcessFail(0, ERROR_FL_ENGINE_IS_NULL, "fl failed ,frEngine is null");
                }
            }
            nv21Data = null;
        }

        private void processLiveness() {
            List<LivenessInfo> livenessInfoList = new ArrayList<>();
            int flCode = -1;
            long flCost = 0;
            synchronized (flEngine) {
                if (livenessType == LivenessType.RGB) {
                    long start = System.currentTimeMillis();
                    flCode = flEngine.process(nv21Data, width, height, format, Arrays.asList(faceInfo), FaceEngine.ASF_LIVENESS);
                    flCost = System.currentTimeMillis() - start;
                    DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
                    recognizeInfo.setLivenessCost(flCost);
                } else {
                    if (dualCameraFaceInfoTransformer != null) {
                        faceInfo = dualCameraFaceInfoTransformer.transformFaceInfo(faceInfo);
                    }
                    List<FaceInfo> faceInfoList = new ArrayList<>();
                    int fdCode = flEngine.detectFaces(nv21Data, width, height, format, faceInfoList);
                    boolean isFaceExists = isFaceExists(faceInfoList, faceInfo);
                    if (fdCode == ErrorInfo.MOK && isFaceExists) {
                        if (needUpdateFaceData) {
                            /*
                             * 若IR人脸框有偏移，则需要对IR的人脸数据进行upadateFaceData处理，再将处理后的FaceInfo信息传输给活体检测接口
                             */
                            flCode = flEngine.updateFaceData(nv21Data, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                                    new ArrayList<>(Collections.singletonList(faceInfo)));
                            if (flCode == ErrorInfo.MOK) {
                                long start = System.currentTimeMillis();
                                flCode = flEngine.processIr(nv21Data, width, height, format, Arrays.asList(faceInfo), FaceEngine.ASF_IR_LIVENESS);
                                flCost = System.currentTimeMillis() - start;
                                DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
                                recognizeInfo.setLivenessCost(flCost);
                            }
                        } else {
                            long start = System.currentTimeMillis();
                            flCode = flEngine.processIr(nv21Data, width, height, format, Arrays.asList(faceInfo), FaceEngine.ASF_IR_LIVENESS);
                            flCost = System.currentTimeMillis() - start;
                            DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
                            recognizeInfo.setLivenessCost(flCost);
                        }
                    }
                }
            }
            if (flCode == ErrorInfo.MOK) {
                if (livenessType == LivenessType.RGB) {
                    flCode = flEngine.getLiveness(livenessInfoList);
                } else {
                    flCode = flEngine.getIrLiveness(livenessInfoList);
                }
            }

            if (flCode == ErrorInfo.MOK && !livenessInfoList.isEmpty()) {
                getRecognizeInfo(recognizeInfoMap, trackId).increaseAndGetLivenessErrorRetryCount();
                onFaceLivenessInfoGet(nv21Data, livenessInfoList.get(0), trackId, faceInfo, flCost, flCode, livenessType);
                if (livenessInfoList.get(0).getLiveness() == LivenessInfo.ALIVE) {
                    synchronized (waitLock) {
                        waitLock.notifyAll();
                    }
                }
            } else {
                onProcessFail(flCost, flCode, "fl failed flCode is " + flCode);
            }
        }

        private void onProcessFail(long cost, int code, String msg) {
            onFail(new Exception(msg));
            onFaceLivenessInfoGet(nv21Data, null, trackId, faceInfo, cost, code, livenessType);
        }
    }

    private void savePerformanceInfo(int trackId, long compareCost, int status) {
        DebugRecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
        recognizeInfo.setCompareCost(compareCost);
        recognizeInfo.setTotalCost(System.currentTimeMillis() - recognizeInfo.getEnterTime() + recognizeInfo.getFtCost() + recognizeInfo.getMaskCost());
        recognizeInfo.setTrackId(trackId);
        recognizeInfo.setStatus(status);
        if (errorCallback != null && errorDumpConfig.isDumpPerformanceInfo()) {
            String performanceInfo = recognizeInfo.performanceDaraToJsonString();
            recognizeInfo.resetCost();
            Log.i(TAG, "performanceInfo:" + performanceInfo);
            errorCallback.onSavePerformanceInfo(performanceInfo);
        }
    }

    /**
     * 如果人脸列表中有一个人脸和faceInfo相交，则认为该faceInfo可信
     *
     * @param faceInfoList 人脸信息列表
     * @param faceInfo     人脸信息
     * @return 人脸信息列表中是否有人脸和传入的人脸信息相交
     */
    private static boolean isFaceExists(List<FaceInfo> faceInfoList, FaceInfo faceInfo) {
        if (faceInfoList == null || faceInfoList.isEmpty() || faceInfo == null) {
            return false;
        }
        for (FaceInfo info : faceInfoList) {
            if (Rect.intersects(faceInfo.getRect(), info.getRect())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
    private void refreshTrackId(List<FaceInfo> ftFaceList) {
        currentTrackIdList.clear();

        for (FaceInfo faceInfo : ftFaceList) {
            currentTrackIdList.add(faceInfo.getFaceId() + trackedFaceCount);
        }
        if (ftFaceList.size() > 0) {
            currentMaxFaceId = ftFaceList.get(ftFaceList.size() - 1).getFaceId();
        }
    }

    /**
     * 获取当前的最大trackID,可用于退出时保存
     *
     * @return 当前trackId
     */
    public int getTrackedFaceCount() {
        // 引擎的人脸下标从0开始，因此需要+1
        return trackedFaceCount + currentMaxFaceId + 1;
    }

    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    public void setName(int trackId, String name) {
        DebugRecognizeInfo recognizeInfo = recognizeInfoMap.get(trackId);
        if (recognizeInfo != null) {
            recognizeInfo.setName(name);
        }
    }


    /**
     * 设置转换方式，用于IR活体检测
     *
     * @param transformer 转换方式
     */
    public void setDualCameraFaceInfoTransformer(IDualCameraFaceInfoTransformer transformer) {
        this.dualCameraFaceInfoTransformer = transformer;
    }


    public String getName(int trackId) {
        DebugRecognizeInfo recognizeInfo = recognizeInfoMap.get(trackId);
        return recognizeInfo == null ? null : recognizeInfo.getName();
    }


    /**
     * 设置可识别区域（相对于View）
     *
     * @param recognizeArea 可识别区域
     */
    public void setRecognizeArea(Rect recognizeArea) {
        if (recognizeArea != null) {
            this.recognizeArea.set(recognizeArea);
        }
    }

    @IntDef(value = {
            RequestFeatureStatus.FAILED,
            RequestFeatureStatus.SEARCHING,
            RequestFeatureStatus.SUCCEED,
            RequestFeatureStatus.TO_RETRY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestFaceFeatureStatus {
    }

    @IntDef(value = {
            LivenessInfo.ALIVE,
            LivenessInfo.NOT_ALIVE,
            LivenessInfo.UNKNOWN,
            LivenessInfo.FACE_NUM_MORE_THAN_ONE,
            LivenessInfo.FACE_TOO_SMALL,
            LivenessInfo.FACE_ANGLE_TOO_LARGE,
            LivenessInfo.FACE_BEYOND_BOUNDARY,
            RequestLivenessStatus.ANALYZING
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestFaceLivenessStatus {
    }

    /**
     * 修改人脸识别的状态
     *
     * @param trackId   根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @param newStatus 新的识别状态，详见{@link RequestFeatureStatus}中的定义
     */
    public void changeRecognizeStatus(int trackId, @RequestFaceFeatureStatus int newStatus) {
        getRecognizeInfo(recognizeInfoMap, trackId).setRecognizeStatus(newStatus);
    }

    /**
     * 修改活体活体值或活体检测状态
     *
     * @param trackId     根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @param newLiveness 新的活体值或活体检测状态
     */
    public void changeLiveness(int trackId, @RequestFaceLivenessStatus int newLiveness) {
        getRecognizeInfo(recognizeInfoMap, trackId).setLiveness(newLiveness);
    }

    /**
     * 获取活体值或活体检测状态
     *
     * @param trackId 根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @return 活体值或活体检测状态
     */
    public Integer getLiveness(int trackId) {
        return getRecognizeInfo(recognizeInfoMap, trackId).getLiveness();
    }

    /**
     * 获取人脸识别状态
     *
     * @param trackId 根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @return 人脸识别状态
     */
    public Integer getRecognizeStatus(int trackId) {
        return getRecognizeInfo(recognizeInfoMap, trackId).getRecognizeStatus();
    }


    public static final class Builder {
        private FaceEngine ftEngine;
        private FaceEngine frEngine;
        private FaceEngine flEngine;
        private Camera.Size previewSize;
        private RecognizeConfiguration recognizeConfiguration;
        private RecognizeCallback recognizeCallback;
        private IDualCameraFaceInfoTransformer dualCameraFaceInfoTransformer;
        private int frQueueSize;
        private int flQueueSize;
        private int trackedFaceCount;
        private boolean needUpdateFaceData;

        public Builder() {
        }


        public Builder recognizeConfiguration(RecognizeConfiguration val) {
            recognizeConfiguration = val;
            return this;
        }

        public Builder dualCameraFaceInfoTransformer(IDualCameraFaceInfoTransformer val) {
            dualCameraFaceInfoTransformer = val;
            return this;
        }

        public Builder recognizeCallback(RecognizeCallback val) {
            recognizeCallback = val;
            return this;
        }

        public Builder ftEngine(FaceEngine val) {
            ftEngine = val;
            return this;
        }

        public Builder frEngine(FaceEngine val) {
            frEngine = val;
            return this;
        }

        public Builder flEngine(FaceEngine val) {
            flEngine = val;
            return this;
        }


        public Builder previewSize(Camera.Size val) {
            previewSize = val;
            return this;
        }


        public Builder frQueueSize(int val) {
            frQueueSize = val;
            return this;
        }

        public Builder flQueueSize(int val) {
            flQueueSize = val;
            return this;
        }

        public Builder trackedFaceCount(int val) {
            trackedFaceCount = val;
            return this;
        }

        public Builder needUpdateFaceData(boolean val) {
            needUpdateFaceData = val;
            return this;
        }

        public DebugFaceHelper build() {
            return new DebugFaceHelper(this);
        }
    }

    /**
     * 仅保留最大人脸
     *
     * @param ftFaceList 人脸追踪时，一帧数据的人脸信息
     */
    private static void keepMaxFace(List<FaceInfo> ftFaceList) {
        if (ftFaceList == null || ftFaceList.size() <= 1) {
            return;
        }
        FaceInfo maxFaceInfo = ftFaceList.get(0);
        for (FaceInfo faceInfo : ftFaceList) {
            if (faceInfo.getRect().width() > maxFaceInfo.getRect().width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        ftFaceList.add(maxFaceInfo);
    }

}
