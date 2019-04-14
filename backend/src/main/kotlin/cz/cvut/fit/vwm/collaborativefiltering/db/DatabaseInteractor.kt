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
        }.fetch(Reviews.id).execute()
    }

    override fun getReviews(): List<Review> = db.transaction {
        from(Reviews)
                .select(Reviews)
                .execute()
                .map { row -> Review(row[Reviews.id], row[Reviews.userId], row[Reviews.songId], row[Reviews.value]) }
                .toList()
    }

    override fun getReviewsCount(): Int = db.transaction {
        from(Reviews).select(Reviews.id.count()).execute().single()[0]
    }

    override fun updateSpearmanCoefficients(): Unit = db.transaction {
        val distance = "distance"
        val u1Id = "user_id_1"
        val u2Id = "user_id_2"
        val myRank1 = "my_rank_1"
        val myRank2 = "my_rank_2"
        val minSame = "1" // count(uid) in SQL QUERY must be above 1 otherwise division by 0 (1-1)
        /*
        * 1. makes a cross join of users
        * 2. joins reviews on user_id for both user columns
        * 3. filters only those lines that have the same song_id
        * 4. recalculates rank partitioned by (userId1 and userId2) depending on review.value
        * 5. calculates distance, sum_of_distances and spearman_coeficient
        * 6. saves it to CorrelationCoefficients table
        * */
        deleteFrom(CorrelationCoefficients).execute()

        val ultimateSpearmanStatement = with(Reviews) {
            with(CorrelationCoefficients) {
                """
                    INSERT INTO ${CorrelationCoefficients.tableName}
                        (   ${userId1.colNameEsc},
                            ${userId2.colNameEsc},
                            $distance,
                            ${spearmanCoeficient.colNameEsc}
                        )

                        SELECT  $u1Id,
                                $u2Id,
                                SUM(POW($myRank1 - $myRank2, 2)) AS sum_of_distance,
                                (1 - ((6 * SUM(POW($myRank1 - $myRank2, 2))) / (POW(COUNT($u1Id), 3) - COUNT($u1Id)))) AS spearman
                        FROM
                            (
                                SELECT  U1.id AS $u1Id,
                                        U2.id AS $u2Id,
                                        (RANK() OVER  ( PARTITION BY U1.id, U2.id
                                                        ORDER BY R1.${value.colNameEsc}) + (
                                                            (COUNT(1) OVER (PARTITION BY
                                                                            U1.id,
                                                                            U2.id,
                                                                            R1.${value.colNameEsc}) - 1
                                                            ) / 2)
                                        ) AS $myRank1,

                                        (RANK() OVER  ( PARTITION BY U1.id, U2.id
                                                        ORDER BY R2.${value.colNameEsc}) + (
                                                            (COUNT(1) OVER (PARTITION BY
                                                                            U1.id,
                                                                            U2.id,
                                                                            R2.${value.colNameEsc}) - 1
                                                            ) / 2)
                                        ) AS $myRank2,

                                        R1.${songId.colNameEsc}
                                FROM ${Users.tableName} U1
                                CROSS JOIN ${Users.tableName} U2
                                JOIN ${Reviews.tableName} R1 ON U1.id = R1.${userId.colNameEsc}
                                JOIN ${Reviews.tableName} R2 ON U2.id = R2.${userId.colNameEsc}
                                WHERE R1.${songId.colNameEsc} = R2.${songId.colNameEsc}
                                AND U1.id < U2.id
                            ) RANKING_TABLE
                        GROUP BY $u1Id, $u2Id
                        HAVING COUNT($u1Id) > $minSame
                """
            }
        }
        executeStatement(ultimateSpearmanStatement)
    }

    override fun updateRecommendations(): Unit = db.transaction {
        val user = 5 // which user
        val limit = 5 // how many songs to recommend

        deleteFrom(Recommendations).execute()

        val recommendationForUser = with(Reviews) {
            with(CorrelationCoefficients){
                with (Recommendations) {
                    "INSERT INTO recommendations(user_id, song_id, viewed) " +
                            "SELECT $user AS REF, " +
                            "       NOT_LISTENED.song_id, " +
                            "       0 AS viewed " +
                            "FROM " +
                            "( " +
                            "    ( " +
                            "        SELECT song_id, value, user_id_1, user_id_2 " +
                            "           FROM " +
                            "             (SELECT user_id_1, " +
                            "                     user_id_2 " +
                            "              FROM correlationcoefficients " +
                            "              WHERE user_id_1 = $user " +
                            "              ORDER BY spearman_coef DESC " +
                            "              LIMIT 2) TOP_MATCHING " +
                            "           JOIN reviews R1 ON user_id_2 = R1.user_id " +
                            "           WHERE R1.song_id NOT IN " +
                            "               (SELECT In_R.song_id " +
                            "                FROM reviews In_R " +
                            "                WHERE In_R.user_id = $user ) " +
                            "           LIMIT 3" +
                            "    ) " +
                            "    UNION " +
                            "    ( " +
                            "        SELECT song_id, value, user_id_1, user_id_2 " +
                            "           FROM " +
                            "             (SELECT user_id_1, " +
                            "                     user_id_2 " +
                            "              FROM correlationcoefficients " +
                            "              WHERE user_id_2 = $user " +
                            "              ORDER BY spearman_coef DESC " +
                            "              LIMIT 2) TOP_MATCHING " +
                            "           JOIN reviews R1 ON user_id_1 = R1.user_id " +
                            "           WHERE R1.song_id NOT IN " +
                            "               (SELECT In_R.song_id " +
                            "                FROM reviews In_R " +
                            "                WHERE In_R.user_id = $user ) " +
                            "           LIMIT 3" +
                            "    ) " +
                            ")NOT_LISTENED " +
                            "GROUP BY NOT_LISTENED.song_id " +
                            "ORDER BY AVG(NOT_LISTENED.value) DESC, COUNT(NOT_LISTENED.song_id) DESC " +
                            "LIMIT $limit"
                }
            }
        }
        executeStatement(recommendationForUser)
    }

    override fun getSpearmanCoefficient(uid1: Int, uid2: Int): CorrelationCoeficient = db.transaction {
        val (first, second) = if (uid1 < uid2) Pair(uid1, uid2) else Pair(uid2, uid1)
        with(CorrelationCoefficients) {
            from(this)
                    .where { ((userId1 eq first) and (userId2 eq second)) }
                    .execute()
                    .mapNotNull { CorrelationCoeficient(it[id], it[userId1], it[userId2], it[distance].toDouble(), it[spearmanCoeficient].toDouble()) }
                    .single()
        }
    }

    override fun getSpearmanCoefficients(userId: Int): List<CorrelationCoeficient> = db.transaction {
        with(CorrelationCoefficients) {
            from(this)
                    .where { (userId1 eq userId) or (userId2 eq userId) }
                    .execute()
                    .map { CorrelationCoeficient(it[id], it[userId1], it[userId2], it[distance].toDouble(), it[spearmanCoeficient].toDouble()) }
                    .toList()
        }
    }

    override fun getSpearmanCoefficients(): List<CorrelationCoeficient> = db.transaction {
        with(CorrelationCoefficients) {
            from(this)
                    .execute()
                    .map { CorrelationCoeficient(it[id], it[userId1], it[userId2], it[distance].toDouble(), it[spearmanCoeficient].toDouble()) }
                    .toList()
        }
    }
}
