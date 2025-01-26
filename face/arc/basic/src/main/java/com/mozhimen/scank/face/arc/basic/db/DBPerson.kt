package com.mozhimen.scank.face.arc.basic.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @ClassName DBPerson
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Version 1.0
 */
@Entity(tableName = "db_person")
class DBPerson(
    @PrimaryKey
    var personUuid: String,
    var personName: String,
    var personUrl: String,
    var personAge: String,
    var personSex: String,
    var analyzeStatus: Boolean = false
) {
    override fun toString(): String {
        return "DBPerson(personUuid='$personUuid', personName='$personName', personUrl='$personUrl', analyzeStatus=$analyzeStatus)"
    }
}