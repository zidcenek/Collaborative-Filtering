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
@Location("/reviews")
class ReviewsLoc

@KtorExperimentalLocationsAPI
@Location("/rank")
class RankLoc