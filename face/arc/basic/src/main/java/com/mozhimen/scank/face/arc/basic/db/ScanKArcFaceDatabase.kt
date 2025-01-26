package com.mozhimen.scank.face.arc.basic.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationWrapper

/**
 * @ClassName DemoDatabase
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Version 1.0
 */
@Database(entities = [com.mozhimen.scank.face.arc.basic.db.DBPerson::class], version = 1, exportSchema = false)
abstract class ScanKArcFaceDatabase : RoomDatabase() {
    abstract val dbPersonDao: com.mozhimen.scank.face.arc.basic.db.IDBPersonDao

    companion object {
        @Volatile
        private var _db: com.mozhimen.scank.face.arc.basic.db.ScanKArcFaceDatabase =
            Room.databaseBuilder(UtilKApplicationWrapper.instance.get(), com.mozhimen.scank.face.arc.basic.db.ScanKArcFaceDatabase::class.java, "scankarcface_db").build()

        @JvmStatic
        fun get(): com.mozhimen.scank.face.arc.basic.db.ScanKArcFaceDatabase {
            return com.mozhimen.scank.face.arc.basic.db.ScanKArcFaceDatabase.Companion._db
        }
    }
}