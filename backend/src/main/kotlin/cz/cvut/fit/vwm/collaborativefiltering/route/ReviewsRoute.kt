package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.Review
import cz.cvut.fit.vwm.collaborativefiltering.data.model.ReviewResponse
import cz.cvut.fit.vwm.collaborativefiltering.data.model.ReviewsResponse
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import cz.cvut.fit.vwm.collaborativefiltering.getLoggedUserOrRespondForbidden
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.contentType
import io.ktor.util.pipeline.PipelineContext

@KtorExperimentalLocationsAPI
fun Route.reviews(storage: IDatabaseInteractor) {
    get<ReviewsLoc> {
        val user = getLoggedUserOrRespondForbidden(storage) ?: return@get
        val reviews = storage.getUserReviews(user.id)
        call.respond(ReviewsResponse(reviews))
    }
    get<ReviewLoc> {
        println("review loc get")
        val user = getLoggedUserOrRespondForbidden(storage) ?: return@get
        val dbReview = getReviewOrRespondError(storage) ?: return@get
        if (dbReview.userId != user.id) { // logged user trying to read someone else's rating
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        try {
            call.respond(ReviewResponse(dbReview))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    contentType(ContentType.Application.Json) {
        post<AddReviewLoc> {
            val user = getLoggedUserOrRespondForbidden(storage) ?: return@post

            val review = call.receive<Review>()
            if (review.songId == 0) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            try {
                val id = storage.createReview(review, user.id)
                val reviewDb = storage.getReview(id)!!
                call.respond(ReviewResponse(reviewDb))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict)
            }

        }
        put<ReviewLoc> {
            val user = getLoggedUserOrRespondForbidden(storage) ?: return@put
            val newReview = call.receiveOrNull<Review>()
            if (newReview == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val dbReview = getReviewOrRespondError(storage) ?: return@put
            if (dbReview.userId != user.id) { // logged user trying to update someone else's rating
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            println(newReview)
            try {
                dbReview.value = newReview.value
                storage.updateReview(dbReview)
                call.respond(ReviewResponse(dbReview))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotModified)
            }
        }
    }
    delete<ReviewLoc> {
        val user = getLoggedUserOrRespondForbidden(storage) ?: return@delete
        val dbReview = getReviewOrRespondError(storage) ?: return@delete

        if (dbReview.userId != user.id) { // logged user trying to delete someone else's rating
            call.respond(HttpStatusCode.Forbidden)
            return@delete
        }

        try {
            storage.deleteReview(dbReview.id)
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotModified)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getReviewIdOrRespondError(): Int? {
    val reviewId = call.parameters["reviewId"]?.toIntOrNull()
    if (reviewId == null) {
        call.respond(HttpStatusCode.BadRequest)
        return null
    }
    return reviewId
}


private suspend fun PipelineContext<Unit, ApplicationCall>.getReviewOrRespondError(storage: IDatabaseInteractor): Review? {
    val reviewId = getReviewIdOrRespondError() ?: return null
    val dbReview = storage.getReview(reviewId)
    if (dbReview == null) {
        call.respond(HttpStatusCode.NoContent)
        return null
    }
    return dbReview
}
