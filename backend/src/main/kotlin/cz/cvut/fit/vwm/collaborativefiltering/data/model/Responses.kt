package cz.cvut.fit.vwm.collaborativefiltering.data.model

interface IResponse

data class SongsResponse(val songs: List<Song>) : IResponse
data class UsersResponse(val users: List<User>) : IResponse
data class ReviewRespond(val reviews: List<Review>) : IResponse
data class RanksUpdatedResponse(val updated: Boolean) : IResponse
data class LoginResponse(val user: User? = null, val error: String? = null) : IResponse