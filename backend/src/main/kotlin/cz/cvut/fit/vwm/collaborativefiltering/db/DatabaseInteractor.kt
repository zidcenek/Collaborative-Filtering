package cz.cvut.fit.vwm.collaborativefiltering.db

import cz.cvut.fit.vwm.collaborativefiltering.*
import cz.cvut.fit.vwm.collaborativefiltering.data.model.*
import cz.cvut.fit.vwm.collaborativefiltering.db.dao.*
import cz.cvut.fit.vwm.collaborativefiltering.db.driver.MySqlConnection
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.expressions.*
import org.jetbrains.squash.query.*
import org.jetbrains.squash.results.ResultRow
import org.jetbrains.squash.results.get
import org.jetbrains.squash.schema.create
import org.jetbrains.squash.statements.deleteFrom
import org.jetbrains.squash.statements.fetch
import org.jetbrains.squash.statements.insertInto
import org.jetbrains.squash.statements.values
import java.math.BigDecimal


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
                .map { parseSong(it) }
                .toList()
    }

    override fun getReviewedSongs(userId: Int): List<ReviewedSong> = db.transaction {
        with(Reviews) {
            from(this)
                    .where(this.userId eq userId)
                    .rightJoin(Songs, (Songs.id eq songId))
                    .execute()
                    .map { ReviewedSong(parseSong(it), it[value]) }
                    .toList()
        }
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

    override fun getUserByEmail(email: String, hash: String?): User? = db.transaction {
        from(Users).where { Users.email eq email }.execute()
                .mapNotNull {
                    if (hash == null || it[Users.password] == hash) {
                        User(it[Users.id], it[Users.name], it[Users.surname], it[Users.email], it[Users.password])
                    } else {
                        null
                    }
                }
                .singleOrNull()
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

    override fun incrementSongsViewed(recommendations: List<Recommendation>): Unit = db.transaction {
        recommendations.forEach {
            val updateViewedStatement = with(Recommendations) {
                """
                    UPDATE $tableName
                    SET ${viewed.colName} = ${viewed.colName} + 1
                    WHERE ${userId.colName} = ${it.userId}
                    AND ${songId.colName} = ${it.songId}
                """
            }
            executeStatement(updateViewedStatement)
        }
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
                                AND NOT U1.id = U2.id
                            ) RANKING_TABLE
                        GROUP BY $u1Id, $u2Id
                        HAVING COUNT($u1Id) > $minSame
                """
            }
        }
        executeStatement(ultimateSpearmanStatement)
        updateRecommendations()
    }

    override fun updateRecommendations(): Unit = db.transaction {
        val spearmenCoeficientLimit = 0 // the limit spearman coeficient
        val numberOfClosestUseres = 5 // how many best matching users will be taken

        val recommendationForUser = with(Reviews) {
            with(CorrelationCoefficients) {
                with(Recommendations) {
                    """
                        INSERT INTO recommendations
                                    (user_id,
                                     song_id,
                                     viewed,
                                     weight)
                        SELECT BEST_SONGS_FOR_USER.u_id_1,
                               BEST_SONGS_FOR_USER.song_id,
                               0 AS viewed,
                               BEST_SONGS_FOR_USER.avg as avg
                        FROM   (SELECT NOT_LISTENED.song_id        AS song_id,
                                       NOT_LISTENED.user_id_1      AS u_id_1,
                                       Avg(NOT_LISTENED.value)     AS avg,
                                       Count(NOT_LISTENED.song_id) AS cnt,
                                       Rank()
                                         OVER(
                                           partition BY u_id_1
                                           ORDER BY avg DESC)      AS song_rank
                                FROM   (SELECT user_id_1,
                                               user_id_2,
                                               song_id,
                                               value
                                        FROM   (SELECT *
                                                FROM   (SELECT user_id_1,
                                                               user_id_2,
                                                               Rank()
                                                                 OVER(
                                                                   partition BY user_id_1
                                                                   ORDER BY spearman_coef DESC) AS
                                                               my_rank
                                                        FROM   correlationcoefficients C1
                                                        WHERE  C1.spearman_coef > $spearmenCoeficientLimit) RANKED_TABLE
                                                WHERE  RANKED_TABLE.my_rank <= $numberOfClosestUseres) TOP_MATCHING
                                               JOIN reviews R1
                                                 ON user_id_2 = R1.user_id
                                        WHERE  R1.song_id NOT IN (SELECT In_R.song_id
                                                                  FROM   reviews In_R
                                                                  WHERE  In_R.user_id = user_id_1))
                                       NOT_LISTENED
                                GROUP  BY NOT_LISTENED.user_id_1,
                                          NOT_LISTENED.song_id
                                ORDER  BY avg DESC,
                                          cnt DESC) BEST_SONGS_FOR_USER
                        ON DUPLICATE KEY UPDATE viewed = viewed + 1, weight = avg
                    """
                }
            }
        }
        executeStatement(recommendationForUser)
    }

    override fun getUserSongRecommendationsAndIncrementViewedCount(userId: Int): List<SongRecommendation> = db.transaction {
        val songs = getUserSongRecommendations(userId)
        incrementSongsViewed(songs.map {
            Recommendation(userId, it.song.id, 0, 0.0)
        })
        songs
    }

    override fun getUserSongRecommendations(userId: Int): List<SongRecommendation> = db.transaction {
        val recommendedCount = 5L
        val weightLimit = BigDecimal(2.5)
        val viewedLimit = 5
        val songs = with(Recommendations) {
            from(this)
                    .where { ((this.userId eq userId) and (weight.gteq(weightLimit)) and (viewed lteq viewedLimit)) }
                    .innerJoin(Songs, (songId eq Songs.id))
                    .orderByDescending(weight)
                    .orderBy(viewed)
                    .limit(recommendedCount)
                    .execute()
                    .mapNotNull {
                        SongRecommendation(parseSong(it), parseRecommendation(it))
                    }
                    .toList()
        }
        if (songs.isNotEmpty()) songs
        else with(Songs) {
            from(this)
                    .orderBy(lastFmRank)
                    .limit(recommendedCount)
                    .execute()
                    .mapNotNull { SongRecommendation(parseSong(it)) }
                    .toList()
        }
    }

    private fun parseRecommendation(row: ResultRow): Recommendation = with(Recommendations) {
        Recommendation(row[songId], row[userId], row[viewed], row[weight].toDouble())
    }


    private fun parseSong(row: ResultRow) = with(Songs) {
        Song(row[id], row[mbid], row[artist], row[title], row[lastFmRank], row[url])
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
