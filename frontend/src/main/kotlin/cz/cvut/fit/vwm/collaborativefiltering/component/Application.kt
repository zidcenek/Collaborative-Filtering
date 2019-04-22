package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.ReactComponentNoProps
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.div
import react.dom.nav

fun RBuilder.application(handler: RHandler<ReactComponentNoProps> = {}) = child(Application::class, handler)

class Application : RComponent<ReactComponentNoProps, Application.ApplicationPageState>() {

    init {
        state = ApplicationPageState(MainView.Home)
    }

    override fun RBuilder.render() {
        div("pure-g") {
            div("sidebar pure-u-1 pure-u-md-1-4") {
                div("header") {
                    div("brand-title") {
                        +"Collborative filtering"

                        if (state.selected != MainView.Loading) {
                            attrs.onClickFunction = { mainViewSelected() }
                        }
                    }
                    nav("nav") {
                        if (state.selected != MainView.Loading) {
                            navBarComponent {
                                attrs.handler = { navBarSelected(it) }
                            }
                        }
                    }
                }
            }

            div("content pure-u-1 pure-u-md-3-4") {
                when (state.selected) {
                    MainView.Songs -> songListComponent()
                    else -> {
                    }
                }
            }

            div("footer") {
                +"FIT CTU | BI-VWM | Collaborative filtering"
            }
        }
    }

    private fun navBarSelected(newSelected: MainView) {
        setState {
            selected = newSelected
        }
    }

    private fun mainViewSelected() {
        setState {
            selected = MainView.Home
        }
    }

    class ApplicationPageState(var selected: MainView) : RState
}

enum class MainView {
    Loading,
    Home,
    Songs
}