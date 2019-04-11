package cz.cvut.fit.vwm.collaborativefiltering.db

import cz.cvut.fit.vwm.collaborativefiltering.*
import cz.cvut.fit.vwm.collaborativefiltering.data.model.CorrelationCoeficient
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Review
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.data.model.User
import cz.cvut.fit.vwm.collaborativefiltering.db.dao.*
import cz.cvut.fit.vwm.collaborativefiltering.db.driver.MySqlConnection
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.expressions.and
import org.jetbrains.squash.expressions.count
import org.jetbrains.squash.expressions.eq
import org.jetbrains.squash.expressions.or
import org.jetbrains.squash.query.from
import org.jetbrains.squash.query.select
import org.jetbrains.squash.query.where
import org.jetbrains.squash.results.get
import org.jetbrains.squash.schema.create
import org.jetbrains.squash.statements.deleteFrom
import org.jetbrains.squash.statements.fetch
import org.jetbrains.squash.statements.insertInto
import org.jetbrains.squash.statements.values


class DatabaseInteractor(val db: DatabaseConnection = MySqlConnection.create(
        "jdbc:mysql://localhost:3306/$DB_NAME?useSSL=false&serverTimezone=Europe/Prague", "root", ""
)) : IDatabaseInteractor {

    init {
        db.transaction {
            executeStatement(Users.dropStatement) // TODO testing only
            executeStatement(CorrelationCoefficients.dropStatement)
            executeStatement(Reviews.dropStatement)
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

    override fun createReview(review: Review): Int = db.transaction {
        insertInto(Reviews).values {
            it[userId] = review.userId
            it[songId] = review.songId
            it[value] = review.value
            it[rank] = review.rank
        }.fetch(Reviews.id).execute()
    }

    override fun getReviews(): List<Review> = db.transaction {
        from(Reviews)
                .select(Reviews)
                .execute()
                .map { row -> Review(row[Reviews.id], row[Reviews.userId], row[Reviews.songId], row[Reviews.value], row[Reviews.rank]) }
                .toList()
    }

    override fun getReviewsCount(): Int = db.transaction {
        from(Reviews).select(Reviews.id.count()).execute().single()[0]
    }

    override fun updateSpearmanCoefficients(): Unit = db.transaction {
        /*deleteFrom(Ranks).execute()
        val ranksStatement = with(Ranks) {
            with(Reviews) {
                "INSERT INTO ${Ranks.tableName}(" +
                        "${userId1.colNameEsc}, " +
                        "${userId2.colNameEsc}, " +
                        "${rank1.colNameEsc}, " +
                        "${rank2.colNameEsc}, " +
                        "${songId.colNameEsc})\n" +
                 "SELECT \n" +
                        "\tU1.id AS user_id_1,\n" +
                        "\tU2.id AS user_id_2,\n" +
                        "\tRANK() OVER( PARTITION BY U1.id, U2.id ORDER BY R1.value ) AS my_rank,\n" +
                        "    RANK() OVER( PARTITION BY U1.id, U2.id ORDER BY R2.value ) AS my_rank,\n" +
                        "    R1.song_id\n" +
                        "FROM users U1\n" +
                        "CROSS JOIN users U2\n" +
                        "JOIN reviews R1 ON U1.id = R1.user_id\n" +
                        "JOIN reviews R2 ON U2.id = R2.user_id\n" +
                 "WHERE R1.song_id = R2.song_id\n" +
                        "AND U1.id < U2.id\n" +
                        "AND not U1.id = U2.id "

            }
        }
        deleteFrom(CorrelationCoefficients).execute()
        val spearmanStatement = with(Ranks) {
            with(CorrelationCoefficients) {
                "INSERT INTO correlationcoefficients(user_id_1, user_id_2, distance, spearman_coef) \n" +
                "SELECT \n" +
                        "   user_id_1, \n" +
                        "    user_id_2,\n" +
                        "    SUM(POW(rank_1 - rank_2, 2)) as sum_of_distance,\n" +
                        "    (1 - ((6 * SUM(POW(rank_1 - rank_2, 2)) ) / ( POW(COUNT(user_id_1),3) - COUNT(user_id_1)) ) ) as spearman\n" +
                        "FROM ranks\n" +
                        "GROUP BY user_id_1, user_id_2\n" +
                "HAVING COUNT(user_id_1) > 1\n" +
                        "\n" +
                        "Vyplnění tabulky CorrelationCoefficient z tabulky Ranks\n" +
                        "---\n" +
                "SELECT \n" +
                        "\tSUM(POW(rank_1 - rank_2, 2)) as sum_of_distance\n" +
                        "FROM ranks\n" +
                        "GROUP BY user_id_1, user_id_2"

            }
        }
        executeStatement(ranksStatement)
        executeStatement(spearmanStatement)
        */

        deleteFrom(CorrelationCoefficients).execute()
        val ultimateSpearmanStatement = with(Reviews) {
            with(CorrelationCoefficients) {
                "INSERT INTO correlationcoefficients(user_id_1, user_id_2, distance, spearman_coef)\n" +
                        "SELECT user_id_1,\n" +
                        "       user_id_2,\n" +
                        "       SUM(POW(my_rank_1 - my_rank_2, 2)) AS sum_of_distance,\n" +
                        "       (1 - ((6 * SUM(POW(my_rank_1 - my_rank_2, 2))) / (POW(COUNT(user_id_1), 3) - COUNT(user_id_1)))) AS spearman\n" +
                        "FROM\n" +
                        "  (SELECT U1.id AS user_id_1,\n" +
                        "          U2.id AS user_id_2,\n" +
                        "          (RANK() OVER(PARTITION BY U1.id, U2.id\n" +
                        "                       ORDER BY R1.value) + ((COUNT(1) OVER (PARTITION BY U1.id,\n" +
                        "                                                                          U2.id,\n" +
                        "                                                                          R1.value) - 1) / 2))AS my_rank_1,\n" +
                        "          (RANK() OVER(PARTITION BY U1.id, U2.id\n" +
                        "                       ORDER BY R2.value) + ((COUNT(1) OVER (PARTITION BY U1.id,\n" +
                        "                                                                          U2.id,\n" +
                        "                                                                          R2.value) - 1) / 2)) AS my_rank_2,\n" +
                        "          R1.song_id\n" +
                        "   FROM users U1\n" +
                        "   CROSS JOIN users U2\n" +
                        "   JOIN reviews R1 ON U1.id = R1.user_id\n" +
                        "   JOIN reviews R2 ON U2.id = R2.user_id\n" +
                        "   WHERE R1.song_id = R2.song_id\n" +
                        "     AND U1.id < U2.id\n" +
                        "     AND NOT U1.id = U2.id ) RANKING_TABLE\n" +
                        "GROUP BY user_id_1,\n" +
                        "         user_id_2\n" +
                        "HAVING COUNT(user_id_1) > 1"
            }
        }
        executeStatement(ultimateSpearmanStatement)
        /*

        val rev1 = "R1"
        val rev2 = "R2"
        val newRank = "new_rank"
        val statement = with(Reviews) {
            "           UPDATE $tableName $rev1" +
                    "   JOIN (" +
                    "       SELECT ${id.colNameEsc}, " +
                    "           RANK() OVER (PARTITION BY ${userId.colNameEsc} " +
                    "       ORDER BY ${value.colNameEsc} DESC) AS $newRank" +
                    "       FROM $tableName" +
                    "   ) AS $rev2 " +
                    "   ON $rev1.${id.colName} = $rev2.${id.colName}" +
                    "   SET $rev1.${rank.colName} = $rev2.$newRank"
        }
        executeStatement(statement)

        val crossedUsers = "cross_users"
        val distance = "distance"

        val u1Id = "u1_id"
        val u2Id = "u2_id"

        deleteFrom(CorrelationCoefficients).execute()

        val spearmanStatement = with(Reviews) {
            with(CorrelationCoefficients) {
                "INSERT INTO ${CorrelationCoefficients.tableName} " +
                        "            (${userId1.colNameEsc}, " +
                        "             ${userId2.colNameEsc}, " +
                        "             $distance, " +
                        "             ${spearmanCoeficient.colNameEsc}) " +
                        "SELECT $crossedUsers.$u1Id, " +
                        "       $crossedUsers.$u2Id, " +
                        "       Sum(Pow(R1.${rank.colNameEsc} - R2.${rank.colNameEsc}, 2)) AS $distance, " +
                        "       (1 - (( 6 * Sum(Pow(R1.${rank.colNameEsc} - R2.${rank.colNameEsc}, 2)) ) / ( " +
                        "          Pow(Count($crossedUsers.$u2Id), 3) - Count( " +
                        "           $crossedUsers.u2_id) ) ) )" +
                        "FROM   (SELECT U1.id AS $u1Id, " +
                        "               U2.id AS $u2Id " +
                        "        FROM   ${Users.tableName} U1, " +
                        "               ${Users.tableName} U2 " +
                        "        WHERE U1.id < U2.id) $crossedUsers " +
                        "       LEFT JOIN ${Reviews.tableName} R1 " +
                        "              ON $crossedUsers.$u1Id = R1.${userId.colNameEsc} " +
                        "       LEFT JOIN ${Reviews.tableName} R2 " +
                        "              ON $crossedUsers.$u2Id = R2.${userId.colNameEsc} " +
                        "        WHERE R1.${songId.colNameEsc} = R2.${songId.colNameEsc} " +
                        "GROUP  BY $crossedUsers.$u1Id, " +
                        "          $crossedUsers.$u2Id " +
                        "HAVING $distance IS NOT NULL "
            }
        }
        executeStatement(spearmanStatement)*/
    }

    override fun getSpearmanCoefficient(uid1: Int, uid2: Int): CorrelationCoeficient = db.transaction {
        val (first, second) = if (uid1 < uid2) Pair(uid1, uid2) else Pair(uid2, uid1)
        with(CorrelationCoefficients) {
            from(this)
                    .where { ((userId1 eq first) and (userId2 eq second)) }
                    .execute()
                    .mapNotNull { CorrelationCoeficient(it[id], it[userId1], it[userId2], it[distance], it[spearmanCoeficient].toDouble()) }
                    .single()
        }
    }

    override fun getSpearmanCoefficients(userId: Int): List<CorrelationCoeficient> = db.transaction {
        with(CorrelationCoefficients) {
            from(this)
                    .where { (userId1 eq userId) or (userId2 eq userId) }
                    .execute()
                    .map { CorrelationCoeficient(it[id], it[userId1], it[userId2], it[distance], it[spearmanCoeficient].toDouble()) }
                    .toList()
        }
    }

    override fun getSpearmanCoefficients(): List<CorrelationCoeficient> = db.transaction {
        with(CorrelationCoefficients) {
            from(this)
                    .execute()
                    .map { CorrelationCoeficient(it[id], it[userId1], it[userId2], it[distance], it[spearmanCoeficient].toDouble()) }
                    .toList()
        }
    }
}