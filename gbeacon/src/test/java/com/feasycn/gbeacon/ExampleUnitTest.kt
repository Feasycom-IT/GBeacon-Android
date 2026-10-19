package com.feasycn.gbeacon

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun a(){
        val a = arrayOf(1,3,5,7,0)
        val b = arrayOf(1,3)
        for ((x,y) in a.withIndex()) {
            if (b.contains(y)){

            }else {
                println(y)
            }
        }

    }

    @Test
    fun b(){
        val b: Byte = 0xb2.toByte()
        println(b)
        println(b.toInt())
        println(b.toUInt())
    }
    // HHmmss
    @Test
    fun c(){
        var currentTimeMillis = System.currentTimeMillis()
        val formatyyyy = SimpleDateFormat("yyyyMMddHHmmss")
        System.out.println("2019-12-31 转 yyyy/MM/dd 格式: " + formatyyyy.format(currentTimeMillis))
    }


    @Test
    fun abc(){


        /*var range = Range(500, 700)
        // println(400 in range)
        println(600 in range)*/


    }


}