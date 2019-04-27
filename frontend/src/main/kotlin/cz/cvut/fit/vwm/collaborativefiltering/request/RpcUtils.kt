package cz.cvut.fit.vwm.collaborativefiltering.request

import cz.cvut.fit.vwm.collaborativefiltering.await
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import kotlin.browser.window
import kotlin.js.Json
import kotlin.js.json

internal suspend fun delete(url: String, body: dynamic) = request("DELETE", url, body)

internal suspend fun <T> putJsonAndParseResult(url: String, body: Json, parse: (dynamic) -> T): T =
        requestJsonAndParseResult("PUT", url, body, parse)

internal suspend fun <T> postJsonAndParseResult(url: String, body: Json, parse: (dynamic) -> T): T =
        requestJsonAndParseResult("POST", url, body, parse)

internal suspend fun <T> postAndParseResult(url: String, body: dynamic, parse: (dynamic) -> T): T =
        requestAndParseResult("POST", url, body, parse)

internal suspend fun <T> getAndParseResult(url: String, body: dynamic, parse: (dynamic) -> T): T =
        requestAndParseResult("GET", url, body, parse)

internal suspend fun <T> requestAndParseResult(method: String, url: String, body: dynamic, parse: (dynamic) -> T): T {
    val response = window.fetch(url, object : RequestInit {
        override var method: String? = method
        override var body: dynamic = body
        override var credentials: RequestCredentials? = "same-origin".asDynamic()
        override var headers: dynamic = json("Accept" to "application/json")
    }).await()
    return parse(response.json().await())
}

internal suspend fun request(method: String, url: String, body: dynamic) = window.fetch(url, object : RequestInit {
    override var method: String? = method
    override var body: dynamic = body
    override var credentials: RequestCredentials? = "same-origin".asDynamic()
}).await()

internal suspend fun <T> requestJsonAndParseResult(method: String, url: String, body: Json, parse: (dynamic) -> T): T {
    val response = window.fetch(url, object : RequestInit {
        override var method: String? = method
        override var body = JSON.stringify(body)
        override var credentials: RequestCredentials? = "same-origin".asDynamic()
        override var headers: dynamic = json(
                "Content-Type" to "application/json")
    }).await()
    return parse(response.json().await())
}