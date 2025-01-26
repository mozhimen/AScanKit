package com.mozhimen.scank.face.arc42

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mozhimen.scank.face.arc42.TaskKExtract

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.mozhimen.scank_arc_face.test", appContext.packageName)
        val taskKExtract = TaskKExtract()
//        taskKExtract.analyzePersons(
//            listOf(
//                DBPerson("111", "张三", "111", "111", "111"),
//                DBPerson("222", "李四", "222", "222", "222")
//            ),
//            listOf(
//                DBPerson("111", "张四", "111", "111", "111"),
//                DBPerson("333", "333", "333", "333", "333")
//            )
//        )
//        taskKExtract.analyzePersons(
//            emptyList(),
//            listOf(
//                DBPerson("111", "111", "111", "111", "111"),
//                DBPerson("333", "333", "333", "333", "333")
//            )
//        )
        taskKExtract.analyzePersons(
            listOf(
                com.mozhimen.scank.face.arc.basic.db.DBPerson("111", "111", "111", "111", "111"),
                com.mozhimen.scank.face.arc.basic.db.DBPerson("333", "333", "333", "333", "333")
            ),
            emptyList()
        )
    }
}