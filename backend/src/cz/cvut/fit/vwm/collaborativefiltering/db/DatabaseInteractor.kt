package cz.cvut.fit.vwm.collaborativefiltering.db

import cz.cvut.fit.vwm.collaborativefiltering.data.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.data.model.User
import cz.cvut.fit.vwm.collaborativefiltering.db.dao.*
import cz.cvut.fit.vwm.collaborativefiltering.hash
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.dialects.h2.H2Connection
import org.jetbrains.squash.expressions.count
import org.jetbrains.squash.expressions.eq
import org.jetbrains.squash.query.from
import org.jetbrains.squash.query.select
import org.jetbrains.squash.query.where
import org.jetbrains.squash.results.get
import org.jetbrains.squash.schema.create
import org.jetbrains.squash.statements.fetch
import org.jetbrains.squash.statements.insertInto
import org.jetbrains.squash.statements.values
import java.io.File


class DatabaseInteractor(val db: DatabaseConnection = H2Connection.createMemoryConnection()) : IDatabaseInteractor {

    constructor(dir: File) : this(H2Connection.create("jdbc:h2:file:${dir.canonicalFile.absolutePath}"))

    init {
        db.transaction {
            databaseSchema().create(Songs, Reviews, Recommendations, CorrelationCoefficients, Users)
        }
    }

    override fun createSong(song: Song) = db.transaction {
        insertInto(Songs).values {
            it[artist] = song.artist
            it[title] = song.title
            it[mbid] = song.mbid
            it[lastFmRank] = song.lastFmRank
            it[url] = song.url
        }.fetch(Songs.id).execute()
    }

    override fun getSong(id: Int): Song = db.transaction {
        val row = from(Songs).where { Songs.id eq id }.execute().single()
        Song(id, row[Songs.mbid], row[Songs.artist], row[Songs.title], row[Songs.lastFmRank], row[Songs.url])
    }

    override fun getSongsCount(): Int = db.transaction {
        from(Songs).select(Songs.id.count()).execute().single()[0]
    }

    override fun getSongs(): List<Song> = db.transaction {
        from(Songs)
                .select(Songs)
                .execute()
                .map { row -> Song(row[Songs.id], row[Songs.mbid], row[Songs.artist], row[Songs.title], row[Songs.lastFmRank], row[Songs.url]) }
                .toList()
    }


    override fun createUser(user: User): Int = db.transaction {
        insertInto(Users).values {
            it[name] = user.name
            it[surname] = user.surname
            it[email] = user.email
            it[password] = hash(user.password)
        }.fetch(Users.id).execute()
    }

    override fun getUsers(): List<User> = db.transaction {
        from(Users)
                .select(Users)
                .execute()
                .map { row -> User(row[Users.id], row[Users.name], row[Users.surname], row[Users.email], row[Users.password]) }
                .toList()
    }

    override fun getUsersCount(): Int = db.transaction {
        from(Users).select(Users.id.count()).execute().single()[0]
    }

    override fun close() {
    }
}
