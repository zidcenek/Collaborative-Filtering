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


fun RBuilder.registerComponent(handler: RHandler<LoginComponent.UserProps> = {}) = child(RegisterComponent::class, handler)

class RegisterComponent : RComponent<LoginComponent.UserProps, RegisterComponent.RegisterFormState>() {

    init {
        state = RegisterFormState("", "", "", "", null, false)
    }

    private enum class RegisterForm(val placeholder: String, val inputType: InputType = InputType.text) {
        NAME("Name"),
        SURNAME("Surname"),
        EMAIL("E-mail"),
        PASSWORD("Password", InputType.password)
    }

    override fun RBuilder.render() {

        div {
            form(classes = "pure-form pure-form-stacked") {
                legend { +"Register" }

                fieldSet(classes = "pure-group") {
                    RegisterForm.values().forEach {
                        input(type = it.inputType, name = it.name.toLowerCase()) {
                            attrs {
                                value = when (it) {
                                    RegisterForm.NAME -> state.name
                                    RegisterForm.SURNAME -> state.surname
                                    RegisterForm.EMAIL -> state.email
                                    RegisterForm.PASSWORD -> state.password
                                }
                                placeholder = it.placeholder
                                disabled = state.disabled

                                onChangeFunction = { e ->
                                    val value = e.inputValue
                                    setState {
                                        when (it) {
                                            RegisterForm.NAME -> name = value
                                            RegisterForm.SURNAME -> surname = value
                                            RegisterForm.EMAIL -> email = value
                                            RegisterForm.PASSWORD -> password = value
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
                    +"Register"
                    attrs.disabled = state.disabled

                    attrs.onClickFunction = {
                        doRegister()
                        it.preventDefault()
                    }
                }
            }
        }
    }

    private fun doRegister() {
        setState {
            disabled = true
        }
        async {
            with(state) {
                val user = UserRpc.register(name, surname, email, password)
                registered(user)
            }
        }.catch { err -> registrationFailed(err) }
    }

    private fun registered(user: User) {
        props.userAssigned(user)
    }

    private fun registrationFailed(err: Throwable) {
        if (err is LoginOrRegisterFailedException) {
            setState {
                errorMessage = err.message
                disabled = false
            }
        } else {
            console.log("Registration failed", err)
            setState {
                errorMessage = "Registration failed"
            }
        }
    }

    class RegisterFormState(var name: String,
                            var surname: String,
                            var email: String,
                            var password: String,
                            var errorMessage: String?,
                            var disabled: Boolean) : RState
}
