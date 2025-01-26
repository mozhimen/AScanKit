package com.mozhimen.scank.face.arc41

import android.graphics.Bitmap
import android.util.Log
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.taskk.bases.BaseWakeBefDestroyTaskK
import com.mozhimen.basick.taskk.executor.TaskKExecutor
import com.mozhimen.kotlin.utilk.kotlin.collections.containsBy
import com.mozhimen.kotlin.utilk.kotlin.collections.joinT2listIgnoreRepeat
import com.mozhimen.scank.face.arc41.temps.DeleteAllTask
import com.mozhimen.scank.face.arc41.temps.DeleteListTask
import com.mozhimen.scank.face.arc41.temps.ExtractMultipleTask
import com.mozhimen.scank.face.arc41.temps.ExtractSingleTask
import java.util.*

/**
 * @ClassName TaskKPullFaceInfo
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/11 11:58
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
class TaskKExtract : BaseWakeBefDestroyTaskK() {

    private val _tempNeedAddList = LinkedList<com.mozhimen.scank.face.arc.basic.db.DBPerson>()//远端有,本地没有
    private val _tempRepeatList = LinkedList<String>()//重复的
    private val _tempNeedDeleteList = LinkedList<com.mozhimen.scank.face.arc.basic.db.DBPerson>()//本地有,远端没有

    fun analyzePersons(
        remotePersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>,
        localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>
    ) {
        clearList()
        if (remotePersons.isEmpty() && localPersons.isNotEmpty()) {        //远端没有本地有, 说明远端清空, 本地同步清空
            _tempNeedDeleteList.addAll(localPersons)
            this.deleteAll()
            Log.d(TAG, "analyzePersons: deleteAll")
        } else if (localPersons.isEmpty() && remotePersons.isNotEmpty()) {//远端有, 本地没有, 本地同步加入
            remotePersons.forEach {
                _tempNeedAddList.add(com.mozhimen.scank.face.arc.basic.db.DBPerson(it.personUuid, it.personName, it.personUrl, it.personAge, it.personSex))
            }
            this.extractMultiple(_tempNeedAddList, localPersons)
            Log.d(TAG, "analyzePersons: addAll")
        } else if (localPersons.isNotEmpty() && remotePersons.isNotEmpty()) {
            //重复的部分->比较迷糊->后期再进一步筛选
            remotePersons.forEach { remote ->
                localPersons.forEach { local ->
                    if (remote.personUuid == local.personUuid) {
                        _tempRepeatList.add(remote.personUuid)
                    }
                }
            }
            //先得到需要删除的人员
            localPersons.forEach {
                if (!_tempRepeatList.containsBy { uuid -> uuid == it.personUuid }) {
                    _tempNeedDeleteList.add(it)
                }
            }
            this.deleteList(_tempNeedDeleteList)
            //再得到需要加入的部分的人员包括(本地库没有的+远端更改的人员,包括在repeat部分)
            remotePersons.forEach {
                if (!_tempRepeatList.containsBy { uuid -> uuid == it.personUuid }) {
                    _tempNeedAddList.add(it)
                }
            }
            for (uuid in _tempRepeatList) {
                val localPerson = localPersons.find { it.personUuid == uuid }
                val remotePerson = remotePersons.find { it.personUuid == uuid } ?: continue
                if (localPerson == null) {
                    _tempNeedAddList.add(remotePerson)
                    continue
                }
                if (localPerson.personName != remotePerson.personName || localPerson.personUrl != remotePerson.personUrl || localPerson.personAge != remotePerson.personAge || localPerson.personSex != remotePerson.personSex) {
                    _tempNeedAddList.add(remotePerson)
                }
            }
            //最后, 提取并入库
            this.extractMultiple(_tempNeedAddList, localPersons)
            Log.d(TAG, "analyzePersons: add multiply")
        }
        Log.d(TAG, "analyzePersons: tempNeedAddList ${_tempNeedAddList.joinT2listIgnoreRepeat {  it.personName }}")
        Log.d(TAG, "analyzePersons: tempRepeatList $_tempRepeatList")
        Log.d(TAG, "analyzePersons: tempNeedDeleteList ${_tempNeedDeleteList.joinT2listIgnoreRepeat { it.personName }}")
    }

    fun analyzePerson(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, bitmap: Bitmap, localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) {
        if (localPersons.containsBy { it.personUuid == dbPerson.personUuid }) {
            val localPerson = localPersons.find { p -> p.personUuid == dbPerson.personUuid }
            if (localPerson == null) {
                this.extractSingle(dbPerson, bitmap, localPersons)
            } else if (!localPerson.analyzeStatus || localPerson.personSex != dbPerson.personSex ||
                localPerson.personAge != dbPerson.personSex || localPerson.personName != dbPerson.personName ||
                localPerson.personUrl != dbPerson.personUrl
            ) {
                this.extractSingle(dbPerson, bitmap, localPersons)
            } else {
                Log.w(TAG, "analyzePerson: dont nee extract")
            }
        } else {
            this.extractSingle(dbPerson, bitmap, localPersons)
        }
    }

    private fun extractSingle(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson, bitmap: Bitmap, localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) {
        TaskKExecutor.execute(TAG, runnable = ExtractSingleTask(dbPerson, bitmap, localPersons))
    }

    private fun extractMultiple(extractList: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>, localPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) {
        if (extractList.isEmpty()) return
        TaskKExecutor.execute(TAG, runnable = ExtractMultipleTask(extractList, localPersons))
    }

    private fun deleteList(deleteList: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>) {
        if (deleteList.isEmpty()) return
        TaskKExecutor.execute(TAG, runnable = DeleteListTask(deleteList))
    }

    private fun deleteAll() {
        TaskKExecutor.execute(TAG, runnable = DeleteAllTask())
    }

    private fun clearList(){
        _tempNeedAddList.clear()
        _tempRepeatList.clear()
        _tempNeedDeleteList.clear()
    }

    override fun isActive(): Boolean {
        return _tempNeedAddList.isNotEmpty() || _tempRepeatList.isNotEmpty() || _tempNeedDeleteList.isNotEmpty()
    }

    override fun cancel() {
        clearList()
    }
}