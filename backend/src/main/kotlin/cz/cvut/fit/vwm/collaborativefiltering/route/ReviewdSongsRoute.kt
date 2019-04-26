package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.SESSION_USER_NAME
import cz.cvut.fit.vwm.collaborativefiltering.data.model.ReviewedSongsResponse
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Session
import cz.cvut.fit.vwm.collaborativefiltering.db.DatabaseInteractor
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.sessions

@KtorExperimentalLocationsAPI
fun Route.reviewedSongs(storage: DatabaseInteractor) {
    get<ReviewedSongsLoc> {
        val user = (call.sessions.get(SESSION_USER_NAME) as? Session)?.let {
            storage.getUserByEmail(it.userEmail)
        }
        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val songs = storage.getReviewedSongs(user.id)
            call.respond(ReviewedSongsResponse(songs))
        }
    }
}
