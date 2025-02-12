package com.mozhimen.scank.face.arc42.test.ui.model;

import android.hardware.Camera;

public class PreviewConfig {
    /**
     * 默认的可见光相机ID
     */
    public static final int DEFAULT_RGB_CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    /**
     * 默认的红外相机ID
     */
    public static final int DEFAULT_IR_CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private int rgbCameraId;
    private int irCameraId;
    private int rgbAdditionalDisplayOrientation;
    private int irAdditionalDisplayOrientation;

    public PreviewConfig(int rgbCameraId, int irCameraId, int rgbAdditionalDisplayOrientation, int irAdditionalDisplayOrientation) {
        this.rgbCameraId = rgbCameraId;
        this.irCameraId = irCameraId;
        this.rgbAdditionalDisplayOrientation = rgbAdditionalDisplayOrientation;
        this.irAdditionalDisplayOrientation = irAdditionalDisplayOrientation;
    }

    public int getRgbCameraId() {
        return rgbCameraId;
    }

    public int getIrCameraId() {
        return irCameraId;
    }

    public int getRgbAdditionalDisplayOrientation() {
        return rgbAdditionalDisplayOrientation;
    }

    public int getIrAdditionalDisplayOrientation() {
        return irAdditionalDisplayOrientation;
    }

    public void setRgbCameraId(int rgbCameraId) {
        this.rgbCameraId = rgbCameraId;
    }

    public void setIrCameraId(int irCameraId) {
        this.irCameraId = irCameraId;
    }

    public void setRgbAdditionalDisplayOrientation(int rgbAdditionalDisplayOrientation) {
        this.rgbAdditionalDisplayOrientation = rgbAdditionalDisplayOrientation;
    }

    public void setIrAdditionalDisplayOrientation(int irAdditionalDisplayOrientation) {
        this.irAdditionalDisplayOrientation = irAdditionalDisplayOrientation;
    }
}
