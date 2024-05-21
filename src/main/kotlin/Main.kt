package performancetests

import performancetests.performancetests.mergesort.MergeSort
import performancetests.performancetests.mergesort.MergeSortCoroutines
import java.io.File
import kotlin.system.measureTimeMillis

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() {

    val fileName = "src/main/kotlin/performancetests/mergesort/Data10000x10000.txt"
    val lines = File(fileName).readLines()

    val arrays = lines.drop(2).map { line ->
        line.split(",").map { it.toInt() }.toIntArray()
    }

    val arrays2 = arrays.map { it.clone() }

    println("Import done!")

    val time = measureTimeMillis {
        MergeSort().runAllMergeSort(arrays)
    }
    println("Time Single: $time")

    val time2 = measureTimeMillis {
        MergeSortCoroutines().runAllMergeSort(arrays2)
    }
    println("Time Coroutines: $time2")

//    for (array in arrays) {
//        println(array.contentToString())
//    }
//
//    for (array in arrays2) {
//        println(array.contentToString())
//    }


}