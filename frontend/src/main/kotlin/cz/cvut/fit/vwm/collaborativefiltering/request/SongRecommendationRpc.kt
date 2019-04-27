package cz.cvut.fit.vwm.collaborativefiltering.request

import cz.cvut.fit.vwm.collaborativefiltering.model.SongRecommendation
import cz.cvut.fit.vwm.collaborativefiltering.model.Recommendation
import cz.cvut.fit.vwm.collaborativefiltering.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.model.Review

object SongRecommendationRpc {
    suspend fun getList(): List<SongRecommendation> = getAndParseResult("/recommendations", null, ::parseSongListResponse)

    private fun parseSongListResponse(json: dynamic): List<SongRecommendation> {
        if (json.error != null) {
            throw Exception(json.error.toString())
        }
        console.log("parsing")
        console.log(json)
        val data = json.songsRecommendations as Array<dynamic>
        console.log(data)
        return data.map(::parseSongRecommendation)
    }

    private fun parseSongRecommendation(json: dynamic) =
            SongRecommendation(parseSong(json.song), parseRecommendation(json.recommendation))

    private fun parseSong(json: dynamic) = Song(json.id, json.mbid, json.artist, json.title, json.lastFmRank, json.url)
    private fun parseRecommendation(json: dynamic) =
            if ( json == null )
                null
            else
                Recommendation(json.userId, json.songId, json.viewed, json.weight)
}