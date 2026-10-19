package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.remote.LoginResponse
import cl.sanaesencia.app.data.repository.AuthRepository
import cl.sanaesencia.app.domain.validacion.ValidadorEmail
import cl.sanaesencia.app.domain.validacion.ValidadorPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: AuthRepository) : BaseViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val state = MutableStateFlow<UiState<LoginResponse>>(UiState.Vacio)
    private val _errors = MutableStateFlow(LoginErrors())
    val errors: StateFlow<LoginErrors> = _errors.asStateFlow()

    fun onEmailChanged(value: String) { email.value = value; if (email.value.isNotBlank()) validateEmail() }
    fun onPasswordChanged(value: String) { password.value = value; if (password.value.isNotBlank()) validatePassword() }
    fun validateEmail() { _errors.value = _errors.value.copy(email = invalid(ValidadorEmail()(email.value))) }
    fun validatePassword() { _errors.value = _errors.value.copy(password = invalid(ValidadorPassword()(password.value))) }
    fun login() {
        validateEmail(); validatePassword()
        if (_errors.value.hasErrors) return
        viewModelScope.launch { ejecutar(state) { repo.login(email.value.trim(), password.value) } }
    }

    fun resetState() {
        email.value = ""
        password.value = ""
        state.value = UiState.Vacio
        _errors.value = LoginErrors()
    }

    private fun invalid(result: cl.sanaesencia.app.domain.validacion.ResultadoValidacion) =
        (result as? cl.sanaesencia.app.domain.validacion.ResultadoValidacion.Invalido)?.mensaje
}

data class LoginErrors(val email: String? = null, val password: String? = null) {
    val hasErrors get() = email != null || password != null
}
