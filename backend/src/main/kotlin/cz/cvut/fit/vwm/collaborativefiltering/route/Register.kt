package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.LoginResponse
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Session
import cz.cvut.fit.vwm.collaborativefiltering.data.model.User
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import cz.cvut.fit.vwm.collaborativefiltering.isEmailValid
import cz.cvut.fit.vwm.collaborativefiltering.redirect
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set


@KtorExperimentalLocationsAPI
fun Route.register(db: IDatabaseInteractor) {
    get<RegisterLoc> {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
    post<RegisterLoc> {
        val userEmail = call.sessions.get<Session>()?.let {
            db.getUserByEmail(it.userEmail)
        }
        if (userEmail != null) {
            call.redirect(LoginResponse(userEmail))
        } else {
            val form = call.receive<Parameters>()
            val name = form["name"] ?: ""
            val surname = form["surname"] ?: ""
            val email = form["email"] ?: ""
            val password = form["password"] ?: ""

            if (name.isEmpty() || surname.isEmpty()) {
                call.respond(LoginResponse(error = "Name and surname must be not be empty."))
            } else if (password.length < 6) {
                call.respond(LoginResponse(error = "Password should be at least 6 characters long"))
            } else if (email.length < 6) {
                call.respond(LoginResponse(error = "Email should be at least 6 characters long"))
            } else if (!isEmailValid(email)) {
                call.respond(LoginResponse(error = "Email must be in the following format: email@domin.com"))
            } else if (db.getUserByEmail(email) != null) {
                call.respond(LoginResponse(error = "User with email $email is already registered"))
            } else {
                val newUser = User(0, name, surname, email, password)
                db.createUser(newUser)
                call.sessions.set(Session(newUser.email))
                call.respond(LoginResponse(newUser))
            }
        }
    }
}
