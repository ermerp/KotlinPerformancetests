package performancetests.bank

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class TransactionExecutor(private val repository: BankAccountRepository) {

    suspend fun executeTransactionsSingle(transactions: List<Transaction>) {
        transactions.forEach { transaction ->
            repository.book(transaction.from, transaction.to, transaction.amount)
        }
    }

    suspend fun executeTransactionsCoroutine(transactions: List<Transaction>) = coroutineScope {
        transactions.forEach { transaction ->
            launch {
                repository.book(transaction.from, transaction.to, transaction.amount)
            }
        }
    }
}
