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
            .option(ConnectionFactoryOptions.HOST, System.getenv("DB_HOST") ?: "localhost")
            .option(ConnectionFactoryOptions.PORT, 5432)
            .option(ConnectionFactoryOptions.DATABASE, "mydatabase")
            .option(ConnectionFactoryOptions.USER, "myuser")
            .option(ConnectionFactoryOptions.PASSWORD, "mypassword")
            .build()
    )

    private val connectionPool = ConnectionPool(
        // Set the maximum pool size
        // Set the connection timeout
        ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(Duration.ofMinutes(60))
            .maxSize(System.getenv("MAX_CONNECTIONS")?.toIntOrNull() ?: 80)
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

    // creates a new account in the database
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

    // deletes all accounts from the database
    override suspend fun deleteAllAccounts(): Int {
        return withConnection { connection ->
            connection.createStatement("DELETE FROM account").execute().awaitSingle()
            1
        }
    }

    // transfers the balance from one account to another
    override suspend fun book(from: String, to: String, amount: Double, delay: Double): Int {
        val maxRetries = 100
        val retryDelayMs = 1000L

        for (attempt in 0 until maxRetries) {
            try {
                return withConnection { connection ->
                    connection.beginTransaction().awaitFirstOrNull()
                    // Lock rows in the correct order
                    val (firstId, secondId) = if (from < to) from to to else to to from

                    val firstAccount = connection.createStatement("SELECT id, balance FROM account WHERE id = $1 FOR UPDATE")
                        .bind("$1", firstId).execute().awaitSingle().map { row, _ ->
                            BankAccount(row.get("id", String::class.java)!!, row.get("balance", java.math.BigDecimal::class.java)!!.toDouble())
                        }.awaitSingle()

                    val secondAccount = connection.createStatement("SELECT id, balance FROM account WHERE id = $1 FOR UPDATE")
                        .bind("$1", secondId).execute().awaitSingle().map { row, _ ->
                            BankAccount(row.get("id", String::class.java)!!, row.get("balance", java.math.BigDecimal::class.java)!!.toDouble())
                        }.awaitSingle()

                    // Perform the delay if specified
                    if (delay > 0) {
                        connection.createStatement("SELECT pg_sleep($delay);")
                            .execute().awaitSingle()
                    }

                    // Perform the balance update
                    if (firstId == from) {
                        connection.createStatement("UPDATE account SET balance = $1 WHERE id = $2")
                            .bind("$1", firstAccount.balance - amount).bind("$2", firstAccount.id).execute().awaitSingle().rowsUpdated.awaitSingle().toInt()

                        connection.createStatement("UPDATE account SET balance = $1 WHERE id = $2")
                            .bind("$1", secondAccount.balance + amount).bind("$2", secondAccount.id).execute().awaitSingle().rowsUpdated.awaitSingle().toInt()
                    } else {
                        connection.createStatement("UPDATE account SET balance = $1 WHERE id = $2")
                            .bind("$1", firstAccount.balance + amount).bind("$2", firstAccount.id).execute().awaitSingle().rowsUpdated.awaitSingle().toInt()

                        connection.createStatement("UPDATE account SET balance = $1 WHERE id = $2")
                            .bind("$1", secondAccount.balance - amount).bind("$2", secondAccount.id).execute().awaitSingle().rowsUpdated.awaitSingle().toInt()
                    }

                    connection.commitTransaction().awaitFirstOrNull()
                    1
                }
            } catch (e: Exception) {
                if (e.message?.contains("deadlock") == true) {
                    val sleepTime = retryDelayMs * (attempt + 1) + (Math.random() * 500).toLong()
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