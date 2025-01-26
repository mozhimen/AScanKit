package com.mozhimen.scank.face.arc.basic.db

import androidx.room.*

/**
 * @ClassName IPersonDao
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Version 1.0
 */
@Dao
interface IDBPersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDBPersons(dbPersons: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDBPerson(dbPerson: com.mozhimen.scank.face.arc.basic.db.DBPerson): Long

    @Delete
    fun deleteDBPersons(passPersonList: List<com.mozhimen.scank.face.arc.basic.db.DBPerson>)

    @Delete
    fun deleteDBPerson(person: com.mozhimen.scank.face.arc.basic.db.DBPerson)

    @Query("DELETE From db_person")
    fun deleteAllDBPersons()

    @Update
    fun update(person: com.mozhimen.scank.face.arc.basic.db.DBPerson)

    @Query("SELECT * From db_person Where personUuid = :uuid")
    fun selectDBPersonByPersonUuid(uuid: String): com.mozhimen.scank.face.arc.basic.db.DBPerson?

    @Query("SELECT * From db_person")
    fun selectAllDBPersons(): List<com.mozhimen.scank.face.arc.basic.db.DBPerson>?
}