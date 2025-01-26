package com.mozhimen.scank.face.arc41.helpers

import android.graphics.Bitmap
import android.util.Log
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.kotlin.UtilKStrUrl
import com.mozhimen.scank.face.arc.basic.mos.FaceRegisterInfo
import com.mozhimen.scank.face.arc41.basic.helpers.ScanKArcFaceMgr

/**
 * @ClassName UtilKExtract
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/3/4 23:45
 * @Version 1.0
 */
object UtilKExtract {
    private const val TAG = "UtilKExtract>>>>>"

    @JvmStatic
    fun extractAndInsertDB(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) {
        Log.d(TAG, "extractAndInsertDB: dbPerson $dbPerson ")
        val tempDBPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson? = localPersons.find { it.personUuid == dbPerson.personUuid }
        val dontExtract: Boolean =
            ScanKArcFaceMgr.instance.isPersonFeatureExistById(dbPerson.personUuid) && tempDBPerson != null && tempDBPerson.analyzeStatus && dbPerson.personUrl == tempDBPerson.personUrl
        Log.d(TAG, "extractAndInsertDB: dontExtract $dontExtract")
        if (!dontExtract) {
            if (tempDBPerson == null || ScanKArcFaceMgr.instance.isPersonFeatureExistById(dbPerson.personUuid)) {
                Log.d(TAG, "extractAndInsertDB: hasn't extract -> extract")
                extractPersonFeature(dbPerson)
            } else if (!tempDBPerson.analyzeStatus) {
                Log.d(TAG, "extractAndInsertDB: last extract fail -> extract")
                ScanKArcFaceMgr.instance.deletePersonFeatureById(tempDBPerson.personUuid)
                extractPersonFeature(dbPerson)
            } else if (tempDBPerson.personUrl != dbPerson.personUrl) {
                Log.d(TAG, "extractAndInsertDB: image change -> extract")
                ScanKArcFaceMgr.instance.deletePersonFeatureById(tempDBPerson.personUuid)
                extractPersonFeature(dbPerson)
            } else {
                Log.w(TAG, "extractAndInsertDB: other problem -> extract no")
            }
        } else {
            if (tempDBPerson!!.personName != dbPerson.personName ||
                tempDBPerson.personAge != dbPerson.personAge ||
                tempDBPerson.personSex != dbPerson.personSex
            ) {
                insertPersonToDB(dbPerson, tempDBPerson.analyzeStatus)
                Log.d(TAG, "extractAndInsertDB: dontExtract update")
            } else {
                Log.d(TAG, "extractAndInsertDB: dontExtract")
            }
        }
    }

    @JvmStatic
    fun extractAndInsertDB(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, bitmap: Bitmap, localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) {
        Log.d(TAG, "extractAndInsertDB: dbPerson $dbPerson ")
        val tempDBPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson? = localPersons.find { it.personUuid == dbPerson.personUuid }
        val dontExtract: Boolean =
            ScanKArcFaceMgr.instance.isPersonFeatureExistById(dbPerson.personUuid) && tempDBPerson != null && tempDBPerson.analyzeStatus && dbPerson.personUrl == tempDBPerson.personUrl
        Log.d(TAG, "extractAndInsertDB: dontExtract $dontExtract")
        if (!dontExtract) {
            if (tempDBPerson == null || ScanKArcFaceMgr.instance.isPersonFeatureExistById(dbPerson.personUuid)) {
                Log.d(TAG, "extractAndInsertDB: hasn't extract -> extract")
                extractPersonFeature(dbPerson, bitmap)
            } else if (!tempDBPerson.analyzeStatus) {
                Log.d(TAG, "extractAndInsertDB: last extract fail -> extract")
                ScanKArcFaceMgr.instance.deletePersonFeatureById(tempDBPerson.personUuid)
                extractPersonFeature(dbPerson, bitmap)
            } else if (tempDBPerson.personUrl != dbPerson.personUrl) {
                Log.d(TAG, "extractAndInsertDB: image change -> extract")
                ScanKArcFaceMgr.instance.deletePersonFeatureById(tempDBPerson.personUuid)
                extractPersonFeature(dbPerson, bitmap)
            } else {
                Log.w(TAG, "extractAndInsertDB: other problem -> extract no")
            }
        } else {
            if (tempDBPerson!!.personName != dbPerson.personName ||
                tempDBPerson.personAge != dbPerson.personAge ||
                tempDBPerson.personSex != dbPerson.personSex
            ) {
                insertPersonToDB(dbPerson, tempDBPerson.analyzeStatus)
                Log.d(TAG, "extractAndInsertDB: dontExtract update")
            } else {
                Log.d(TAG, "extractAndInsertDB: dontExtract")
            }
        }
    }

    @JvmStatic
    fun extractPersonFeature(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson) {
        extractPersonFeature(dbPerson, dbPerson.personUrl.trim())
    }

    @OptIn(OPermission_INTERNET::class)
    @JvmStatic
    fun extractPersonFeature(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, imageUrl: String) {
        if (imageUrl.isEmpty() || !UtilKStrUrl.isStrUrlConnectable(imageUrl)) {
            Log.e(TAG, "extractPersonFeature: url isn't valid")
            return
        }
        extractPersonFeature(dbPerson, UtilKStrUrl.strUrl2bitmapAny(imageUrl) ?: kotlin.run {
            Log.e(TAG, "extractPersonFeature: bitmap is null")
            return
        })
    }

    @JvmStatic
    fun extractPersonFeature(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, bitmap: Bitmap) {
        try {
            val faceRegisterInfo: FaceRegisterInfo? = ScanKArcFaceMgr.instance.extractFeatureAndInsert(dbPerson.personUuid, bitmap)
            val count = if (faceRegisterInfo == null) {
                insertPersonToDB(dbPerson, false)
            } else {
                insertPersonToDB(dbPerson, true)
            }
            Log.d(TAG, "extractPersonFeature: ${if (faceRegisterInfo != null) "success" else "fail"} affect line $count")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "extractPersonFeature: extract fail ${e.message}")
        }
    }

    @JvmStatic
    fun insertPersonToDB(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, status: Boolean): Long {
        return com.mozhimen.scank.face.arc.basic.db.DBMgr.dbPersonDao.insertDBPerson(
            com.mozhimen.scank.face.arc.basic.db.DBPerson(
                dbPerson.personUuid,
                dbPerson.personName,
                dbPerson.personUrl,
                dbPerson.personAge,
                dbPerson.personSex,
                status
            )
        )
    }
}