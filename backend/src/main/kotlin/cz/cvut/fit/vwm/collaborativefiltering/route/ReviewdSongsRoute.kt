package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.ReviewedSongsResponse
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import cz.cvut.fit.vwm.collaborativefiltering.getLoggedUserOrRespondForbidden
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
fun Route.reviewedSongs(storage: IDatabaseInteractor) {
    get<ReviewedSongsLoc> {
        val user = getLoggedUserOrRespondForbidden(storage) ?: return@get
        val songs = storage.getReviewedSongs(user.id)
        call.respond(ReviewedSongsResponse(songs))
    }
}
