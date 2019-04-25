package cz.cvut.fit.vwm.collaborativefiltering.model

data class User(val id: Int, val name: String, val surname: String, val email: String, val password: String)
data class Song(val id: Int = 0, val mbid: String?, val artist: String, val title: String, val lastFmRank: Int? = null, val url: String? = null)