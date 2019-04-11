package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.ReviewRespond
import cz.cvut.fit.vwm.collaborativefiltering.db.DatabaseInteractor
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.contentType

@KtorExperimentalLocationsAPI
fun Route.reviews(storage: DatabaseInteractor) {
    get<ReviewsLoc> {
        val reviews = storage.getReviews()
        call.respond(ReviewRespond(reviews))
    }
    contentType(ContentType.Application.Json) {
        get<ReviewsLoc> {
            val reviews = storage.getReviews()
            println("reviews: ${reviews.size}")
            call.respond(ReviewRespond(reviews))
        }
    }
}
