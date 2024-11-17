package performancetests.bank

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object Account : Table() {
    val id = varchar("id", 50)
    val balance = double("balance")
}

class JDBCBankAccountRepository : BankAccountRepository {
    private val logger = LoggerFactory.getLogger(JDBCBankAccountRepository::class.java)

    init {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/mydatabase",
            driver = "org.postgresql.Driver",
            user = "myuser",
            password = "mypassword"
        )
        transaction {
            SchemaUtils.create(Account)
        }
    }

    override suspend fun createAccount(accountId: String, balance: Double): Int = dbQuery {
        Account.insert {
            it[Account.id] = accountId
            it[Account.balance] = balance
        }
        1
    }

    override suspend fun deleteAllAccounts(): Int = dbQuery {
        Account.deleteAll()
        1
    }

    override suspend fun book(from: String, to: String, amount: Double): Int = dbQuery {
        val maxRetries = 100
        val retryDelayMs = 1000L

        for (attempt in 0 until maxRetries) {
            try {
                val result = transaction {
                    // Lock rows in the correct order
                    if (from < to) {
                        Account.select { Account.id eq from }.forUpdate().first()
                        Account.select { Account.id eq to }.forUpdate().first()
                    } else {
                        Account.select { Account.id eq to }.forUpdate().first()
                        Account.select { Account.id eq from }.forUpdate().first()
                    }

                    // Perform the balance update
                    Account.update({ Account.id eq from }) {
                        with(SqlExpressionBuilder) {
                            it.update(balance, balance - amount)
                        }
                    }

                    Account.update({ Account.id eq to }) {
                        with(SqlExpressionBuilder) {
                            it.update(balance, balance + amount)
                        }
                    }

                    //logger.info("Transfer successful: from=$from, to=$to, amount=$amount")
                    1
                }
                return@dbQuery 1
            } catch (e: ExposedSQLException) {
                if (e.sqlState == "40001") { // SQLState for deadlock
                    val sleepTime = retryDelayMs * (attempt + 1) + (Math.random() * 5000).toLong()
                    println("Attempt $attempt: Deadlock detected. Retrying after $sleepTime ms...")
                    kotlinx.coroutines.delay(sleepTime)
                } else {
                    println("Attempt $attempt: Error during transfer: ${e.message}")
                    throw e
                }
            }
        }
        println("Error!!! Transfer failed after $maxRetries attempts due to persistent deadlocks: $from -> $to, amount: $amount")
        return@dbQuery 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            newSuspendedTransaction {
                block()
            }
        }
}