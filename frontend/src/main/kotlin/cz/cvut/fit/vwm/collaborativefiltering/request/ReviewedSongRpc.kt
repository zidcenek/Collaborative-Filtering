package cz.cvut.fit.vwm.collaborativefiltering.request

import cz.cvut.fit.vwm.collaborativefiltering.model.ReviewedSong
import cz.cvut.fit.vwm.collaborativefiltering.model.Song
import cz.cvut.fit.vwm.collaborativefiltering.model.Review

object ReviewedSongRpc {
    suspend fun getList(): List<ReviewedSong> = getAndParseResult("/songs/reviewed", null, ::parseSongListResponse)

    private fun parseSongListResponse(json: dynamic): List<ReviewedSong> {
        if (json.error != null) {
            throw Exception(json.error.toString())
        }
        val data = json.reviewedSongs as Array<dynamic>
        return data.map(::parseReviewedSong)
    }

    private fun parseReviewedSong(json: dynamic) =
        ReviewedSong(parseSong(json.song), parseReview(json.review))

    private fun parseSong(json: dynamic) = Song(json.id, json.mbid, json.artist, json.title, json.lastFmRank, json.url)
    private fun parseReview(json: dynamic) =
            if ( json == null )
                null
            else
                Review(json.id, json.songId, json.value)
}