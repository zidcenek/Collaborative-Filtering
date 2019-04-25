package cz.cvut.fit.vwm.collaborativefiltering

import cz.cvut.fit.vwm.collaborativefiltering.db.DatabaseInteractor
import cz.cvut.fit.vwm.collaborativefiltering.route.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie

data class Session(val userId: String)

@KtorExperimentalLocationsAPI
fun Application.main() {
    val storage = DatabaseInteractor()

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(Compression)
    install(Locations)
    install(PartialContent)
    install(ContentNegotiation) { gson() }
    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
    }

    install(Sessions) {
        cookie<Session>(name = "SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    fillDbWithMockData(storage)

    routing {
        index(storage)
        songs(storage)
        users(storage)
        reviews(storage)
        rank(storage)
    }
}

