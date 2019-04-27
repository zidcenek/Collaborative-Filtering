package cz.cvut.fit.vwm.collaborativefiltering

import cz.cvut.fit.vwm.collaborativefiltering.data.json.MockDataJsonParser
import cz.cvut.fit.vwm.collaborativefiltering.data.model.Session
import cz.cvut.fit.vwm.collaborativefiltering.data.model.User
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.sessions.sessions
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import io.ktor.util.pipeline.PipelineContext
import java.net.URL
import java.net.UnknownHostException
import java.util.regex.Pattern
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@KtorExperimentalAPI
val hashKey = hex("c11cae575f8e4058662a5ef3c0c2bc")
@KtorExperimentalAPI
val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")

@KtorExperimentalAPI
fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}


fun fillDbWithMockData(storage: IDatabaseInteractor) {
    if (storage.getSongsCount() == 0) {
        try {
            val url = "https://ws.audioscrobbler.com/2.0/?method=user.gettoptracks&user=rj&api_key=$LAST_FM_API_KEY&format=json&limit=100"
            MockDataJsonParser.parseSongs(URL(url).readText()).forEach { storage.createSong(it) }
        } catch (e: UnknownHostException) {
            MockDataJsonParser.parseSongs(MockDataJsonParser.getFileContent("mock/songs.json")).forEach { storage.createSong(it) }
        }
    }
    if (storage.getUsersCount() == 0) {
        MockDataJsonParser.parseUsers("mock/users.json").take(7).forEach { storage.createUser(it) }
    }
    if (storage.getReviewsCount() == 0) {
        MockDataJsonParser.praseReview("mock/reviewUser1.json").take(1).forEach { storage.createReview(it, 1) }
        MockDataJsonParser.praseReview("mock/reviewUser2.json").take(2).forEach { storage.createReview(it, 2) }
        MockDataJsonParser.praseReview("mock/reviewUser3.json").take(3).forEach { storage.createReview(it, 3) }
        MockDataJsonParser.praseReview("mock/reviewUser4.json").take(4).forEach { storage.createReview(it, 4) }
        MockDataJsonParser.praseReview("mock/reviewUser5.json").take(5).forEach { storage.createReview(it, 5) }
        MockDataJsonParser.praseReview("mock/reviewUser6.json").take(5).forEach { storage.createReview(it, 6) }
        MockDataJsonParser.praseReview("mock/reviewUser7.json").take(5).forEach { storage.createReview(it, 7) }
    }
}

// source: https://gist.github.com/ironic-name/f8e8479c76e80d470cacd91001e7b45b
fun isEmailValid(email: String): Boolean {
    return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    ).matcher(email).matches()
}


@KtorExperimentalLocationsAPI
suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host()
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.feature(Locations).href(location)}")
}

fun PipelineContext<Unit, ApplicationCall>.getLoggedUser(storage: IDatabaseInteractor): User? =
        (call.sessions.get(SESSION_USER_NAME) as? Session)?.let {
            storage.getUserByEmail(it.userEmail)
        }

suspend fun PipelineContext<Unit, ApplicationCall>.getLoggedUserOrRespondForbidden(storage: IDatabaseInteractor): User? {
    val user = getLoggedUser(storage)
    return if (user == null) {
        call.respond(HttpStatusCode.Forbidden)
        null
    } else {
        user
    }
}