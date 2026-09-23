package com.example.sanaesencia_app.session

import com.example.sanaesencia_app.domain.model.RolUsuario

object DemoSession {
    var rol: RolUsuario = RolUsuario.PACIENTE
    var usuarioId: String = "P001"
    var usuarioNombre: String = "Paciente Demo"
    const val terapeutaDemoId = "T001"
}
