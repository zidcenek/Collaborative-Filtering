package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.Session
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route

import io.ktor.sessions.clear
import io.ktor.sessions.sessions

@KtorExperimentalLocationsAPI
fun Route.logout() {
    post<LogoutLoc> {
        call.sessions.clear<Session>()
        call.respond(HttpStatusCode.OK)
    }
}