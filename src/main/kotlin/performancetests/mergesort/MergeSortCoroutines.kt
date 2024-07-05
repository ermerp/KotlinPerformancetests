package performancetests.performancetests.mergesort

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class MergeSortCoroutines {

    public suspend fun mergeSort(array: IntArray, chunkSize: Int) {
        if (array.size <= 1) {
            return
        }

        val mid = array.size / 2
        val leftArray = IntArray(mid)
        val rightArray = IntArray(array.size - mid)

        System.arraycopy(array, 0, leftArray, 0, mid)
        System.arraycopy(array, mid, rightArray, 0, array.size - mid)

        if(mid >= chunkSize) {
            val deferred1 = GlobalScope.async {
                mergeSort(leftArray, chunkSize)
            }
            val deferred2 = GlobalScope.async {
                mergeSort(rightArray, chunkSize)
            }
            deferred1.await()
            deferred2.await()
        } else {
            mergeSort(leftArray, chunkSize)
            mergeSort(rightArray, chunkSize)
        }

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