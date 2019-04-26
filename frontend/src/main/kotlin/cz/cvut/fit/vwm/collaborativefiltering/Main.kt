package cz.cvut.fit.vwm.collaborativefiltering

import cz.cvut.fit.vwm.collaborativefiltering.component.application
import react.dom.render
import kotlin.browser.document

fun main() {
    kotlinext.js.require("pure-blog.css")

    render(document.getElementById("content")) {
        application()
    }
}