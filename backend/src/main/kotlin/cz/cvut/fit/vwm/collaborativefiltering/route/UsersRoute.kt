package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.UsersResponse
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.contentType

@KtorExperimentalLocationsAPI
fun Route.users(storage: IDatabaseInteractor) {
    get<UsersLoc> {
        val users = storage.getUsers()
        call.respond(UsersResponse(users))
    }
    contentType(ContentType.Application.Json) {
        get<UsersLoc> {
            val users = storage.getUsers()
            println("users: ${users.size}")
            call.respond(UsersResponse(users))
        }
    }
}
