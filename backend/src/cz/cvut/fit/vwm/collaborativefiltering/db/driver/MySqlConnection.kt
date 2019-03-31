package cz.cvut.fit.vwm.collaborativefiltering.db.driver


import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.dialects.mysql.MySqlDialect
import org.jetbrains.squash.dialects.mysql.MySqlTransaction
import org.jetbrains.squash.drivers.JDBCConnection
import java.sql.Connection
import java.sql.DriverManager

/**
 * Source: https://github.com/orangy/squash/tree/master/squash-mysql
 * Explicitly defined here as custom data conversion class is used.
 */
class MySqlConnection(connector: () -> Connection) : JDBCConnection(MySqlDialect, MySqlDataConversion(), connector) {
    override fun createTransaction() = MySqlTransaction(this)

    companion object {
        fun create(url: String, user: String = "", password: String = ""): DatabaseConnection {
            require(url.startsWith("jdbc:mysql:")) { "MySQL JDBC connection requires 'jdbc:mysql:' prefix" }
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance()
            return MySqlConnection { DriverManager.getConnection(url, user, password) }
        }
    }
}