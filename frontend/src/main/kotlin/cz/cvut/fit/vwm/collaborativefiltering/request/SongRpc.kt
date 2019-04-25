package cz.cvut.fit.vwm.collaborativefiltering.request

import cz.cvut.fit.vwm.collaborativefiltering.model.Song

object SongRpc {
    suspend fun getList(): List<Song> = getAndParseResult("/songs", null, ::parseSongListResponse)

    private fun parseSongListResponse(json: dynamic): List<Song> {
        if (json.error != null) {
            throw Exception(json.error.toString())
        }

        val data = json.songs as Array<dynamic>
        return data.map(::parseSong)
    }

    private fun parseSong(json: dynamic) = Song(json.id, json.mbid, json.artist, json.title, json.lastFmRank, json.url)
}