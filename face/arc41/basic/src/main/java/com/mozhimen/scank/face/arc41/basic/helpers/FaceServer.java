package com.mozhimen.scank.face.arc41.basic.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.ExtractType;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;
import com.mozhimen.kotlin.utilk.java.io.UtilKFile;
import com.mozhimen.scank.face.arc.basic.mos.CompareInfo;
import com.mozhimen.scank.face.arc.basic.mos.FaceRegisterInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName FaceServer
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Version 1.0
 */
public class FaceServer {
    //region #常量
    private static final String TAG = "FaceServer>>>>>";

    private static FaceServer faceServer = null;

    public static FaceServer getInstance() {
        if (faceServer == null) {
            synchronized (FaceServer.class) {
                if (faceServer == null) {
                    faceServer = new FaceServer();
                }
            }
        }
        return faceServer;
    }

    //图片格式
    public static final String IMG_SUFFIX = ".jpg";

    //文件目录
    public static String ROOT_PATH;

    //存放注册图的目录
    public static final String SAVE_IMG_DIR = "register" + File.separator + "imgs";

    //存放特征的目录
    private static final String SAVE_FEATURE_DIR = "register" + File.separator + "features";
    //endregion

    //region #变量

    //endregion

    //region #初始化
    private static FaceEngine faceEngine = null;
    private static List<FaceRegisterInfo> faceRegisterInfoList;

    //初始化
    public boolean init(Context context) {
        synchronized (this) {
            if (faceEngine == null && context != null) {
                faceEngine = new FaceEngine();
                int engineCode = faceEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
                if (engineCode == ErrorInfo.MOK) {
                    initFaceList(context);
                    return true;
                } else {
                    faceEngine = null;
                    Log.e(TAG, "init: failed! code = " + engineCode);
                    return false;
                }
            }
            return false;
        }
    }

    //初始化人脸特征数据以及人脸特征数据对应的注册图
    private void initFaceList(Context context) {
        synchronized (this) {
            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() || !featureDir.isDirectory()) {
                return;
            }
            File[] featureFiles = featureDir.listFiles();
            if (featureFiles == null || featureFiles.length == 0) {
                return;
            }
            faceRegisterInfoList = new ArrayList<>();
            for (File featureFile : featureFiles) {
                try {
                    FileInputStream fis = new FileInputStream(featureFile);
                    byte[] feature = new byte[FaceFeature.FEATURE_SIZE];
                    fis.read(feature);
                    fis.close();
                    faceRegisterInfoList.add(new FaceRegisterInfo(feature, featureFile.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //endregion

    //region #释放
    //销毁
    public void unInit() {
        synchronized (this) {
            if (faceRegisterInfoList != null) {
                faceRegisterInfoList.clear();
                faceRegisterInfoList = null;
            }
            if (faceEngine != null) {
                faceEngine.unInit();
                faceEngine = null;
            }
        }
    }
    //endregion

    //region #工具方法
    public String getFaceImagePathForByName(String name) {
        //图片存储的文件夹
        File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
        if (!imgDir.exists() && !imgDir.mkdirs()) {
            Log.e(TAG, "registerNv21: can not create image directory");
            return null;
        }
        String pathname = imgDir + File.separator + name + IMG_SUFFIX;
        return pathname;
    }

    public int getFaceNumber(Context context) {
        synchronized (this) {
            if (context == null) {
                return 0;
            }
            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }

            File featureFileDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            int featureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                String[] featureFiles = featureFileDir.list();
                featureCount = featureFiles == null ? 0 : featureFiles.length;
            }
            int imageCount = 0;
            File imgFileDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                String[] imageFiles = imgFileDir.list();
                imageCount = imageFiles == null ? 0 : imageFiles.length;
            }
            return featureCount > imageCount ? imageCount : featureCount;
        }
    }

    public int clearAllFaces(Context context) {
        synchronized (this) {
            if (context == null) {
                return 0;
            }
            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            if (faceRegisterInfoList != null) {
                faceRegisterInfoList.clear();
            }
            File featureFileDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            int deletedFeatureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                File[] featureFiles = featureFileDir.listFiles();
                if (featureFiles != null && featureFiles.length > 0) {
                    for (File featureFile : featureFiles) {
                        if (featureFile.delete()) {
                            deletedFeatureCount++;
                        }
                    }
                }
            }
            int deletedImageCount = 0;
            File imgFileDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                File[] imgFiles = imgFileDir.listFiles();
                if (imgFiles != null && imgFiles.length > 0) {
                    for (File imgFile : imgFiles) {
                        if (imgFile.delete()) {
                            deletedImageCount++;
                        }
                    }
                }
            }
            return deletedFeatureCount > deletedImageCount ? deletedImageCount : deletedFeatureCount;
        }
    }
    //endregion

    //region #Face操作

    /**
     * 用于预览时注册人脸
     *
     * @param context  上下文对象
     * @param nv21     NV21数据
     * @param width    NV21宽度
     * @param height   NV21高度
     * @param faceInfo {@link FaceEngine#detectFaces(byte[], int, int, int, List)}获取的人脸信息
     * @param name     保存的名字，若为空则使用时间戳
     * @return 是否注册成功
     */
    public boolean registerNv21(Context context, byte[] nv21, int width, int height, FaceInfo faceInfo, String name) {
        synchronized (this) {
            if (faceEngine == null || context == null || nv21 == null || width % 4 != 0 || nv21.length != width * height * 3 / 2) {
                Log.e(TAG, "registerNv21: invalid params");
                return false;
            }

            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            //特征存储的文件夹
            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() && !featureDir.mkdirs()) {
                Log.e(TAG, "registerNv21: can not create feature directory");
                return false;
            }
            //图片存储的文件夹
            File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (!imgDir.exists() && !imgDir.mkdirs()) {
                Log.e(TAG, "registerNv21: can not create image directory");
                return false;
            }
            FaceFeature faceFeature = new FaceFeature();
            //特征提取
            int code = faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfo, ExtractType.REGISTER, MaskInfo.NOT_WORN, faceFeature);
            if (code != ErrorInfo.MOK) {
                Log.e(TAG, "registerNv21: extractFaceFeature failed , code is " + code);
                return false;
            } else {

                String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
                try {
                    // 保存注册结果（注册图、特征数据）
                    // 为了美观，扩大rect截取注册图
                    Rect cropRect = getBestRect(width, height, faceInfo.getRect());
                    if (cropRect == null) {
                        Log.e(TAG, "registerNv21: cropRect is null!");
                        return false;
                    }

                    cropRect.left &= ~3;
                    cropRect.top &= ~3;
                    cropRect.right &= ~3;
                    cropRect.bottom &= ~3;

                    File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);


                    // 创建一个头像的Bitmap，存放旋转结果图
                    Bitmap headBmp = getHeadImage(nv21, width, height, faceInfo.getOrient(), cropRect, ArcSoftImageFormat.NV21);

                    FileOutputStream fosImage = new FileOutputStream(file);
                    headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                    fosImage.close();


                    FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                    fosFeature.write(faceFeature.getFeatureData());
                    fosFeature.close();

                    //内存中的数据同步
                    if (faceRegisterInfoList == null) {
                        faceRegisterInfoList = new ArrayList<>();
                    }
                    faceRegisterInfoList.add(new FaceRegisterInfo(faceFeature.getFeatureData(), userName));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

    }

    /**
     * 用于注册照片人脸
     *
     * @param context 上下文对象
     * @param bgr24   bgr24数据
     * @param width   bgr24宽度
     * @param height  bgr24高度
     * @param name    保存的名字，若为空则使用时间戳
     * @return 是否注册成功
     */
    public boolean registerBgr24(Context context, byte[] bgr24, int width, int height, String name) {
        synchronized (this) {
            if (faceEngine == null || context == null || bgr24 == null || width % 4 != 0 || bgr24.length != width * height * 3) {
                Log.e(TAG, "registerBgr24:  invalid params");
                return false;
            }

            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            //特征存储的文件夹
            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() && !featureDir.mkdirs()) {
                Log.e(TAG, "registerBgr24: can not create feature directory");
                return false;
            }
            //图片存储的文件夹
            File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (!imgDir.exists() && !imgDir.mkdirs()) {
                Log.e(TAG, "registerBgr24: can not create image directory");
                return false;
            }
            //人脸检测
            List<FaceInfo> faceInfoList = new ArrayList<>();
            int code = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
            if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                FaceFeature faceFeature = new FaceFeature();

                //特征提取
                code = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), ExtractType.REGISTER, MaskInfo.UNKNOWN, faceFeature);
                String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
                try {
                    //保存注册结果（注册图、特征数据）
                    if (code == ErrorInfo.MOK) {
                        //为了美观，扩大rect截取注册图
                        Rect cropRect = getBestRect(width, height, faceInfoList.get(0).getRect());
                        if (cropRect == null) {
                            Log.e(TAG, "registerBgr24: cropRect is null");
                            return false;
                        }

                        cropRect.left &= ~3;
                        cropRect.top &= ~3;
                        cropRect.right &= ~3;
                        cropRect.bottom &= ~3;

                        File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);
                        FileOutputStream fosImage = new FileOutputStream(file);


                        // 创建一个头像的Bitmap，存放旋转结果图
                        Bitmap headBmp = getHeadImage(bgr24, width, height, faceInfoList.get(0).getOrient(), cropRect, ArcSoftImageFormat.BGR24);
                        // 保存到本地
                        headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                        fosImage.close();

                        // 保存特征数据
                        FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                        fosFeature.write(faceFeature.getFeatureData());
                        fosFeature.close();

                        // 内存中的数据同步
                        if (faceRegisterInfoList == null) {
                            faceRegisterInfoList = new ArrayList<>();
                        }
                        faceRegisterInfoList.add(new FaceRegisterInfo(faceFeature.getFeatureData(), userName));
                        return true;
                    } else {
                        Log.e(TAG, "registerBgr24: extract face feature failed, code is " + code);
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                Log.e(TAG, "registerBgr24: no face detected, code is " + code);
                return false;
            }
        }

    }

    /**
     * 截取合适的头像并旋转，保存为注册头像
     *
     * @param originImageData 原始的BGR24数据
     * @param width           BGR24图像宽度
     * @param height          BGR24图像高度
     * @param orient          人脸角度
     * @param cropRect        裁剪的位置
     * @param imageFormat     图像格式
     * @return 头像的图像数据
     */
    public Bitmap getHeadImage(byte[] originImageData, int width, int height, int orient, Rect cropRect, ArcSoftImageFormat imageFormat) {
        byte[] headImageData = ArcSoftImageUtil.createImageData(cropRect.width(), cropRect.height(), imageFormat);
        int cropCode = ArcSoftImageUtil.cropImage(originImageData, headImageData, width, height, cropRect, imageFormat);
        if (cropCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("crop image failed, code is " + cropCode);
        }

        //判断人脸旋转角度，若不为0度则旋转注册图
        byte[] rotateHeadImageData = null;
        int rotateCode;
        int cropImageWidth;
        int cropImageHeight;
        // 90度或270度的情况，需要宽高互换
        if (orient == FaceEngine.ASF_OC_90 || orient == FaceEngine.ASF_OC_270) {
            cropImageWidth = cropRect.height();
            cropImageHeight = cropRect.width();
        } else {
            cropImageWidth = cropRect.width();
            cropImageHeight = cropRect.height();
        }
        ArcSoftRotateDegree rotateDegree = null;
        switch (orient) {
            case FaceEngine.ASF_OC_90:
                rotateDegree = ArcSoftRotateDegree.DEGREE_270;
                break;
            case FaceEngine.ASF_OC_180:
                rotateDegree = ArcSoftRotateDegree.DEGREE_180;
                break;
            case FaceEngine.ASF_OC_270:
                rotateDegree = ArcSoftRotateDegree.DEGREE_90;
                break;
            case FaceEngine.ASF_OC_0:
            default:
                rotateHeadImageData = headImageData;
                break;
        }
        // 非0度的情况，旋转图像
        if (rotateDegree != null) {
            rotateHeadImageData = new byte[headImageData.length];
            rotateCode = ArcSoftImageUtil.rotateImage(headImageData, rotateHeadImageData, cropRect.width(), cropRect.height(), rotateDegree, imageFormat);
            if (rotateCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("rotate image failed, code is " + rotateCode);
            }
        }
        // 将创建一个Bitmap，并将图像数据存放到Bitmap中
        Bitmap headBmp = Bitmap.createBitmap(cropImageWidth, cropImageHeight, Bitmap.Config.RGB_565);
        if (ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, imageFormat) != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("failed to transform image data to bitmap");
        }
        return headBmp;
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param width   图像宽度
     * @param height  图像高度
     * @param srcRect 原Rect
     * @return 调整后的Rect
     */
    private static Rect getBestRect(int width, int height, Rect srcRect) {
        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);

        // 原rect边界已溢出宽高的情况
        int maxOverFlow = Math.max(-rect.left, Math.max(-rect.top, Math.max(rect.right - width, rect.bottom - height)));
        if (maxOverFlow >= 0) {
            rect.inset(maxOverFlow, maxOverFlow);
            return rect;
        }

        // 原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;

        // 若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0 && rect.right + padding < width && rect.top - padding > 0 && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }
        rect.inset(-padding, -padding);
        return rect;
    }

    public Bitmap getHeadBitmap(byte[] nv21, int width, int height, FaceInfo faceInfo) {
        // 保存注册结果（注册图、特征数据）
        // 为了美观，扩大rect截取注册图
        Rect cropRect = getBestRect(width, height, faceInfo.getRect());
        if (cropRect == null) {
            Log.e(TAG, "registerNv21: cropRect is null!");
        }
        cropRect.left &= ~3;
        cropRect.top &= ~3;
        cropRect.right &= ~3;
        cropRect.bottom &= ~3;
        Bitmap headBmp = FaceServer.getInstance().getHeadImage(nv21, width, height,
                faceInfo.getOrient(), cropRect, ArcSoftImageFormat.NV21);
        return headBmp;
    }
    //endregion

    //region #特征值操作
    public void insert(String userName, byte[] faceFeature) {
        faceRegisterInfoList.add(new FaceRegisterInfo(faceFeature, userName));
    }

    public FaceRegisterInfo getFaceFeatureForBitmap(Context context, Bitmap bitmap, String name) {
        synchronized (this) {
            bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
            if (bitmap == null) {
                Log.d(TAG, "getFaceFeatureForBitmap: bitmap is null");
                return null;
            }
            byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
            int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
            if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                return null;
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (faceEngine == null || context == null || width % 4 != 0 || height % 4 != 0) {
                Log.e(TAG, "registerBgr24:  invalid params");
                return null;
            }

            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            //特征存储的文件夹
            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() && !featureDir.mkdirs()) {
                Log.e(TAG, "registerBgr24: can not create feature directory");
                return null;
            }
            //图片存储的文件夹
            File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (!imgDir.exists() && !imgDir.mkdirs()) {
                Log.e(TAG, "registerBgr24: can not create image directory");
                return null;
            }
            //人脸检测
            List<FaceInfo> faceInfoList = new ArrayList<>();
            int code = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
            if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                FaceFeature faceFeature = new FaceFeature();

                //特征提取
                code = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), ExtractType.REGISTER, MaskInfo.UNKNOWN, faceFeature);
                String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
                try {
                    //保存注册结果（注册图、特征数据）
                    if (code == ErrorInfo.MOK) {
                        //为了美观，扩大rect截取注册图
                        Rect cropRect = getBestRect(width, height, faceInfoList.get(0).getRect());
                        if (cropRect == null) {
                            Log.e(TAG, "registerBgr24: cropRect is null");
                            return null;
                        }

                        cropRect.left &= ~3;
                        cropRect.top &= ~3;
                        cropRect.right &= ~3;
                        cropRect.bottom &= ~3;

                        File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);
                        FileOutputStream fosImage = new FileOutputStream(file);


                        // 创建一个头像的Bitmap，存放旋转结果图
                        Bitmap headBmp = getHeadImage(bgr24, width, height, faceInfoList.get(0).getOrient(), cropRect, ArcSoftImageFormat.BGR24);
                        // 保存到本地
                        headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                        fosImage.close();

                        // 保存特征数据
                        FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                        fosFeature.write(faceFeature.getFeatureData());
                        fosFeature.close();

                        // 内存中的数据同步
                        if (faceRegisterInfoList == null) {
                            faceRegisterInfoList = new ArrayList<>();
                        }
                        FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo(faceFeature.getFeatureData(), userName);
                        faceRegisterInfoList.add(faceRegisterInfo);
                        return faceRegisterInfo;
                    } else {
                        Log.e(TAG, "registerBgr24: extract face feature failed, code is " + code);
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                Log.e(TAG, "registerBgr24: no face detected, code is " + code);
                return null;
            }
        }

    }

    public boolean isExistFeatureForById(Context context, String id) {
        if (faceRegisterInfoList == null) {
            faceRegisterInfoList = new ArrayList<>();
        }
        if (ROOT_PATH == null) {
            ROOT_PATH = context.getFilesDir().getAbsolutePath();
        }
        //特征存储的文件夹
        File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
        if (!featureDir.exists() && !featureDir.mkdirs()) {
            Log.e(TAG, "registerBgr24: can not create feature directory");
            return false;
        }
        //图片存储的文件夹
        File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
        if (!imgDir.exists() && !imgDir.mkdirs()) {
            Log.e(TAG, "registerBgr24: can not create image directory");
            return false;
        }
        for (int i = 0; i < faceRegisterInfoList.size(); i++) {
            if (faceRegisterInfoList.get(i).getName().equals(id)) {
                //File featurefile = new File(featureDir + File.separator + faceRegisterInfoList.get(i).getName());
                File imagefile = new File(imgDir + File.separator + faceRegisterInfoList.get(i).getName() + IMG_SUFFIX);
                return imagefile.exists();
            }
        }
        return false;
    }

    public void deleteFeatureForById(Context context, String id) {
        if (faceRegisterInfoList == null) {
            faceRegisterInfoList = new ArrayList<>();
        }
        if (ROOT_PATH == null) {
            ROOT_PATH = context.getFilesDir().getAbsolutePath();
        }
        //特征存储的文件夹
        File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
        if (!featureDir.exists() && !featureDir.mkdirs()) {
            Log.e(TAG, "registerBgr24: can not create feature directory");
        }
        //图片存储的文件夹
        File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
        if (!imgDir.exists() && !imgDir.mkdirs()) {
            Log.e(TAG, "registerBgr24: can not create image directory");
        }
        int index = -1;
        for (int i = 0; i < faceRegisterInfoList.size(); i++) {
            if (faceRegisterInfoList.get(i).getName().equals(id)) {
                //faceRegisterInfoList.remove(faceRegisterInfoList.get(i));
//                faceRegisterInfoList.remove(i);
                index = i;
                Log.e(TAG, "registerBgr24: featurefile directory" +
                        featureDir + File.separator + faceRegisterInfoList.get(i).getName());
                Log.e(TAG, "registerBgr24: imagefile image directory" +
                        imgDir + File.separator + faceRegisterInfoList.get(i).getName() + IMG_SUFFIX);
                //特征值
                File featurefile = new File(featureDir + File.separator + faceRegisterInfoList.get(i).getName());
                if (featurefile.exists()) {
                    UtilKFile.deleteFile(featurefile);
                    //FileUtils.delete(featurefile);
                }
                //人脸图像
                File imagefile = new File(imgDir + File.separator + faceRegisterInfoList.get(i).getName() + IMG_SUFFIX);
                if (imagefile.exists()) {
                    UtilKFile.deleteFile(imagefile);
                    //FileUtils.delete(imagefile);
                }
            }
        }
        if (index != -1) {
            faceRegisterInfoList.remove(index);
        }
    }

    //是否正在搜索人脸，保证搜索操作单线程进行
    private boolean isProcessing = false;

    /**
     * 在特征库中搜索
     *
     * @param faceFeature 传入特征数据
     * @return 比对结果
     */
    public CompareInfo getTopOfFaceLib(FaceFeature faceFeature) {
        if (faceEngine == null || isProcessing || faceFeature == null || faceRegisterInfoList == null || faceRegisterInfoList.size() == 0) {
            return null;
        }
        FaceFeature tempFaceFeature = new FaceFeature();
        FaceSimilar faceSimilar = new FaceSimilar();
        float maxSimilar = 0;
        int maxSimilarIndex = -1;
        isProcessing = true;
        for (int i = 0; i < faceRegisterInfoList.size(); i++) {
            tempFaceFeature.setFeatureData(faceRegisterInfoList.get(i).getFeatureData());
            faceEngine.compareFaceFeature(faceFeature, tempFaceFeature, faceSimilar);
            if (faceSimilar.getScore() > maxSimilar) {
                maxSimilar = faceSimilar.getScore();
                maxSimilarIndex = i;
            }
        }
        isProcessing = false;
        if (maxSimilarIndex != -1) {
            Log.i(TAG, "getTopOfFaceLib: already search one person");
            return new CompareInfo(0, faceRegisterInfoList.get(maxSimilarIndex).getName(), maxSimilar);
        }else {
            Log.i(TAG, "getTopOfFaceLib: no person");
            return null;
        }
    }

    /**
     * 在特征库中搜索
     *
     * @param faceFeature1 传入特征数据
     * @param faceFeature2 传入特征数据
     * @return 比对结果
     */
    public float compareFeature(FaceFeature faceFeature1, FaceFeature faceFeature2) {
        if (faceEngine == null || isProcessing || faceFeature1 == null || faceFeature2 == null || faceRegisterInfoList == null || faceRegisterInfoList.size() == 0) {
            return 0;
        }
        FaceSimilar faceSimilar = new FaceSimilar();
        float maxSimilar = 0;
        isProcessing = true;
        faceEngine.compareFaceFeature(faceFeature1, faceFeature2, faceSimilar);
        if (faceSimilar.getScore() > maxSimilar) {
            maxSimilar = faceSimilar.getScore();
        }
        isProcessing = false;
        return maxSimilar;
    }
    //endregion
}
