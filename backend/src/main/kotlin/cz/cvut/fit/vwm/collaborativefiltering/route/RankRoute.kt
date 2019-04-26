package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.data.model.RanksUpdatedResponse
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
fun Route.rank(storage: IDatabaseInteractor) {
    get<RankLoc> {
        storage.updateSpearmanCoefficients()
        call.respond(RanksUpdatedResponse(true))
    }
}
