package performancetests.performancetests.mergesort

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MergeSortCoroutines : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun runAllMergeSort(arrays: List<IntArray>) {

        val tasks = mutableListOf<Job>();

        arrays.forEach { array ->
            tasks.add(coroutineScope {
                launch {
                    mergeSort(array)
                }
            })
        }

        tasks.forEach { it.join() }
    }

    private suspend fun mergeSort(array: IntArray) {
        if (array.size <= 1) {
            return
        }

        val mid = array.size / 2
        val leftArray = IntArray(mid)
        val rightArray = IntArray(array.size - mid)

        System.arraycopy(array, 0, leftArray, 0, mid)
        System.arraycopy(array, mid, rightArray, 0, array.size - mid)

//        coroutineScope {
//            val left = async { mergeSort(leftArray) }
//            val right = async { mergeSort(rightArray) }
//            left.await()
//            right.await()
//            merge(leftArray, rightArray, array)
//        }

        mergeSort(leftArray)
        mergeSort(rightArray)
        merge(leftArray, rightArray, array)
    }

    private fun merge(
        leftArray: IntArray,
        rightArray: IntArray,
        array: IntArray
    ) {
        var i = 0
        var j = 0
        var k = 0

        while (i < leftArray.size && j < rightArray.size) {
            if (leftArray[i] <= rightArray[j]) {
                array[k++] = leftArray[i++]
            } else {
                array[k++] = rightArray[j++]
            }
        }
        while (i < leftArray.size) {
            array[k++] = leftArray[i++]
        }
        while (j < rightArray.size) {
            array[k++] = rightArray[j++]
        }
    }
}