package performancetests

import performancetests.performancetests.mergesort.MergeSort
import java.io.File
import kotlin.system.measureTimeMillis

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val fileName = "src/main/kotlin/performancetests/mergesort/Data10000x10000.txt"
    val lines = File(fileName).readLines()

    val arrays = lines.drop(2).map { line ->
        line.split(",").map { it.toInt() }.toIntArray()
    }

    val time = measureTimeMillis {
        MergeSort.runAllMergeSort(arrays)
    }

//    for (array in arrays) {
//        println(array.contentToString())
//    }

    println("Time: $time")
}