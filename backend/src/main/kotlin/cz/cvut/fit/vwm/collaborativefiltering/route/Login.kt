package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.LoginResponse
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Session
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import cz.cvut.fit.vwm.collaborativefiltering.getLoggedUserOrRespondForbidden
import cz.cvut.fit.vwm.collaborativefiltering.isEmailValid
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set

@KtorExperimentalLocationsAPI
fun Route.login(db: IDatabaseInteractor, hash: (String) -> String) {
    get<LoginLoc> {
        val user = getLoggedUserOrRespondForbidden(db) ?: return@get
        call.respond(LoginResponse(user))
    }
    post<LoginLoc> {
        val form = call.receive<Parameters>()
        val email = form["email"] ?: ""
        val password = form["password"] ?: ""
        val login = when {
            email.length < 6 -> null
            password.length < 6 -> null
            !isEmailValid(email) -> null
            else -> db.getUserByEmail(email, hash(password))
        }

        if (login == null) {
            call.respond(LoginResponse(error = "Invalid username or password"))
        } else {
            call.sessions.set(Session(login.email))
            call.respond(LoginResponse(login))
        }
    }
}