package com.mozhimen.scank.face.arc41.basic.helpers

import android.content.Context
import com.arcsoft.face.FaceInfo
import com.mozhimen.kotlin.utilk.android.app.UtilKApplication
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationReflect
import java.io.File

/**
 * @ClassName TrackUtil
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:46
 * @Version 1.0
 */
object ScanKArcFaceUtil {
    private val _context: Context = UtilKApplicationReflect.instance.get()

    /**
     * 是否是相同人脸
     * @param faceInfo1 FaceInfo
     * @param faceInfo2 FaceInfo
     * @return Boolean
     */
    @JvmStatic
    fun isSamePerson(faceInfo1: FaceInfo, faceInfo2: FaceInfo): Boolean {
        return faceInfo1.faceId == faceInfo2.faceId
    }

    /**
     * 若需要多人脸搜索，删除此行代码
     * @param faceInfos MutableList<FaceInfo>?
     */
    @JvmStatic
    fun keepMaxFace(faceInfos: MutableList<FaceInfo>?) {
        if (faceInfos == null || faceInfos.size <= 1) return
        var maxFaceInfo = faceInfos[0]
        for (faceInfo in faceInfos) {
            if (faceInfo.rect.width() > maxFaceInfo.rect.width()) {
                maxFaceInfo = faceInfo
            }
        }
        faceInfos.clear()
        faceInfos.add(maxFaceInfo)
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     * @param libraries Array<String>
     * @return Boolean
     */
    @JvmStatic
    fun checkSoLibrary(libraries: Array<String>): Boolean {
        val files = File(_context.applicationInfo.nativeLibraryDir).listFiles()
        if (files == null || files.isEmpty()) {
            return false
        }
        val libraryNameList: MutableList<String> = ArrayList()
        for (file in files) {
            libraryNameList.add(file.name)
        }
        var exists = true
        for (library in libraries) {
            exists = exists and libraryNameList.contains(library)
        }
        return exists
    }
}