package performancetests.bank

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import java.time.Duration

class R2DBCBankAccountRepository : BankAccountRepository {

    private val connectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "postgresql")
            .option(ConnectionFactoryOptions.HOST, "localhost")
            .option(ConnectionFactoryOptions.PORT, 5432)
            .option(ConnectionFactoryOptions.DATABASE, "mydatabase")
            .option(ConnectionFactoryOptions.USER, "myuser")
            .option(ConnectionFactoryOptions.PASSWORD, "mypassword")
            .build()
    )

    private val connectionPool = ConnectionPool(
        ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(Duration.ofMinutes(30))
            .maxSize(20)
            .build()
    )

    private suspend fun <T> withConnection(block: suspend (Connection) -> T): T {
        return withContext(Dispatchers.IO) {
            val connection = connectionPool.create().awaitSingle()
            try {
                block(connection)
            } finally {
                connection.close().awaitFirstOrNull()
            }
        }
    }

    override suspend fun createAccount(accountId: String, balance: Double): Int {
        return withConnection { connection ->
            val result = connection.createStatement("INSERT INTO account (id, balance) VALUES ($1, $2)")
                .bind("$1", accountId)
                .bind("$2", balance)
                .execute()
                .awaitSingle()

            result.rowsUpdated.awaitSingle().toInt()
        }
    }

    override suspend fun deleteAllAccounts(): Int {
        return withConnection { connection ->
            connection.createStatement("DELETE FROM account").execute().awaitSingle()
            1
        }
    }

    override suspend fun book(from: String, to: String, amount: Double): Int {
        val maxRetries = 100
        val retryDelayMs = 1000L

        for (attempt in 0 until maxRetries) {
            try {
                return withConnection { connection ->
                    connection.beginTransaction().awaitFirstOrNull()

                    val fromAccount = connection.createStatement("SELECT id, balance FROM account WHERE id = $1 FOR UPDATE")
                        .bind("$1", from).execute().awaitSingle().map { row, _ ->
                            BankAccount(row.get("id", String::class.java)!!, row.get("balance", java.math.BigDecimal::class.java)!!.toDouble())
                        }.awaitSingle()

                    val toAccount = connection.createStatement("SELECT id, balance FROM account WHERE id = $1 FOR UPDATE")
                        .bind("$1", to).execute().awaitSingle().map { row, _ ->
                            BankAccount(row.get("id", String::class.java)!!, row.get("balance", java.math.BigDecimal::class.java)!!.toDouble())
                        }.awaitSingle()

                    connection.createStatement("UPDATE account SET balance = $1 WHERE id = $2")
                        .bind("$1", fromAccount.balance - amount).bind("$2", fromAccount.id).execute().awaitSingle().rowsUpdated.awaitSingle().toInt()

                    connection.createStatement("UPDATE account SET balance = $1 WHERE id = $2")
                        .bind("$1", toAccount.balance + amount).bind("$2", toAccount.id).execute().awaitSingle().rowsUpdated.awaitSingle().toInt()

                    connection.commitTransaction().awaitFirstOrNull()
                    1
                }
            } catch (e: Exception) {
                if (e.message?.contains("deadlock") == true) {
                    val sleepTime = retryDelayMs * (attempt + 1) + (Math.random() * 5000).toLong()
                    println("Deadlock detected. Retrying after $sleepTime ms...")
                    kotlinx.coroutines.delay(sleepTime)
                } else {
                    println("Error during transfer: ${e.message}")
                    throw e
                }
            }
        }
        println("Error!!! Transfer failed after $maxRetries attempts due to persistent deadlocks: $from -> $to, amount: $amount")
        return 0
    }
}