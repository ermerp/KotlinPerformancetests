package performancetests.bank

interface BankAccountRepository {
    @Throws(Exception::class)
    suspend fun createAccount(accountId: String, balance: Double): Int

    @Throws(Exception::class)
    suspend fun deleteAllAccounts(): Int

    @Throws(Exception::class)
    suspend fun book(from: String, to: String, amount: Double): Int
}