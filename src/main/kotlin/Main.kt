package performancetests

import kotlinx.coroutines.runBlocking
import performancetests.performancetests.mergesort.MergeSort
import performancetests.performancetests.mergesort.MergeSortCoroutines
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.time.Duration

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) = runBlocking {
    val algorithm = if (args.isNotEmpty() && args[0].isNotEmpty()) args[0] else "coroutines"
    val listLength = if (args.size > 1 && args[1].isNotEmpty()) args[1].toInt() else 30000000
    val chunkNumber = if (args.size > 2 && args[2].isNotEmpty()) args[2].toInt() else 16
    val runs = if (args.size > 3 && args[3].isNotEmpty()) args[3].toInt() else 1
    val chunkSize = listLength / chunkNumber

    println("Kotlin - Algorithm: $algorithm, List length: $listLength, Chunk number: $chunkNumber, Runs: $runs")


    val fileName = "List$listLength.txt"
    val line = File(fileName).readText().trim()

    val list = line.split(",").map { it.toInt() }.toIntArray()

    println("File imported.")

    val time = measureTimeMillis {
        repeat(runs) {
            runAlgorithm(algorithm, list.clone(), chunkSize)
        }
    }

    println("Kotlin - $algorithm, Time: $time")

   //println(array.contentToString())

}

private suspend fun runAlgorithm(algorithm: String, list: IntArray, chunkSize: Int) {
    when (algorithm) {
        "single" -> {
            MergeSort().mergeSort(list)
        }

        "coroutines" -> {
            MergeSortCoroutines().mergeSort(list, chunkSize)
        }

        else -> {
            println("Unknown algorithm")
        }
    }
}