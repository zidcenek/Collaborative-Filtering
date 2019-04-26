package cz.cvut.fit.vwm.collaborativefiltering.request

import cz.cvut.fit.vwm.collaborativefiltering.LoginOrRegisterFailedException
import cz.cvut.fit.vwm.collaborativefiltering.await
import cz.cvut.fit.vwm.collaborativefiltering.model.User
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import kotlin.browser.window

object UserRpc {
    suspend fun login(email: String, password: String): User =
            postAndParseResult("/login", URLSearchParams().apply {
                append("email", email)
                append("password", password)
            }, ::parseLoginOrRegisterResponse)

    private fun parseLoginOrRegisterResponse(json: dynamic): User {
        if (json.error != null) {
            throw LoginOrRegisterFailedException(json.error.toString())
        }

        return User(json.user.id, json.user.name, json.user.surname, json.user.email, json.user.password)
    }

    suspend fun register(name: String, surname: String, email: String, password: String): User =
            postAndParseResult("/register", URLSearchParams().apply {
                append("name", name)
                append("surname", surname)
                append("email", email)
                append("password", password)
            }, ::parseLoginOrRegisterResponse)

    suspend fun logout() {
        window.fetch("/logout", object : RequestInit {
            override var method: String? = "POST"
            override var credentials: RequestCredentials? = "same-origin".asDynamic()
        }).await()
    }

    suspend fun checkSession(): User =
            getAndParseResult("/login", null, ::parseLoginOrRegisterResponse)

}