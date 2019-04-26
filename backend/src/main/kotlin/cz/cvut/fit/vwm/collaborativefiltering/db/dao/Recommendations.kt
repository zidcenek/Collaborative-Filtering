package cz.cvut.fit.vwm.collaborativefiltering.db.dao

import org.jetbrains.squash.definition.*

object Recommendations : TableDefinition() {
    //val id = integer("id").autoIncrement().primaryKey()
    val userId = integer("user_id")
    val songId = integer("song_id")
    val viewed = integer("viewed").default(0)
    val weight = decimal("weight", 38, 20)

    init {
        primaryKey(userId, songId)
    }
}
