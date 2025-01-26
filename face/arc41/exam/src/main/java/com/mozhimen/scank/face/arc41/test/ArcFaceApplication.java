package com.mozhimen.scank.face.arc41.test;

import android.app.Application;

public class ArcFaceApplication extends Application {
    private static ArcFaceApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        //initCrashDumper();
    }

//    private void initCrashDumper() {
//        XCrash.InitParameters initParameters = new XCrash.InitParameters();
//        File dir = new File(DebugInfoDumper.CRASH_LOG_DIR);
//        if (dir.isFile()){
//            dir.delete();
//        }
//        if (!dir.exists()){
//            dir.mkdirs();
//        }
//        initParameters.setLogDir(DebugInfoDumper.CRASH_LOG_DIR);
//        XCrash.init(application, initParameters);
//    }

    @Override
    public void onTerminate() {
        application = null;
        super.onTerminate();
    }

    public static ArcFaceApplication getApplication() {
        return application;
    }
}
