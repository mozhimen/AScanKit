package com.mozhimen.scank.edge.opencv.interfaces;

import android.graphics.Bitmap;

import com.mozhimen.scank.edge.opencv.enums.ScanHint;

/**
 * Interface between activity and surface view
 */

public interface IScanner {
    void displayHint(ScanHint scanHint);
    void onPictureClicked(Bitmap bitmap);
}
