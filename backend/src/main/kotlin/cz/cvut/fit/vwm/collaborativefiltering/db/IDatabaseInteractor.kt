package cz.cvut.fit.vwm.collaborativefiltering.db

import cz.cvut.fit.vwm.collaborativefiltering.data.model.*
import java.io.Closeable

interface IDatabaseInteractor : Closeable {
    fun createSong(song: Song): Int
    fun getSong(id: Int): Song
    fun getSongs(): List<Song>
    fun getSongsCount(): Int

    fun createUser(user: User): Int
    fun getUsers(): List<User>
    fun getUsersCount(): Int

    fun createReview(review: Review): Int
    fun getReviews(): List<Review>
    fun getReviewsCount(): Int

    fun updateSpearmanCoefficients()
    fun getSpearmanCoefficient(uid1: Int, uid2: Int): CorrelationCoeficient
    fun getSpearmanCoefficients(userId: Int): List<CorrelationCoeficient>
    fun getSpearmanCoefficients(): List<CorrelationCoeficient>

    fun incrementSongsViewed(recommendations: List<Recommendation>)

    fun updateRecommendations()
    fun getUserSongRecommendations(userId: Int): List<SongRecommendation>
    fun getUserSongRecommendationsAndIncrementViewedCount(userId: Int): List<SongRecommendation>

}
