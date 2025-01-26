//package com.mozhimen.scank_arc_face.demo;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Rect;
//import android.media.Image;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.util.Size;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.arcsoft.face.ActiveFileInfo;
//import com.arcsoft.face.ErrorInfo;
//import com.arcsoft.face.FaceEngine;
//import com.arcsoft.face.FaceFeature;
//import com.arcsoft.face.FaceInfo;
//import com.arcsoft.face.LivenessInfo;
//import com.arcsoft.face.VersionInfo;
//import com.arcsoft.face.enums.DetectFaceOrientPriority;
//import com.arcsoft.face.enums.DetectMode;
//import com.arcsoft.face.enums.RuntimeABI;
//import com.blankj.utilcode.util.ImageUtils;
//import com.mozhimen.kotlin.utilk.android.app.UtilKApplication;
//import com.mozhimen.scank_arc_face.annors.AProcess;
//import com.mozhimen.scank_arc_face.annors.ADetectResCode;
//import com.mozhimen.scank_arc_face.commons.IScanKArcFace;
//import com.mozhimen.scank_arc_face.commons.IFaceListener;
//import com.mozhimen.scank_arc_face.commons.IInitListener;
//import com.mozhimen.scank_arc_face.cons.CameraConfig;
//import com.mozhimen.scank_arc_face.cons.FaceConfig;
//import com.mozhimen.scank_arc_face.commons.BinocularDetectCallback;
//import com.mozhimen.scank_arc_face.mos.LivenessResult;
//import com.mozhimen.scank_arc_face.mos.CompareInfo;
//import com.mozhimen.scank_arc_face.mos.FacePreviewInfo;
//import com.mozhimen.scank_arc_face.mos.FaceRegisterInfo;
//import com.mozhimen.scank_arc_face.mos.ImageConfig;
//import com.mozhimen.scank_arc_face.mos.DetectConfig;
//import com.mozhimen.scank_arc_face.mos.EngineConfig;
//import com.mozhimen.scank_arc_face.mos.ParamConfig;
//import com.mozhimen.scank_arc_face.mos.FaceLocation;
//import com.mozhimen.scank_arc_face.cons.Configs;
//import com.mozhimen.scank_arc_face.mos.FaceException;
//import com.mozhimen.scank_arc_face.helpers.BinocularLivenessImpl;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//
//import io.reactivex.Observable;
//import io.reactivex.ObservableEmitter;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.Observer;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//
///**
// * @ClassName Face1vnHelper
// * @Description TODO
// * @Author Kolin Zhao / Mozhimen
// * @Version 1.0
// */
//public class Face1vnHelper implements IScanKArcFace<DetectConfig, BinocularDetectCallback> {
//
//    private final Context _context = UtilKApplication.getInstance().get();
//
//    //region #虹软参数配置
//    //最大检测数
//    private static final int MAX_DETECT_NUM = 10;
//
//    //当FR成功，活体未成功时，FR等待活体的时间
//    private static final int WAIT_LIVENESS_INTERVAL = 50;
//
//    //失败重试间隔时间（ms）
//    private static final long FAIL_RETRY_INTERVAL = 3000;
//
//    private static final long NOMARL_RETRY_INTERVAL = 500;
//
//    //失败重试间隔时间（ms）
//    private static final long SUCCESS_RETRY_INTERVAL = 1000;
//
//    //出错重试最大次数
//    private static final int MAX_RETRY_TIME = 3;
//
//    //识别阈值
//    private static final float SIMILAR_THRESHOLD = 0.8F;
//    //endregion
//
//    //region #常量
//    //public static final String TAG = "ScanKArcFaceMgr>>>>>";
//
//    @SuppressLint("StaticFieldLeak")
//    private static Face1vnHelper face1vnHelper;
//
//    public static Face1vnHelper getInstance() {
//        if (face1vnHelper == null) {
//            face1vnHelper = new Face1vnHelper();
//        }
//        return face1vnHelper;
//    }
//
//    //注册人脸状态码，准备注册
//    private static final int REGISTER_STATUS_READY = 0;
//
//    //注册人脸状态码，注册中
//    private static final int REGISTER_STATUS_PROCESSING = 1;
//
//    //注册人脸状态码，注册结束（无论成功失败）
//    private static final int REGISTER_STATUS_DONE = 2;
//
//    //所需的动态库文件
//    private static final String[] LIBRARIES = new String[]{
//            "libarcsoft_face_engine.so", // 人脸相关
//            "libarcsoft_face.so",
//            "libarcsoft_image_util.so",// 图像库相关
//    };
//
//    //活体检测的开关
//    private boolean livenessDetect = false;
//    //endregion
//
//    //region #变量
//    //isDEBUG
//    private boolean isDebug = false;
//
//    //注册状态
//    private int registerStatus = REGISTER_STATUS_DONE;
//
//    //库是否存在
//    boolean isLibraryExists = true;
//    //endregion
//
//    //region #初始化
//    private boolean isRegister = false; //是否注册成功
//
//    private boolean isDetectStart = false;//是否开始识别
//
//    private BinocularLivenessImpl _binocularLivenessImpl;
//
//    private HandlerThread mTrackHandlerThread;
//    private Handler mTrackHandler;
//    private EngineConfig _engineConfig;
//
//    @Override
//    public void init(@NonNull EngineConfig engineConfig, IInitListener initListener) {
//        if (isRegister) {
//            if (initListener != null) {
//                initListener.onInitResult(false, "already init");
//            }
//        }
//
//        mTrackHandlerThread = new HandlerThread("mTrackHandlerThread");
//        mTrackHandlerThread.start();
//
//        mTrackHandler = new Handler(mTrackHandlerThread.getLooper());
//
//        isDetectStart = false;
//        _engineConfig = engineConfig;
//        _binocularLivenessImpl = new BinocularLivenessImpl();
//
//        try {
//            //搜库
//            isLibraryExists = checkSoLibrary(LIBRARIES);
//            if (isLibraryExists) {
//                VersionInfo versionInfo = new VersionInfo();
//                int code = FaceEngine.getVersion(versionInfo);
//            } else {
//                throw new FaceException(FaceException.SQ_ERR_NATIVE_LIB_LOAD_FAIL, "native lib load fail!");
//            }
//
//            //激活引擎
//            ActiveFileInfo activeFileInfo = new ActiveFileInfo();
//            int res = FaceEngine.getActiveFileInfo(_context, activeFileInfo);
//            if (res == ErrorInfo.MOK) {
//                initEngine(initListener);
//            } else {
//                activeEngine(initListener);
//            }
//        } catch (FaceException e) {
//            isRegister = false;
//            if (initListener != null) {
//                initListener.onInitResult(false, e.getMessage());
//            }
//        }
//    }
//
//    @Override
//    public void setImageConfig(ImageConfig imageConfig) {
//        if (this._binocularLivenessImpl != null && this._binocularLivenessImpl.getConfig() != null) {
//            this._binocularLivenessImpl.getConfig().setImageConfig(imageConfig);
//        }
//    }
//
//    //检查能否找到动态链接库，如果找不到，请修改工程配置
//    private boolean checkSoLibrary(String[] libraries) {
//        File dir = new File(_context.getApplicationInfo().nativeLibraryDir);
//        File[] files = dir.listFiles();
//        if (files == null || files.length == 0) {
//            return false;
//        }
//        List<String> libraryNameList = new ArrayList<>();
//        for (File file : files) {
//            libraryNameList.add(file.getName());
//        }
//        boolean exists = true;
//        for (String library : libraries) {
//            exists &= libraryNameList.contains(library);
//        }
//        return exists;
//    }
//
//    //激活引擎
//    public void activeEngine(final IInitListener initListener) throws FaceException {
//        if (!isLibraryExists) {
//            throw new FaceException(FaceException.SQ_ERR_NATIVE_LIB_LOAD_FAIL, "native lib load fail!");
//        }
//
//        Observable.create(new ObservableOnSubscribe<Integer>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<Integer> emitter) {
//                        RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
//
//                        //激活引擎
//                        long start = System.currentTimeMillis();
//                        int activeCode = FaceEngine.activeOnline(_context, _engineConfig.getActiveKey(), _engineConfig.getAppId(), _engineConfig.getSdkKey());
//
//                        emitter.onNext(activeCode);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Integer>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(Integer activeCode) {
//                        if (activeCode == ErrorInfo.MOK) {
//                            initEngine(initListener);
//                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            initEngine(initListener);
//                        } else {
//                            isRegister = false;
//                            if (initListener != null) {
//                                initListener.onInitResult(false, "active fail " + activeCode);
//                            }
//                        }
//
//                        //激活文件信息
//                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
//                        int res = FaceEngine.getActiveFileInfo(_context, activeFileInfo);
//                        if (res == ErrorInfo.MOK) {
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        isRegister = false;
//                        if (initListener != null) {
//                            initListener.onInitResult(false, e.getMessage());
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
//    }
//
//    //人脸追踪引擎
//    private FaceEngine faceTraceEngine;
//
//    //特征提取引擎
//    private FaceEngine faceFeatureEngine;
//
//    //活体检测引擎
//    //private FaceEngine faceLiveEngine;
//
//    private int ftInitCode = -1;
//    private int frInitCode = -1;
//    //private int flInitCode = -1;
//
//    private List<CompareInfo> compareResultList;
//
//    //初始化引擎
//    private void initEngine(IInitListener initListener) {
//        //faceTraceEngine
//        faceTraceEngine = new FaceEngine();
//        DetectFaceOrientPriority detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_ALL_OUT;
//        if (CameraConfig.INSTANCE.getDegress() == 0) {
//            detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_0_ONLY;
//        } else if (CameraConfig.INSTANCE.getDegress() == 90) {
//            detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_90_ONLY;
//        } else if (CameraConfig.INSTANCE.getDegress() == 180) {
//            detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_180_ONLY;
//        } else if (CameraConfig.INSTANCE.getDegress() == 270) {
//            detectFaceOrientPriority = DetectFaceOrientPriority.ASF_OP_270_ONLY;
//        }
//        ftInitCode = faceTraceEngine.init(_context, DetectMode.ASF_DETECT_MODE_VIDEO, detectFaceOrientPriority,
//                MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);
//
//        //faceFeatureEngine
//        faceFeatureEngine = new FaceEngine();
//        frInitCode = faceFeatureEngine.init(_context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
//                MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);
//
//        //faceLiveEngine
//        /*faceLiveEngine = new FaceEngine();
//        flInitCode = faceLiveEngine.init(_context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
//                16, MAX_DETECT_NUM, FaceEngine.ASF_IR_LIVENESS);
//        faceLiveEngine.setLivenessParam(new LivenessParam(0.4f, 0.5f)); //0.5,0.7
//        Log.i(TAG, "initFlEngine:  init: " + ftInitCode);*/
//
//        if (ftInitCode != ErrorInfo.MOK) {
//            String error = "faceTraceEngine 初始化失败，错误码为:" + ftInitCode;
//            isRegister = false;
//            if (initListener != null) {
//                initListener.onInitResult(false, "init fail " + ftInitCode);
//            }
//        } else if (frInitCode != ErrorInfo.MOK) {
//            String error = "faceFeatureEngine 初始化失败，错误码为:" + ftInitCode;
//            isRegister = false;
//            if (initListener != null) {
//                initListener.onInitResult(false, "init fail" + ftInitCode);
//            }
//        } /*else if (flInitCode != ErrorInfo.MOK) {
//            String error = _context.getString(R.string.specific_engine_init_failed, "faceLiveEngine", ftInitCode);
//            Log.i(TAG, "initEngine: " + error);
//            isRegister = false;
//            if (initListener != null) {
//                initListener.onResult(false);
//            }
//        }*/ else {
//            FaceServer.getInstance().init(_context);
//            compareResultList = new ArrayList<>();
//            isRegister = true;
//            if (initListener != null) {
//                initListener.onInitResult(true, "face init success");
//            }
//        }
//    }
//    //endregion
//
//    //region #继承方法
//    @Override
//    public boolean isInitSuccess() {
//        return isRegister;
//    }
//
//    @Override
//    public void setDebug(boolean isDebug) {
//        this.isDebug = isDebug;
//    }
//
//    @Override
//    public void setThresholdConfig(ParamConfig paramConfig) {
//        if (this._binocularLivenessImpl != null && this._binocularLivenessImpl.getConfig() != null) {
//            this._binocularLivenessImpl.getConfig().setParamConfig(paramConfig);
//        }
//    }
//
//    @Override
//    public void addListener(BinocularDetectCallback listener) {
//        if (this._binocularLivenessImpl != null) {
//            _binocularLivenessImpl.addListener(listener);
//        }
//    }
//
//    @Override
//    public void removeListener(BinocularDetectCallback listener) {
//        if (this._binocularLivenessImpl != null) {
//            _binocularLivenessImpl.removeListener(listener);
//        }
//    }
//    //endregion
//
//    //region #工具方法
//    @Override
//    public void deleteAllCache() {
//        FaceServer.getInstance().clearAllFaces(_context);
//    }
//
//    public synchronized void deleteForById(String name) {
//        FaceServer.getInstance().deleteFeatureForById(_context, name);
//    }
//
//    //根据注册人脸UserName获取Bitmap
//    public Bitmap getRegisterFaceForById(String name) {
//        String path = FaceServer.getInstance().getFaceImagePathForByName(name);
//        Bitmap bitmap = ImageUtils.getBitmap(path);
//        return bitmap;
//    }
//
//    public synchronized boolean isExistForById(String name) {
//        return FaceServer.getInstance().isExistFeatureForById(_context, name);
//    }
//    //endregion
//
//    //region #特征值
//    //特征值比对
//    public boolean compareFeature(FaceFeature f1, FaceFeature f2) {
//        return FaceServer.getInstance().compareFeature(f1, f2) > FaceConfig.INSTANCE.getVerifyScore();
//    }
//
//    //特征值查询
//    public CompareInfo search(byte[] feature) {
//        return FaceServer.getInstance().getTopOfFaceLib(new FaceFeature(
//                feature
//        ));
//    }
//
//    //搜寻人脸
//    private void searchFace(final byte[] nv21, final FaceInfo faceInfo, final FaceFeature frFace, final Integer requestId) {
//        Observable
//                .create(new ObservableOnSubscribe<CompareInfo>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<CompareInfo> emitter) {
//                        CompareInfo compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        if (compareResult == null) {
//                            emitter.onError(new Throwable("VISITOR"));
//                        } else {
//                            emitter.onNext(compareResult);
//                        }
//                    }
//                })
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<CompareInfo>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(CompareInfo compareResult) {
//                        if (compareResult == null || compareResult.getPersonUuid() == null) {
//                            requestFeatureStatusMap.put(requestId, AProcess.Companion.getRES_FAILED());
//                            faceHelper.setName(requestId, "VISITOR " + requestId);
//                            retryFailRecognizeDelayed(requestId);
//                            Bitmap bitmap = FaceServer.getInstance().getHeadBitmap(nv21, 1280,
//                                    720, faceInfo);
//                            Rect rect = new Rect(faceInfo.getRect().left, faceInfo.getRect().top, faceInfo.getRect().right, faceInfo.getRect().bottom);
//                            _binocularLivenessImpl.onLivenessResultGet(
//                                    ADetectResCode.Companion.getRES_HACK(),
//                                    new LivenessResult(
//                                            requestId,
//                                            bitmap,
//                                            rect,
//                                            null,
//                                            faceInfo
//                                    )
//                            );
//                            return;
//                        }
//
//                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
//                            boolean isAdded = false;
//                            // 初始化失败了
//                            if (compareResultList == null) {
//                                //访客
//                                requestFeatureStatusMap.put(requestId, AProcess.Companion.getRES_FAILED());
//                                faceHelper.setName(requestId, "VISITOR " + requestId);
//                                return;
//                            }
//                            for (CompareInfo compareResult1 : compareResultList) {
//                                if (compareResult1.getTrackId() == requestId) {
//                                    isAdded = true;
//                                    break;
//                                }
//                            }
//                            if (!isAdded) {
//                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
//                                if (compareResultList.size() >= MAX_DETECT_NUM) {
//                                    compareResultList.remove(0);
//                                    //adapter.notifyItemRemoved(0);
//                                }
//                                //添加显示人员时，保存其trackId
//                                compareResult.setTrackId(requestId);
//                                compareResultList.add(compareResult);
//                                //adapter.notifyItemInserted(compareResultList.size() - 1);
////                                File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResultList.get(compareResultList.size() - 1).getUserName() + FaceServer.IMG_SUFFIX);
////                                Glide.with(holder.imageView)
////                                        .load(imgFile)
////                                        .into(holder.imageView);
//                                Bitmap bitmap = FaceServer.getInstance().getHeadBitmap(nv21,
//                                        1280,
//                                        720,
//                                        faceInfo);
//                                Rect rect = new Rect(faceInfo.getRect().left, faceInfo.getRect().top, faceInfo.getRect().right, faceInfo.getRect().bottom);
//                                _binocularLivenessImpl.onLivenessResultGet(
//                                        ADetectResCode.Companion.getRES_OK(),
//                                        new LivenessResult(
//                                                compareResult.getTrackId(),
//                                                bitmap,
//                                                rect,
//                                                compareResult,
//                                                faceInfo
//                                        ));
//                            }
//                            requestFeatureStatusMap.put(requestId, AProcess.Companion.getRES_SUCCEED());
//                            faceHelper.setName(requestId, "通过:" + compareResult.getPersonUuid());
//
//                        } else {
//                            faceHelper.setName(requestId, "未通过:NOT_REGISTERED");
//                            retryFailRecognizeDelayed(requestId);
//                            Bitmap bitmap = FaceServer.getInstance().getHeadBitmap(nv21, 1280,
//                                    720, faceInfo);
//                            Rect rect = new Rect(faceInfo.getRect().left, faceInfo.getRect().top, faceInfo.getRect().right, faceInfo.getRect().bottom);
//                            _binocularLivenessImpl.onLivenessResultGet(
//                                    ADetectResCode.Companion.getRES_HACK(),
//                                    new LivenessResult(
//                                            compareResult.getTrackId(),
//                                            bitmap,
//                                            rect,
//                                            compareResult,
//                                            faceInfo
//                                    )
//                            );
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        faceHelper.setName(requestId, "未通过:NOT_REGISTERED");
//                        retryFailRecognizeDelayed(requestId);
//                        Bitmap bitmap = FaceServer.getInstance().getHeadBitmap(nv21, 1280,
//                                720, faceInfo);
//                        Rect rect = new Rect(faceInfo.getRect().left, faceInfo.getRect().top, faceInfo.getRect().right, faceInfo.getRect().bottom);
//                        _binocularLivenessImpl.onLivenessResultGet(
//                                ADetectResCode.Companion.getRES_HACK(),
//                                new LivenessResult(
//                                        requestId,
//                                        bitmap,
//                                        rect,
//                                        null,
//                                        faceInfo
//                                )
//                        );
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }
//
//    //延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
//    public void retryFailRecognizeDelayed(final Integer requestId) {
//        requestFeatureStatusMap.put(requestId, AProcess.Companion.getRES_FAILED());
//        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
//                .subscribe(new Observer<Long>() {
//                    Disposable disposable;
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        disposable = d;
//                        delayFaceTaskCompositeDisposable.add(disposable);
//                    }
//
//                    @Override
//                    public void onNext(Long aLong) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
//                        faceHelper.setName(requestId, Integer.toString(requestId));
//                        requestFeatureStatusMap.put(requestId, AProcess.Companion.getRETRY());
//                        delayFaceTaskCompositeDisposable.remove(disposable);
//                    }
//                });
//    }
//
//    //延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
//    public void retryRecognizeDelayed(final Integer requestId) {
//        requestFeatureStatusMap.put(requestId, AProcess.Companion.getRES_FAILED());
//        Observable.timer(NOMARL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
//                .subscribe(new Observer<Long>() {
//                    Disposable disposable;
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        disposable = d;
//                        delayFaceTaskCompositeDisposable.add(disposable);
//                    }
//
//                    @Override
//                    public void onNext(Long aLong) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
//                        faceHelper.setName(requestId, Integer.toString(requestId));
//                        requestFeatureStatusMap.put(requestId, AProcess.Companion.getRETRY());
//                        delayFaceTaskCompositeDisposable.remove(disposable);
//                    }
//                });
//    }
//
//    //获取注册信息并且保存特征数据库
//    public synchronized FaceRegisterInfo extractFeatureAndInsert(String userName, Bitmap image) {
//        return FaceServer.getInstance().getFaceFeatureForBitmap(_context, image, userName);
//    }
//
//    //删除已经离开的人脸
//    private void clearLastFace(List<FacePreviewInfo> facePreviewInfoList) {//人脸和trackId列表
//        if (compareResultList != null) {
//            for (int i = compareResultList.size() - 1; i >= 0; i--) {
//                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
//                    compareResultList.remove(i);
////                    adapter.notifyItemRemoved(i);
//                }
//            }
//        }
//        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
//            requestFeatureStatusMap.clear();
//            //livenessMap.clear();
//            //livenessErrorRetryMap.clear();
//            extractErrorRetryMap.clear();
//            if (getFeatureDelayedDisposables != null) {
//                getFeatureDelayedDisposables.clear();
//            }
//            return;
//        }
//        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
//        while (keys.hasMoreElements()) {
//            int key = keys.nextElement();
//            boolean contained = false;
//            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
//                if (facePreviewInfo.getTrackId() == key) {
//                    contained = true;
//                    break;
//                }
//            }
//            if (!contained) {
//                requestFeatureStatusMap.remove(key);
//                //livenessMap.remove(key);
//                //livenessErrorRetryMap.remove(key);
//                extractErrorRetryMap.remove(key);
//            }
//        }
//    }
//
//    public void insert(String userName, byte[] bytes) {
//        FaceServer.getInstance().insert(userName, bytes);
//    }
//
//    /**
//     * 注册人脸
//     *
//     * @param nv21Rgb             RGB摄像头的帧数据
//     * @param facePreviewInfoList {@link FaceHelper#onPreviewFrame(byte[])}回传的处理结果
//     */
//    /*private void registerFace(final byte[] nv21Rgb, final List<FacePreviewInfo> facePreviewInfoList) {
//        if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
//            registerStatus = REGISTER_STATUS_PROCESSING;
//            Observable.create(new ObservableOnSubscribe<Boolean>() {
//                @Override
//                public void subscribe(ObservableEmitter<Boolean> emitter) {
//                    boolean success = FaceServer.getInstance().registerNv21(
//                            IrRegisterAndRecognizeActivity.this, nv21Rgb,
//                            previewSize.width, previewSize.height,
//                            facePreviewInfoList.get(0).getFaceInfo(),
//                            "registered " + faceHelperIr.getTrackedFaceCount());
//                    emitter.onNext(success);
//                }
//            })
//                    .subscribeOn(Schedulers.computation())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Boolean>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(Boolean success) {
//                            String result = success ? "register success!" : "register failed!";
//                            showToast(result);
//                            registerStatus = REGISTER_STATUS_DONE;
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            e.printStackTrace();
//                            showToast("register failed!");
//                            registerStatus = REGISTER_STATUS_DONE;
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        }
//    }*/
//    //endregion
//
//    //region #检测开始
//    private transient byte[] rgbData = null;
//    private transient Image rgbImageData = null;
//
//    @Override
//    public void input(byte[] rgbByte) {
//        if (rgbByte != null && isRegister && isDetectStart) {
//            rgbData = null;
//            rgbData = rgbByte;
//            processPreviewData();
//        }
//    }
//
//    List<FacePreviewInfo> oldFacePreviewInfoList;
//
//    //用于记录人脸识别相关状态
//    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
//
//    //处理预览数据
//    private synchronized void processPreviewData() {
//        if (rgbData != null) {
//            final byte[] cloneNv21Rgb = rgbData.clone();
//            //刷新人脸框
////            this.mStrategyAdapter.onFaceLocation(null);
//            List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(cloneNv21Rgb);
////            if (facePreviewInfoList != null && faceRectView != null && drawHelperRgb != null
////                    && faceRectViewIr != null && drawHelperIr != null) {
////                drawPreviewInfo(facePreviewInfoList);
////            }
//            //刷新人脸框 只显示第一个脸
//            if (facePreviewInfoList != oldFacePreviewInfoList && facePreviewInfoList != null && facePreviewInfoList.size() != 0) {
//                oldFacePreviewInfoList = facePreviewInfoList;
//                this._binocularLivenessImpl.onFaceLocationGet(new FaceLocation(
//                        facePreviewInfoList.get(0).getFaceInfo().getRect().left,
//                        facePreviewInfoList.get(0).getFaceInfo().getRect().top,
//                        facePreviewInfoList.get(0).getFaceInfo().getRect().right,
//                        facePreviewInfoList.get(0).getFaceInfo().getRect().bottom
//                ));
////                Logger.INSTANCE.d("ffff ---,",(facePreviewInfoList.get(0).getFaceInfo().getRect().bottom - facePreviewInfoList.get(0).getFaceInfo().getRect().top)+"");
////                Logger.INSTANCE.d("ffff ---,",(facePreviewInfoList.get(0).getFaceInfo().getRect().right - facePreviewInfoList.get(0).getFaceInfo().getRect().left)+"");
//                if (facePreviewInfoList.get(0).getFaceInfo().getRect().bottom - facePreviewInfoList.get(0).getFaceInfo().getRect().top < 80 ||
//                        facePreviewInfoList.get(0).getFaceInfo().getRect().right - facePreviewInfoList.get(0).getFaceInfo().getRect().left < 80) {
//                    clearLastFace(facePreviewInfoList);
//                    return;
//                }
//            } else {
//                this._binocularLivenessImpl.onFaceLocationGet(null);
//            }
////            registerFace(cloneNv21Rgb, facePreviewInfoList);
//            clearLastFace(facePreviewInfoList);
//
//            if (facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
//                for (int i = 0; i < facePreviewInfoList.size(); i++) {
//                    // 注意：这里虽然使用的是IR画面活体检测，RGB画面特征提取，但是考虑到成像接近，所以只用了RGB画面的图像质量检测
//                    Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
//                    /**
//                     * 在活体检测开启，在人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
//                     */
//                    /*if (livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
//                        Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
//                        if (liveness == null
//                                || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
//                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
//                            // IR数据偏移
//                            FaceInfo faceInfo = facePreviewInfoList.get(i).getFaceInfo().clone();
//                            faceInfo.getRect().offset(FaceConfig.HORIZONTAL_OFFSET, FaceConfig.VERTICAL_OFFSET);
//                            faceHelperIr.requestFaceLiveness(irData.clone(), faceInfo, CameraConfig.INSTANCE.getPreview_width(),
//                                    CameraConfig.INSTANCE.getPreview_height(), FaceEngine.CP_PAF_NV21,
//                                    facePreviewInfoList.get(i).getTrackId(), LivenessType.IR);
//                        }
//                    }*/
//                    /**
//                     * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
//                     * 特征提取回传的人脸特征结果在{@link IFaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
//                     */
//                    if (status == null
//                            || status == AProcess.Companion.getRETRY()) {
//                        requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), AProcess.Companion.getDETECING());
//                        faceHelper.requestFaceFeature(cloneNv21Rgb, facePreviewInfoList.get(i).getFaceInfo(),
//                                1280, 720
//                                , FaceEngine.CP_PAF_NV21,
//                                facePreviewInfoList.get(i).getTrackId());
//                    }
//                }
//            }
//            rgbData = null;
//        }
//    }
//
//    @Override
//    public void start() throws FaceException {
//        if (!isRegister) {
//            throw new FaceException(FaceException.SQ_ERR_VALID_FAIL, "not init engine!");
//        }
//
//        if (faceHelper == null) {
//            faceHelper = new FaceHelper.Builder()
//                    .faceTraceEngine(faceTraceEngine)
//                    .faceFeatureEngine(faceFeatureEngine)
//                    //.faceLiveEngine(faceLiveEngine)
//                    //.faceFeatureQueueSize(MAX_DETECT_NUM)
//                    //.faceLiveQueueSize(MAX_DETECT_NUM)
//                    .previewSize(new Size(
//                            1280,
//                            720
//                    ))
//                    .faceListener(faceListener)
//                    .detectedFaceCount(
//                            Configs.getTrackedFaceCount(
//                                    _context.getApplicationContext()))
//                    .build();
//        }
//
//        isDetectStart = true;
//    }
//    //endregion
//
//    //region #检测重新开始
//    //重新识别
//    public void clearFaceRecord(int trackId) {
//        compareResultList.clear();
//        retrySuccessRecognizeDelayed(trackId);
//    }
//
//    //延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
//    public void retrySuccessRecognizeDelayed(final Integer requestId) {
//        requestFeatureStatusMap.put(requestId, AProcess.Companion.getRES_FAILED());
//        Observable.timer(SUCCESS_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
//                .subscribe(new Observer<Long>() {
//                    Disposable disposable;
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        disposable = d;
//                        delayFaceTaskCompositeDisposable.add(disposable);
//                    }
//
//                    @Override
//                    public void onNext(Long aLong) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
//                        faceHelper.setName(requestId, Integer.toString(requestId));
//                        requestFeatureStatusMap.put(requestId, AProcess.Companion.getRETRY());
//                        delayFaceTaskCompositeDisposable.remove(disposable);
//                    }
//                });
//    }
//    //endregion
//
//    //region #检测停止
//    @Override
//    public void stop() throws FaceException {
//        if (!isRegister) {
//            throw new FaceException(FaceException.SQ_ERR_VALID_FAIL, "not init engine!");
//        }
//        isDetectStart = true;
//
//        //TODO sdk stop
//    }
//    //endregion
//
//    //region #释放
//    private FaceHelper faceHelper;
//
//    @Override
//    public void release() {
//        isDetectStart = false;
//        isRegister = false;
//
//        unInitEngine();
//
//        FaceServer.getInstance().unInit();
//
//        if (faceHelper != null) {
//            Configs.setTrackedFaceCount(_context, faceHelper.getTrackedFaceCount());
//            faceHelper.release();
//            faceHelper = null;
//        }
//
//        if (_binocularLivenessImpl != null) {
//            _binocularLivenessImpl.onDestroy();
//            _binocularLivenessImpl = null;
//        }
//        if (mTrackHandler != null) {
//            mTrackHandler.removeCallbacksAndMessages(null);
//        }
//        if (mTrackHandlerThread != null) {
//            mTrackHandlerThread.quit();
//            mTrackHandlerThread.interrupt();
//            mTrackHandlerThread = null;
//        }
//    }
//
//    //销毁引擎，faceHelperIr中可能会有特征提取耗时操作仍在执行，加锁防止crash
//    private void unInitEngine() {
//        if (ftInitCode == ErrorInfo.MOK && faceTraceEngine != null) {
//            synchronized (faceTraceEngine) {
//                int ftUnInitCode = faceTraceEngine.unInit();
//            }
//        }
//        if (frInitCode == ErrorInfo.MOK && faceFeatureEngine != null) {
//            synchronized (faceFeatureEngine) {
//                int frUnInitCode = faceFeatureEngine.unInit();
//            }
//        }
//        /*if (flInitCode == ErrorInfo.MOK && faceLiveEngine != null) {
//            synchronized (faceLiveEngine) {
//                int flUnInitCode = faceLiveEngine.unInit();
//                Log.i(TAG, "unInitEngine: " + flUnInitCode);
//            }
//        }*/
//    }
//    //endregion
//
//    //region #监听器
//    //用于记录人脸特征提取出错重试次数
//    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
//
//    //用于存储活体值
//    //private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
//
//    //用于存储活体检测出错重试次数
//    //private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();
//
//    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
//    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
//
//    IFaceListener faceListener = new IFaceListener() {
//        @Override
//        public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, @Nullable Integer trackId, @Nullable Integer errorCode) {
//
//        }
//
//        @Override
//        public void onFail(Exception e) {
//
//        }
//
//        //请求FR的回调
//        @Override
//        public void onFaceFeatureInfoGet(@Nullable final byte[] nv21, final FaceInfo faceInfo, @Nullable final FaceFeature faceFeature, final Integer trackId, final Integer errorCode) {
//            //FR成功
//            if (faceFeature != null) {
//                //Integer liveness = livenessMap.get(requestId);
//                //不做活体检测的情况，直接搜索
//                if (!livenessDetect) {
//                    searchFace(nv21, faceInfo, faceFeature, trackId);
//                }
//                //活体检测通过，搜索特征
//                /*else if (liveness != null && liveness == LivenessInfo.ALIVE) {
//                    searchFace(nv21, faceInfo, faceFeature, requestId);
//                }*/
//                //活体检测未出结果，或者非活体，延迟执行该函数
//                /*else {
//                    if (requestFeatureStatusMap.containsKey(requestId)) {
//                        Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
//                                .subscribe(new Observer<Long>() {
//                                    Disposable disposable;
//
//                                    @Override
//                                    public void onSubscribe(Disposable d) {
//                                        disposable = d;
//                                        getFeatureDelayedDisposables.add(disposable);
//                                    }
//
//                                    @Override
//                                    public void onNext(Long aLong) {
//                                        onFaceFeatureInfoGet(nv21, faceInfo, faceFeature, requestId, errorCode);
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//
//                                    }
//
//                                    @Override
//                                    public void onComplete() {
//                                        getFeatureDelayedDisposables.remove(disposable);
//                                    }
//                                });
//                    }
//                }*/
//            }
//            //特征提取失败
//            else {
//                if (increaseAndGetValue(extractErrorRetryMap, trackId) > MAX_RETRY_TIME) {
//                    extractErrorRetryMap.put(trackId, 0);
//                    String msg;
//                    // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
//                    if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
//                        msg = /*mContext.getString(R.string.low_confidence_level);*/"人脸置信度低";
//                    } else {
//                        msg = "ExtractCode:" + errorCode;
//                    }
//                    faceHelper.setName(trackId, "未通过:" + msg);
//                    // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
//                    requestFeatureStatusMap.put(trackId, AProcess.Companion.getRES_FAILED());
//                    retryRecognizeDelayed(trackId);
//                } else {
//                    requestFeatureStatusMap.put(trackId, AProcess.Companion.getRETRY());
//                }
//            }
//        }
//
//        /*@Override
//        public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
//            if (livenessInfo != null) {
//                int liveness = livenessInfo.getLiveness();
//                livenessMap.put(requestId, liveness);
//                // 非活体，重试
//                if (liveness == LivenessInfo.NOT_ALIVE) {
//                    faceHelper.setName(requestId, mContext.getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
//                    // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
//                    retryLivenessDetectDelayed(requestId);
//                }
//            } else {
//                if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
//                    livenessErrorRetryMap.put(requestId, 0);
//                    String msg;
//                    // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用RGB人脸框 + IR数据，一般是人脸模糊或画面中无人脸
//                    if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
//                        msg = mContext.getString(R.string.low_confidence_level);
//                    } else {
//                        msg = "ProcessCode:" + errorCode;
//                    }
//                    faceHelper.setName(requestId, mContext.getString(R.string.recognize_failed_notice, msg));
//                    // 在尝试最大次数后，活体检测仍然失败，则认定为非活体
//                    livenessMap.put(requestId, LivenessInfo.NOT_ALIVE);
//                    retryLivenessDetectDelayed(requestId);
//                } else {
//                    livenessMap.put(requestId, LivenessInfo.UNKNOWN);
//                }
//            }
//        }*/
//    };
//
//    //延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
//    /*public void retryLivenessDetectDelayed(final Integer requestId) {
//        Observable.timer(NOMARL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
//                .subscribe(new Observer<Long>() {
//                    Disposable disposable;
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        disposable = d;
//                        delayFaceTaskCompositeDisposable.add(disposable);
//                    }
//
//                    @Override
//                    public void onNext(Long aLong) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
//                        if (livenessDetect) {
//                            faceHelper.setName(requestId, Integer.toString(requestId));
//                        }
//                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
//                        delayFaceTaskCompositeDisposable.remove(disposable);
//                    }
//                });
//    }*/
//
//    //将map中key对应的value增1回传
//    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
//        if (countMap == null) {
//            return 0;
//        }
//        Integer value = countMap.get(key);
//        if (value == null) {
//            value = 0;
//        }
//        countMap.put(key, ++value);
//        return value;
//    }
//    //endregion
//}
