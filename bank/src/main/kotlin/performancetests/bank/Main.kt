package performancetests.bank

import java.io.File

suspend fun main() {
    println("Kotlin:Bank")

    val numberOfAccounts = 100
    val numberOfTransactions = 1000

    //val repository = PostgRESTBankAccountRepository()
    //val repository = JDBCBankAccountRepository()
    val repository = R2DBCBankAccountRepository()

    // Delete old data
    repository.deleteAllAccounts()

    println("deleted")
    kotlinx.coroutines.delay(10000)

    // Import bank accounts and save to the database
    File("BankAccounts$numberOfAccounts.txt").readLines().forEach { line ->
        val (accountId, balance) = line.split(", ").map { it.trim() }
        repository.createAccount(accountId, balance.replace(",", ".").toDouble())
    }

    println("created")
    kotlinx.coroutines.delay(10000)

    // Import transactions
    val transactions = mutableListOf<Transaction>()
    File("BankTransactions$numberOfTransactions-$numberOfAccounts.txt").readLines().forEach { line ->
        val (from, to, amount) = line.split(", ").map { it.trim() }
        val transaction = Transaction(from, to, amount.replace(",", ".").toDouble())
        transactions.add(transaction)
    }

    println("File imported.")

    val startTime = System.currentTimeMillis()

    val transactionExecutor = TransactionExecutor(repository)
    transactionExecutor.executeTransactionsCoroutine(transactions)
    //transactionExecutor.executeTransactionsSingle(transactions)

    val endTime = System.currentTimeMillis()
    println("Kotlin:Bank -  Time:  ${endTime - startTime} ms")
}