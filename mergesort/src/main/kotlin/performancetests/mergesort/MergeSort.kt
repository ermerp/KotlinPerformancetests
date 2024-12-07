package performancetests.mergesort

class MergeSort {

    fun runMergeSort(array: IntArray) {
        val tempArray = IntArray(array.size) // Temporäres Array für den Merge-Schritt
        mergeSort(array, tempArray, 0, array.size - 1)
    }

    private fun mergeSort(array: IntArray, tempArray: IntArray, left: Int, right: Int) {
        if (left >= right) return

        val mid = (left + right) / 2
        mergeSort(array, tempArray, left, mid)
        mergeSort(array, tempArray, mid + 1, right)
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
