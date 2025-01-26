package com.mozhimen.scank.face.arc41.temps

import android.util.Log
import com.mozhimen.kotlin.utilk.kotlin.collections.joinT2listIgnoreRepeat
import com.mozhimen.scank.face.arc41.bases.BaseExtractTask
import com.mozhimen.scank.face.arc41.helpers.UtilKExtract

/**
 * @ClassName ExtractMultipleTask
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/3/5 0:44
 * @Version 1.0
 */
class ExtractMultipleTask(private val _extractList: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>, private val _localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) : BaseExtractTask() {
    override fun run() {
        if (_extractList.isEmpty()) return
        Log.d(TAG, "run: extractMultiple start")
        Log.d(TAG, "run: extractMultiple size ${_extractList.size} names ${_extractList.joinT2listIgnoreRepeat { it.personName }}")
        _extractList.forEach {
            Log.d(TAG, "run: extractMultiple person $it")
            UtilKExtract.extractAndInsertDB(it, _localPersons)
        }
        Log.d(TAG, "run: extractMultiple finish")
    }
}