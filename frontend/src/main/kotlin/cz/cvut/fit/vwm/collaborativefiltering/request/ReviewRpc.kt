package cz.cvut.fit.vwm.collaborativefiltering.request

import cz.cvut.fit.vwm.collaborativefiltering.ReviewParseError
import cz.cvut.fit.vwm.collaborativefiltering.model.Review
import kotlin.js.json

object ReviewRpc {

    suspend fun create(songId: Int, value: Int): Review =
            postJsonAndParseResult("/reviews", json(
                    "songId" to songId,
                    "value" to value
            ), ::parseReviewResponse)

    suspend fun getReview(reviewId: Int): Review =
            getAndParseResult("/reviews/$reviewId", null, ::parseReviewResponse)

    suspend fun getReviews(): List<Review> =
            getAndParseResult("/reviews", null, ::parseReviewsResponse)

    suspend fun update(reviewId: Int, value: Int): Review =
            putJsonAndParseResult("/reviews/$reviewId", json("value" to value), ::parseReviewResponse)

    suspend fun delete(reviewId: Int): Unit =
            deleteAndParseResult("/reviews/$reviewId", null) { Unit }

    private fun parseReviewResponse(json: dynamic): Review {
        if (json == null) {
            throw ReviewParseError("Error reading review.")
        }
        return parseReviewObject(json.review)
    }

    private fun parseReviewsResponse(json: dynamic): List<Review> {
        if (json == null) {
            throw ReviewParseError("Error reading review.")
        }
        val arr = json.reviews as Array<dynamic>
        return arr.map(::parseReviewObject)
    }

    private fun parseReviewObject(data: dynamic): Review {
        if (data == null) {
            throw ReviewParseError("Error reading review.")
        }
        return Review(data.id, data.songId, data.value)
    }
}