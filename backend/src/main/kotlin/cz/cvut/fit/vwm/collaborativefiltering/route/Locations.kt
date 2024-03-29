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
@Location("/reviews/")
class AddReviewLoc

@KtorExperimentalLocationsAPI
@Location("/reviews/{reviewId}")
data class ReviewLoc(val reviewId: Int)

@KtorExperimentalLocationsAPI
@Location("/reviews")
class ReviewsLoc

@KtorExperimentalLocationsAPI
@Location("/rank")
class RankLoc

@KtorExperimentalLocationsAPI
@Location("/recommendations")
class RecommendationsLoc

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
