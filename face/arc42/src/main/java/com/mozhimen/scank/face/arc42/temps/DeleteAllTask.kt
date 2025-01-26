package com.mozhimen.scank.face.arc42.temps

import android.util.Log
import com.mozhimen.scank.face.arc42.bases.BaseExtractTask
import com.mozhimen.scank.face.arc42.basic.helpers.ScanKArcFaceMgr

/**
 * @ClassName DeleteAllTask
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/3/4 23:40
 * @Version 1.0
 */
class DeleteAllTask : BaseExtractTask() {
    override fun run() {
        Log.d(TAG, "run: deleteAll start")
        ScanKArcFaceMgr.instance.deleteAllCache()
        com.mozhimen.scank.face.arc.basic.db.DBMgr.dbPersonDao.deleteAllDBPersons()
        Log.d(TAG, "run: deleteAll finish")
    }
}