package cz.cvut.fit.vwm.collaborativefiltering.db

import cz.cvut.fit.vwm.collaborativefiltering.data.model.Review
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.data.model.User
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
}
