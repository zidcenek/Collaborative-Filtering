package cz.cvut.fit.vwm.collaborativefiltering.data.model

data class Session(val userEmail: String)
data class User(val id: Int, val name: String, val surname: String, val email: String, val password: String)
data class Song(val id: Int = 0, val mbid: String?, val artist: String, val title: String, val lastFmRank: Int? = null, val url: String? = null)
data class Recommendation(val id: Int, val userId: Int, val songId: Int, val viewed: Boolean)
data class Review(val id: Int, val userId: Int, val songId: Int, val value: Int, val rank: Int? = null)
data class CorrelationCoeficient(val id: Int, val userId1: Int, val userId2: Int, val distance: Int, val spearmanCoeficient: Double)
