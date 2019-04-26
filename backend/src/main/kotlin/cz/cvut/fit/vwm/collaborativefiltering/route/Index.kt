package cz.cvut.fit.vwm.collaborativefiltering.route

import cz.cvut.fit.vwm.collaborativefiltering.ApplicationPage
import cz.cvut.fit.vwm.collaborativefiltering.db.IDatabaseInteractor
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.ContentType
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.get


fun Route.index(storage: IDatabaseInteractor) {
    accept(ContentType.Text.Html) {
        get {
            call.respondHtmlTemplate(ApplicationPage()) {
                caption { +"Collaborative filtering" }
            }
        }
    }
}