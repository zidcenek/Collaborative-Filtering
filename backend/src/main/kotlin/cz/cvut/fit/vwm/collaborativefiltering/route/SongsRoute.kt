package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.SongsResponse
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.contentType

@KtorExperimentalLocationsAPI
fun Route.songs(storage: IDatabaseInteractor) {
    get<SongsLoc> {
        val songs = storage.getSongs()
        call.respond(SongsResponse(songs))
    }
    contentType(ContentType.Application.Json) {
        get<SongsLoc> {
            val songs = storage.getSongs()
            println("songs: ${songs.size}")
            call.respond(SongsResponse(songs))
        }
    }
}
