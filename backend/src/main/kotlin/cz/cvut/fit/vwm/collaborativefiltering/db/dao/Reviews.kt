package cz.cvut.fit.vwm.collaborativefiltering.db.dao

import org.jetbrains.squash.definition.*

object Reviews : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val userId = integer("user_id")
    val songId = integer("song_id")
    val value = integer("value")

    init {
        constraints.add(IndexConstraint(
                Identifier("UIX_review_user_song"),
                listOf(userId, songId),
                true))
    }
}
