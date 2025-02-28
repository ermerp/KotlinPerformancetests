package performancetests.bank

import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

class TransactionExecutor(private val repository: BankAccountRepository) {

    suspend fun executeTransactionsSingle(transactions: List<Transaction>) {
        transactions.forEach { transaction ->
            repository.book(transaction.from, transaction.to, transaction.amount, 0.0)
        }
    }

    suspend fun executeTransactionsCoroutine(transactions: List<Transaction>, delayTransaction: Double) = coroutineScope{
        val maxCon = System.getenv("MAX_CONNECTIONS")?.toIntOrNull() ?: 80
        val semaphore = if (repository is PostgRESTBankAccountRepository) Semaphore(maxCon)
        else null

        transactions.forEach { transaction ->
            launch(Dispatchers.IO) {
                semaphore?.acquire()
                try {
                    repository.book(transaction.from, transaction.to, transaction.amount, delayTransaction)
                } finally {
                    semaphore?.release()
                }
            }
        }
    }
}
