package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.ReactComponentNoState
import cz.cvut.fit.vwm.collaborativefiltering.launch
import cz.cvut.fit.vwm.collaborativefiltering.model.User
import cz.cvut.fit.vwm.collaborativefiltering.request.UserRpc
import kotlinx.html.UL
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.dom.RDOMBuilder
import react.dom.a
import react.dom.li
import react.dom.ul


fun RBuilder.navBarComponent(handler: RHandler<NavBarComponent.NavBarHandlerProps> = {}) = child(NavBarComponent::class, handler)

class NavBarComponent : RComponent<NavBarComponent.NavBarHandlerProps, ReactComponentNoState>() {

    init {
        state = ReactComponentNoState()
    }

    override fun RBuilder.render() {
        val user = props.user
        ul(classes = "nav-list") {
            if (user != null) {
                navItem("Show songs") {
                    showSongsView()
                }
                navItem("Show recommended songs") {
                    showSongRecommendationView()
                }
                navItem("Sign out, ${user.name} ${user.surname}") {
                    logout()
                }
            } else {
                navItem("Sign up") {
                    register()
                }
                navItem("Sign in") {
                    login()
                }
            }
        }
    }

    private fun showSongsView() {
        props.handler(MainView.Songs)
    }

    private fun showSongRecommendationView() {
        props.handler(MainView.SongRecommendation)
    }

    private fun register() {
        props.handler(MainView.Register)
    }

    private fun login() {
        props.handler(MainView.Login)
    }

    private fun logout() {
        launch {
            UserRpc.logout()
            props.logoutHandler()
        }
    }

    private fun RDOMBuilder<UL>.navItem(title: String, onButtonClick: () -> Unit = {}) {
        li(classes = "nav-item") {
            a(classes = "pure-button", href = "javascript:void(0)") {
                +title

                attrs.onClickFunction = { onButtonClick() }
            }
        }
    }

    class NavBarHandlerProps : RProps {
        var user: User? = null
        var logoutHandler: () -> Unit = {}
        var handler: (MainView) -> Unit = { }
    }
}