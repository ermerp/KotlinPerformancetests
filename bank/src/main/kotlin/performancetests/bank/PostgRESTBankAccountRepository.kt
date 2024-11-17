package performancetests.bank

import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

class PostgRESTBankAccountRepository : BankAccountRepository {

    companion object {
        const val URL = "http://localhost:3000/rpc/"
        const val MAX_RETRIES = 100
        const val RETRY_DELAY_MS = 1000L
    }

    @Throws(Exception::class)
    override suspend fun createAccount(accountId: String, balance: Double): Int {
        val connection = setupConnection("create_account")
        sendRequest(connection, """{ "account_id": "$accountId", "balance": $balance }""")
        return connection.responseCode
    }

    @Throws(Exception::class)
    override suspend fun deleteAllAccounts(): Int {
        val connection = setupConnection("delete_all_accounts")
        sendRequest(connection, "{}")
        return connection.responseCode
    }

    @Throws(Exception::class)
    override suspend fun book(from: String, to: String, amount: Double): Int {
        var attempt = 0
        while (attempt < MAX_RETRIES) {
            val connection = setupConnection("transfer_balance")
            sendRequest(connection, """{ "from_id": "$from", "to_id": "$to", "amount": $amount }""")
            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    return responseCode
                }
                HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    attempt++
                    val sleepTime = calculateRetryDelay(attempt)
                    println("Deadlock detected. Transfer attempt $attempt failed: $from -> $to, amount: $amount. Retrying after $sleepTime ms...")
                    Thread.sleep(sleepTime)
                }
                HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> {
                    attempt++
                    val sleepTime = calculateRetryDelay(attempt)
                    println("Gateway Timeout. Transfer attempt $attempt failed: $from -> $to, amount: $amount. Retrying after $sleepTime ms...")
                    Thread.sleep(sleepTime)
                }
                else -> {
                    val sleepTime = calculateRetryDelay(attempt)
                    println("Error!!! Transfer failed - Code: $responseCode Retrying after $sleepTime ms ...")
                    Thread.sleep(sleepTime)
                }
            }
        }
        println("Error!!! Transfer failed after $MAX_RETRIES attempts due to persistent deadlocks: $from -> $to, amount: $amount")
        return HttpURLConnection.HTTP_INTERNAL_ERROR
    }

    private fun calculateRetryDelay(attempt: Int): Long {
        return RETRY_DELAY_MS * attempt + (Math.random() * 5000).toLong()
    }

    @Throws(Exception::class)
    private fun setupConnection(endpoint: String): HttpURLConnection {
        val uri = URI(URL + endpoint)
        val connection = uri.toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        return connection
    }

    @Throws(Exception::class)
    private fun sendRequest(connection: HttpURLConnection, jsonData: String) {
        connection.outputStream.use { os ->
            val input = jsonData.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }
    }
}