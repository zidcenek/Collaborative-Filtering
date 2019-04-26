package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.ReactComponentNoProps
import cz.cvut.fit.vwm.collaborativefiltering.async
import cz.cvut.fit.vwm.collaborativefiltering.model.User
import cz.cvut.fit.vwm.collaborativefiltering.request.UserRpc
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.div
import react.dom.nav

fun RBuilder.application(handler: RHandler<ReactComponentNoProps> = {}) = child(Application::class, handler)

class Application : RComponent<ReactComponentNoProps, Application.ApplicationPageState>() {

    init {
        state = ApplicationPageState(MainView.Home)
        checkUserSession()
    }

    private fun checkUserSession() {
        async {
            val user = UserRpc.checkSession()
            onUserAssigned(user)
        }.catch {
            setState {
                selected = MainView.Home
            }
        }
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
                                attrs {
                                    handler = { navBarSelected(it) }
                                    user = state.currentUser
                                    handler = { navBarSelected(it) }
                                    logoutHandler = { onLoggedOut() }
                                }
                            }
                        }
                    }
                }
            }

            div("content pure-u-1 pure-u-md-3-4") {
                when (state.selected) {
                    MainView.Songs -> songListComponent()
                    MainView.Login -> loginComponent {
                        attrs.userAssigned = { onUserAssigned(it) }
                    }
                    MainView.Register -> registerComponent {
                        attrs.userAssigned = { onUserAssigned(it) }
                    }
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

    private fun onUserAssigned(user: User) {
        setState {
            currentUser = user
            selected = MainView.Home
        }
    }

    private fun onLoggedOut() {
        val oldSelected = state.selected

        setState {
            currentUser = null
            selected = when (oldSelected) {
                MainView.Home, MainView.Login, MainView.Register -> oldSelected
                else -> MainView.Home
            }
        }
    }

    class ApplicationPageState(var selected: MainView, var currentUser: User? = null) : RState
}

enum class MainView {
    Loading,
    Home,
    Songs,
    Login,
    Register
}