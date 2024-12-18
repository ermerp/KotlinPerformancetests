package performancetests.bank

import java.io.File

suspend fun main() {

    // Retrieve the environment variables
    val interfaceType = System.getenv("INTERFACE_TYPE") ?: "R2DBC"
    val algorithm = System.getenv("ALGORITHM") ?: "COROUTINE"
    val maxConnections = System.getenv("MAX_CONNECTIONS")?.toIntOrNull() ?: 80
    val numberOfAccounts = System.getenv("NUMBER_OF_ACCOUNTS")?.toIntOrNull() ?: 100
    val numberOfTransactions = System.getenv("NUMBER_OF_TRANSACTIONS")?.toIntOrNull() ?: 1000
    val delayTransaction = System.getenv("DELAY_TRANSACTION")?.toDoubleOrNull() ?: 0.0

    println("Kotlin:Bank - Interface: $interfaceType" +
            ", Algorithm: $algorithm" +
            ", Max Connections: $maxConnections" +
            ", Number of accounts: $numberOfAccounts" +
            ", Number of Transactions: $numberOfTransactions" +
            ", Delay Transaction: $delayTransaction")

    val repository = when (interfaceType) {
        "REST" -> PostgRESTBankAccountRepository()
        "R2DBC" -> R2DBCBankAccountRepository()
        else -> throw IllegalArgumentException("Unknown interface type: $interfaceType")
    }

    // Clean up the database
    repository.deleteAllAccounts()

    // Import bank accounts and save them to the database
    File("/bankData/BankAccounts$numberOfAccounts.txt").readLines().forEach { line ->
        val (accountId, balance) = line.split(", ").map { it.trim() }
        repository.createAccount(accountId, balance.replace(",", ".").toDouble())
    }

    // Import transactions
    val transactions = mutableListOf<Transaction>()
    File("/bankData/BankTransactions$numberOfTransactions-$numberOfAccounts.txt").readLines().forEach { line ->
        val (from, to, amount) = line.split(", ").map { it.trim() }
        val transaction = Transaction(from, to, amount.replace(",", ".").toDouble())
        transactions.add(transaction)
    }

    println("File imported.")

    // Execute transactions
    val startTime = System.currentTimeMillis()

    val transactionExecutor = TransactionExecutor(repository)
    when (algorithm) {
        "COROUTINE" -> transactionExecutor.executeTransactionsCoroutine(transactions, delayTransaction)
        "SINGLE" -> transactionExecutor.executeTransactionsSingle(transactions)
        else -> throw IllegalArgumentException("Unknown algorithm: $algorithm")
    }

    val endTime = System.currentTimeMillis()

    println("Kotlin:Bank - Time:  ${endTime - startTime} ms")
}