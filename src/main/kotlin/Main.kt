package performancetests

import performancetests.performancetests.mergesort.MergeSort
import performancetests.performancetests.mergesort.MergeSortCoroutines
import java.io.File
import kotlin.system.measureTimeMillis

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() {

    val algorithm = "coroutines"
    val listLength = "50000000"
    val chunkSize = listLength.toInt()/16

    val fileName = "src/main/kotlin/performancetests/mergesort/List$listLength.txt"
    val line = File(fileName).readText().trim()

    val array = line.split(",").map { it.toInt() }.toIntArray()

    println("Import done!")


    if (algorithm == "single") {
        val time = measureTimeMillis {
            MergeSort().mergeSort(array)
        }
        println("Time Single: $time")
    } else if (algorithm == "coroutines") {
        val time = measureTimeMillis {
            MergeSortCoroutines().mergeSort(array, chunkSize)
        }
        println("Time Coroutines: $time")
    } else {
        println("Unknown algorithm, fallback: single")
        val time = measureTimeMillis {
            MergeSort().mergeSort(array)
        }
        println("Time Single: $time")
    }

   //println(array.contentToString())

}