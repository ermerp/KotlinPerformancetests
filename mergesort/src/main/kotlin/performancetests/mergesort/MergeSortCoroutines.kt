package performancetests.mergesort

import kotlinx.coroutines.*

class MergeSortCoroutines {

    suspend fun runMergeSort(array: IntArray, maxDepth: Int) {
        val tempArray = IntArray(array.size) // Temporäres Array für den Merge-Schritt
        mergeSort(array, tempArray, 0, array.size - 1, 0, maxDepth)
    }

    private suspend fun mergeSort(
        array: IntArray,
        tempArray: IntArray,
        left: Int,
        right: Int,
        currentDepth: Int,
        maxDepth: Int
    ) {
        if (left >= right) return

        val mid = (left + right) / 2

        if (currentDepth < maxDepth) {
            val deferred1 = GlobalScope.async {
                mergeSort(array, tempArray, left, mid, currentDepth + 1, maxDepth)
            }
            val deferred2 = GlobalScope.async {
                mergeSort(array, tempArray, mid + 1, right, currentDepth + 1, maxDepth)
            }
            deferred1.await()
            deferred2.await()
        } else {
            mergeSort(array, tempArray, left, mid, currentDepth + 1, maxDepth)
            mergeSort(array, tempArray, mid + 1, right, currentDepth + 1, maxDepth)
        }

        merge(array, tempArray, left, mid, right)
    }

    private fun merge(array: IntArray, tempArray: IntArray, left: Int, mid: Int, right: Int) {
        for (i in left..right) {
            tempArray[i] = array[i]
        }

        var i = left
        var j = mid + 1
        var k = left

        while (i <= mid && j <= right) {
            if (tempArray[i] <= tempArray[j]) {
                array[k++] = tempArray[i++]
            } else {
                array[k++] = tempArray[j++]
            }
        }

        while (i <= mid) {
            array[k++] = tempArray[i++]
        }
    }
}
