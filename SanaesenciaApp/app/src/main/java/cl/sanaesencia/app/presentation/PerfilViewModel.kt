package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope
import cl.sanaesencia.app.data.remote.ActualizarPerfilRequest
import cl.sanaesencia.app.data.remote.PerfilResponse
import cl.sanaesencia.app.data.remote.mensajeUsuario
import cl.sanaesencia.app.data.repository.AuthRepository
import cl.sanaesencia.app.domain.validacion.ResultadoValidacion
import cl.sanaesencia.app.domain.validacion.ValidadorFechaNacimiento
import cl.sanaesencia.app.domain.validacion.ValidadorNombre
import cl.sanaesencia.app.domain.validacion.ValidadorRut
import cl.sanaesencia.app.domain.validacion.ValidadorTelefonoChile
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repo: AuthRepository
) : BaseViewModel() {

    companion object {
        val FECHA_UI: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    val state =
        MutableStateFlow<UiState<PerfilResponse>>(UiState.Cargando)

    val message =
        MutableStateFlow<String?>(null)

    private val _errores =
        MutableStateFlow(PerfilValidationState())

    val errores: StateFlow<PerfilValidationState> =
        _errores.asStateFlow()

    fun cargar() =
        viewModelScope.launch {
            ejecutar(state) {
                repo.perfil()
            }
        }

    fun validarCampoRut(v: String) {
        _errores.value = _errores.value.copy(
            rut = invalid(
                ValidadorRut()(v)
            )
        )
    }

    fun validarCampoNombre(v: String) {
        _errores.value = _errores.value.copy(
            nombre = invalid(
                ValidadorNombre()(v)
            )
        )
    }

    fun validarCampoTelefono(v: String) {
        _errores.value = _errores.value.copy(
            telefono = invalid(
                ValidadorTelefonoChile()(v)
            )
        )
    }

    fun validarCampoFechaNacimiento(v: String) {

        val date = runCatching {
            LocalDate.parse(v, FECHA_UI)
        }.getOrNull()

        _errores.value = _errores.value.copy(
            fechaNacimiento = invalid(
                ValidadorFechaNacimiento()(
                    date,
                    Clock.system(
                        ZoneId.of("America/Santiago")
                    )
                )
            )
        )
    }

    fun validarConsentimiento(v: Boolean) {
        _errores.value = _errores.value.copy(
            consentimiento =
                if (v) {
                    null
                } else {
                    "Debes aceptar el consentimiento para guardar datos de salud"
                }
        )
    }

    fun guardar(
        rut: String,
        nombre: String,
        telefono: String,
        fechaNacimiento: String,
        consentimiento: Boolean
    ) {

        validarCampoRut(rut)
        validarCampoNombre(nombre)
        validarCampoTelefono(telefono)
        validarCampoFechaNacimiento(fechaNacimiento)
        validarConsentimiento(consentimiento)

        val date = runCatching {
            LocalDate.parse(
                fechaNacimiento,
                FECHA_UI
            )
        }.getOrNull()

        if (
            _errores.value.tieneErrores ||
            date == null
        ) {
            message.value =
                "Revisa los campos marcados antes de guardar"
            return
        }

        /*
         * La interfaz utiliza dd/MM/yyyy,
         * mientras que el backend recibe la fecha como String.
         * Se envía en formato ISO: yyyy-MM-dd.
         */
        val fechaBackend =
            date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        viewModelScope.launch {

            runCatching {

                repo.actualizarPerfil(
                    ActualizarPerfilRequest(
                        rut = rut,
                        nombreCompleto = nombre.trim(),
                        telefono = telefono,
                        fechaNacimiento = fechaBackend,
                        consentimientoSalud = consentimiento
                    )
                )

            }.onSuccess {

                state.value =
                    UiState.Exito(it)

                message.value =
                    "Perfil actualizado"

            }.onFailure {

                message.value =
                    it.mensajeUsuario("No fue posible actualizar el perfil")
            }
        }
    }

    fun clearMessage() {
        message.value = null
    }

    private fun invalid(
        result: ResultadoValidacion
    ): String? {
        return (
                result as? ResultadoValidacion.Invalido
                )?.mensaje
    }
}

data class PerfilValidationState(
    val rut: String? = null,
    val nombre: String? = null,
    val telefono: String? = null,
    val fechaNacimiento: String? = null,
    val consentimiento: String? = null
) {
    val tieneErrores: Boolean
        get() = listOf(
            rut,
            nombre,
            telefono,
            fechaNacimiento,
            consentimiento
        ).any { it != null }
}