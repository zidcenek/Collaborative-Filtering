package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.ReactComponentNoState
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
        ul(classes = "nav-list") {
            navItem("Show songs") {
                showSongsView()
            }
        }
    }

    private fun showSongsView() {
        props.handler(MainView.Songs)
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
        var handler: (MainView) -> Unit = { }
    }
}