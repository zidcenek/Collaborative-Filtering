package cz.cvut.fit.vwm.collaborativefiltering.db.dao

import org.jetbrains.squash.definition.*

object Songs : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val mbid = varchar("mbid", 60).index().nullable()
    val artist = varchar("artist", 50)
    val title = varchar("title", 50)
    val lastFmRank = integer("last_fm_rank").nullable()
    val url = varchar("url", 1024).nullable()
}
