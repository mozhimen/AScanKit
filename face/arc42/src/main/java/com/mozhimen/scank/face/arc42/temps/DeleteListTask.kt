package com.mozhimen.scank.face.arc42.temps

import android.util.Log
import com.mozhimen.kotlin.utilk.kotlin.collections.joinT2listIgnoreRepeat
import com.mozhimen.scank.face.arc42.bases.BaseExtractTask
import com.mozhimen.scank.face.arc42.basic.helpers.ScanKArcFaceMgr

/**
 * @ClassName DeleteAllTask
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/3/4 23:39
 * @Version 1.0
 */
class DeleteListTask(private val _deleteList: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) : BaseExtractTask() {
    override fun run() {
        if (_deleteList.isEmpty()) return
        Log.d(TAG, "run: delete list start")
        Log.d(TAG, "run: delete list -> ${_deleteList.joinT2listIgnoreRepeat { it.personName }}")
        com.mozhimen.scank.face.arc.basic.db.DBMgr.dbPersonDao.deleteDBPersons(_deleteList)
        _deleteList.forEach {
            ScanKArcFaceMgr.instance.deletePersonFeatureById(it.personUuid)
        }
        Log.d(TAG, "run: delete list finish size ${_deleteList.size}")
    }
}