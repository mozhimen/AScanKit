package com.mozhimen.scank.face.arc42.temps

import android.graphics.Bitmap
import android.util.Log
import com.mozhimen.scank.face.arc42.helpers.UtilKExtract
import com.mozhimen.scank.face.arc42.bases.BaseExtractTask

/**
 * @ClassName ExtractSingleTask
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/3/4 23:49
 * @Version 1.0
 */
class ExtractSingleTask(
    private val _dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson,
    private val _bitmap: Bitmap,
    private val _localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>
) : BaseExtractTask() {
    override fun run() {
        Log.d(TAG, "run: extractSingle start")
        Log.d(TAG, "run: extractSingle name ${_dbPerson.personName}")
        UtilKExtract.extractAndInsertDB(_dbPerson, _bitmap, _localPersons)
        Log.d(TAG, "run: extractSingle finish")
    }
}