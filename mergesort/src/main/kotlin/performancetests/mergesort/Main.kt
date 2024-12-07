package performancetests.mergesort

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) = runBlocking {
    val algorithm = if (args.isNotEmpty() && args[0].isNotEmpty()) args[0] else "coroutines"
    val listLength = if (args.size > 1 && args[1].isNotEmpty()) args[1].toInt() else 10000000 //30000000
    val maxDepth = if (args.size > 2 && args[2].isNotEmpty()) args[2].toInt() else 4
    val runs = if (args.size > 3 && args[3].isNotEmpty()) args[3].toInt() else 1
    val warmUpRuns = if (args.size > 4 && args[4].isNotEmpty()) args[4].toInt() else 0

    println("Kotlin - Algorithm: $algorithm, List length: $listLength, Max Depth: $maxDepth, Runs: $runs, Warm up runs: $warmUpRuns")

    val fileName = "List$listLength.txt"
    val line = File(fileName).readText().trim()

    val list = line.split(",").map { it.toInt() }.toIntArray()

    println("File imported.")

    repeat(warmUpRuns) {
        runAlgorithm(algorithm, list.clone(), maxDepth)
    }

    println("warum up runs finished")

    val time = measureTimeMillis {
        repeat(runs) {
            runAlgorithm(algorithm, list.clone(), maxDepth)
        }
    }

    println("Kotlin - $algorithm, Time: $time")

}

private suspend fun runAlgorithm(algorithm: String, list: IntArray, maxDepth: Int) {
    when (algorithm) {
        "single" -> {
            MergeSort().runMergeSort(list)
        }

        "coroutines" -> {
            MergeSortCoroutines().runMergeSort(list, maxDepth)
        }

        else -> {
            println("Unknown algorithm")
        }
    }
    //println(list.contentToString())
}