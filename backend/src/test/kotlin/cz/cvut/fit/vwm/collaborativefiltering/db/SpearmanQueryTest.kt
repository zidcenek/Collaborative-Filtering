package cz.cvut.fit.vwm.collaborativefiltering.db

import cz.cvut.fit.vwm.collaborativefiltering.data.json.MockDataJsonParser
import cz.cvut.fit.vwm.collaborativefiltering.data.model.CorrelationCoeficient
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Review
import cz.cvut.fit.vwm.collaborativefiltering.db.dao.*
import cz.cvut.fit.vwm.collaborativefiltering.dropStatement
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.statements.deleteFrom
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*


class SpearmanQueryTest {

    private val storage = DatabaseInteractor()

    private val ur1 = MockDataJsonParser.praseReview("mock/reviewUser1.json")
    private val ur2 = MockDataJsonParser.praseReview("mock/reviewUser2.json")
    private val ur3 = MockDataJsonParser.praseReview("mock/reviewUser3.json")
    private val ur4 = MockDataJsonParser.praseReview("mock/reviewUser4.json")
    private val ur5 = MockDataJsonParser.praseReview("mock/reviewUser5.json")
    private val ur6 = MockDataJsonParser.praseReview("mock/reviewUser1.json")
            .map { Review(0, 6, it.songId, it.value) }

    @Before
    fun setUp() {
        MockDataJsonParser.parseSongs(MockDataJsonParser.getFileContent("mock/songs.json")).forEach { storage.createSong(it) }
        MockDataJsonParser.parseUsers("mock/users.json").forEach { storage.createUser(it) }
    }

    @After
    fun tearDown() {
        storage.db.transaction {
            executeStatement(Songs.dropStatement)
            executeStatement(Users.dropStatement)
            executeStatement(CorrelationCoefficients.dropStatement)
            executeStatement(Reviews.dropStatement)
            executeStatement(Recommendations.dropStatement)
        }
    }

    private fun removeAllReviews() {
        storage.db.transaction {
            deleteFrom(Reviews).execute()
        }
    }

    private fun insertRandomReviews() {
        removeAllReviews()
        val r = Random()
        ur1.shuffled().take(r.nextInt(100)).forEach { storage.createReview(it, 1) }
        ur2.shuffled().take(r.nextInt(100)).forEach { storage.createReview(it, 2) }
        ur3.shuffled().take(r.nextInt(100)).forEach { storage.createReview(it, 3) }
        ur4.shuffled().take(r.nextInt(100)).forEach { storage.createReview(it, 4) }
        ur5.shuffled().take(r.nextInt(100)).forEach { storage.createReview(it, 5) }
        ur6.shuffled().take(r.nextInt(100)).forEach { storage.createReview(it, 6) }
    }


    private fun List<CorrelationCoeficient>.isSpearmanRangeValid() =
            all { it.spearmanCoeficient >= -1 || it.spearmanCoeficient <= 1 }

    private fun testSameUsers(revCountUser1: Int, revCountUser2: Int = revCountUser1) {
        removeAllReviews()
        ur1.take(revCountUser1).forEach { storage.createReview(it, 1) }
        ur6.take(revCountUser2).forEach { storage.createReview(it, 6) }
        storage.updateSpearmanCoefficients()

        val sc1 = storage.getSpearmanCoefficient(1, 6)
        val sc6 = storage.getSpearmanCoefficient(6, 1)
        Assert.assertEquals(0, sc1.spearmanCoeficient.compareTo(1))
        Assert.assertEquals(0, sc6.spearmanCoeficient.compareTo(1))
        Assert.assertEquals(0.0, sc1.distance, 10E-38)
        Assert.assertEquals(0.0, sc6.distance, 10E-38)

        val sc3 = storage.getSpearmanCoefficients()
        Assert.assertEquals(true, sc3.isSpearmanRangeValid())
        Assert.assertEquals(2, sc3.size)  // now there are two records for every pair
    }

    @Test
    fun sameUsersOnlyTest() {
        for (i in 2..100 step 20) {
            testSameUsers(i)
        }
    }

    @Test
    fun sameUsersDifferentReviewsCountTest() {
        for (i in 4..100 step 20) {
            testSameUsers(i, i / 2)
        }
    }

    @Test
    fun rangeTest() {
        for (i in 0 until 10) {
            insertRandomReviews()
            storage.updateSpearmanCoefficients()
            val sc = storage.getSpearmanCoefficients()
            Assert.assertEquals(true, sc.isSpearmanRangeValid())
        }
    }
}