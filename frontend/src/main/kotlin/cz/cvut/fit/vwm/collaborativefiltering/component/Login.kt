package cz.cvut.fit.vwm.collaborativefiltering.component

import cz.cvut.fit.vwm.collaborativefiltering.LoginOrRegisterFailedException
import cz.cvut.fit.vwm.collaborativefiltering.async
import cz.cvut.fit.vwm.collaborativefiltering.inputValue
import cz.cvut.fit.vwm.collaborativefiltering.model.User
import cz.cvut.fit.vwm.collaborativefiltering.request.UserRpc
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*

fun RBuilder.loginComponent(handler: RHandler<LoginComponent.UserProps> = {}) = child(LoginComponent::class, handler)

class LoginComponent : RComponent<LoginComponent.UserProps, LoginComponent.LoginFormState>() {

    init {
        state = LoginFormState("", "", "", false)
    }

    private enum class LoginForm(val placeholder: String, val inputType: InputType = InputType.text) {
        EMAIL("E-mail"),
        PASSWORD("Password", InputType.password)
    }

    override fun RBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legend { +"Login" }

                fieldSet(classes = "pure-group") {
                    LoginForm.values().forEach {
                        input(type = it.inputType, name = it.name.toLowerCase()) {
                            attrs {
                                value = when (it) {
                                    LoginForm.EMAIL -> state.email
                                    LoginForm.PASSWORD -> state.password
                                }
                                placeholder = it.placeholder
                                disabled = state.disabled

                                onChangeFunction = { e ->
                                    val value = e.inputValue
                                    setState {
                                        when (it) {
                                            LoginForm.EMAIL -> email = value
                                            LoginForm.PASSWORD -> password = value
                                        }
                                    }
                                }
                            }
                        }
                    }


                }

                state.errorMessage?.takeIf(String::isNotEmpty)?.let { message ->
                    label {
                        +message
                    }
                }

                button(classes = "pure-button pure-button-primary") {
                    +"Login"
                    attrs.disabled = state.disabled

                    attrs.onClickFunction = {
                        it.preventDefault()
                        doLogin()
                    }
                }
            }
        }
    }

    private fun doLogin() {
        setState {
            disabled = true
        }
        async {
            val user = UserRpc.login(state.email, state.password)
            loggedIn(user)
        }.catch { err -> loginFailed(err) }
    }

    private fun loggedIn(user: User) {
        props.userAssigned(user)
    }

    private fun loginFailed(err: Throwable) {
        if (err is LoginOrRegisterFailedException) {
            setState {
                disabled = false
                errorMessage = err.message
            }
        } else {
            console.error("Login failed", err)
            setState {
                disabled = false
                errorMessage = "Login failed: please reload page and try again"
            }
        }
    }

    class UserProps : RProps {
        var userAssigned: (User) -> Unit = {}
    }

    class LoginFormState(var email: String,
                         var password: String,
                         var errorMessage: String?,
                         var disabled: Boolean) : RState
}
