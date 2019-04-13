package cz.cvut.fit.vwm.collaborativefiltering.db.dao

import org.jetbrains.squash.definition.*

object Recommendations : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val userId = varchar("user_id", 50)
    val songId = varchar("song_id", 50)
    val viewed = bool("viewed").default(false)
}
