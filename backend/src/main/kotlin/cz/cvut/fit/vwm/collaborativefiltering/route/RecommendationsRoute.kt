package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.SongRecommendationsResponse
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
fun Route.recommendations(storage: IDatabaseInteractor) {
    get<RecommendationsLoc> {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId != null) {
            val songs = storage.getUserSongRecommendationsAndIncrementViewedCount(userId)
            call.respond(SongRecommendationsResponse(songs))
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}