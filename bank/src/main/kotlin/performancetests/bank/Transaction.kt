package performancetests.bank

data class Transaction(
    val from: String,
    val to: String,
    val amount: Double
)