package cz.cvut.fit.vwm.collaborativefiltering.data.model

interface IResponse

data class SongsResponse(val songs: List<Song>) : IResponse
data class UsersResponse(val users: List<User>) : IResponse
