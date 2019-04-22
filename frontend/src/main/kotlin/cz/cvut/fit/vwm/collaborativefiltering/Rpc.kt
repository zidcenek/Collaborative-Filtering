package cz.cvut.fit.vwm.collaborativefiltering

import cz.cvut.fit.vwm.collaborativefiltering.model.Song
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import kotlin.browser.window
import kotlin.js.json

suspend fun getSongList(): List<Song> = getAndParseResult("/songs", null, ::parseSongListResponse)

private fun parseSongListResponse(json: dynamic): List<Song> {
    if (json.error != null) {
        throw Exception(json.error.toString())
    }

    val data = json.songs as Array<dynamic>
    return data.map(::parseSong)
}

fun parseSong(json: dynamic) = Song(json.id, json.mbid, json.artist, json.title, json.lastFmRank, json.url)

suspend fun <T> postAndParseResult(url: String, body: dynamic, parse: (dynamic) -> T): T =
        requestAndParseResult("POST", url, body, parse)

suspend fun <T> getAndParseResult(url: String, body: dynamic, parse: (dynamic) -> T): T =
        requestAndParseResult("GET", url, body, parse)

suspend fun <T> requestAndParseResult(method: String, url: String, body: dynamic, parse: (dynamic) -> T): T {
    val response = window.fetch(url, object : RequestInit {
        override var method: String? = method
        override var body: dynamic = body
        override var credentials: RequestCredentials? = "same-origin".asDynamic()
        override var headers: dynamic = json("Accept" to "application/json")
    }).await()
    return parse(response.json().await())
}
