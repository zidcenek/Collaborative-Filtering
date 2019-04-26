package cz.cvut.fit.vwm.collaborativefiltering.route

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

@KtorExperimentalLocationsAPI
@Location("/users")
class UsersLoc

@KtorExperimentalLocationsAPI
@Location("/songs")
class SongsLoc

@KtorExperimentalLocationsAPI
@Location("/songs/reviewed")
class ReviewedSongsLoc

@KtorExperimentalLocationsAPI
@Location("/reviews")
class ReviewsLoc

@KtorExperimentalLocationsAPI
@Location("/rank")
class RankLoc

@KtorExperimentalLocationsAPI
@Location("/recommendations/{userId}")
data class RecommendationsLoc(val userId: Int)

@KtorExperimentalLocationsAPI
@Location("/")
class Index

@KtorExperimentalLocationsAPI
@Location("/register")
class RegisterLoc(val userId: String = "", val displayName: String = "", val email: String = "", val password: String = "", val error: String = "")

@KtorExperimentalLocationsAPI
@Location("/login")
class LoginLoc(val userId: String = "", val password: String = "", val error: String = "")


@KtorExperimentalLocationsAPI
@Location("/logout")
class LogoutLoc
