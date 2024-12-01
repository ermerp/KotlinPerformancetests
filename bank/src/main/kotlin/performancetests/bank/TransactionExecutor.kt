package performancetests.bank

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore

class TransactionExecutor(private val repository: BankAccountRepository) {

    suspend fun executeTransactionsSingle(transactions: List<Transaction>) {
        transactions.forEach { transaction ->
            repository.book(transaction.from, transaction.to, transaction.amount)
        }
    }

    suspend fun executeTransactionsCoroutine(transactions: List<Transaction>) = coroutineScope {
        val semaphore = if (repository is PostgRESTBankAccountRepository) Semaphore(System.getenv("MAX_CONNECTIONS")?.toIntOrNull() ?: 80) else null

        transactions.forEach { transaction ->
            launch {
                semaphore?.acquire()
                try {
                    repository.book(transaction.from, transaction.to, transaction.amount)
                } finally {
                    semaphore?.release()
                }
            }
        }
    }
}
