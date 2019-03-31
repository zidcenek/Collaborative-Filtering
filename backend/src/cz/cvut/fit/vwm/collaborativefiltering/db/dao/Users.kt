package cz.cvut.fit.vwm.collaborativefiltering.db.dao

import org.jetbrains.squash.definition.*

object Users : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 50)
    val surname = varchar("surname", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 64)
}