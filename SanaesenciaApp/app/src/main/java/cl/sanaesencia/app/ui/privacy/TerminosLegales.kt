package cl.sanaesencia.app.ui.privacy

/**
 * Texto de los Términos y Condiciones y de la Política de Privacidad.
 * Se comparte entre la pantalla inicial de consentimiento y el diálogo que se abre desde la app.
 */
data class SeccionLegal(val titulo: String, val texto: String)

object TerminosLegales {

    const val ACTUALIZACION = "Octubre 2026"

    val secciones = listOf(
        SeccionLegal(
            "1. Quiénes somos y para qué usamos tus datos",
            "Centro Integrativo Sanaesencia (Caupolicán 220, Los Vilos) es responsable del tratamiento de los datos " +
                "que ingresas en esta aplicación. Los usamos solo para: crear tu cuenta, agendar y gestionar tus citas, " +
                "registrar tus atenciones, procesar tus pedidos de la tienda y enviarte recordatorios. " +
                "No vendemos ni cedemos tus datos a terceros con fines comerciales.",
        ),
        SeccionLegal(
            "2. Ley N.º 19.628 — Protección de la vida privada",
            "Tus datos personales se tratan con tu consentimiento y solo para las finalidades indicadas. " +
                "Los datos de salud son datos sensibles: únicamente se registran con tu autorización expresa y " +
                "se guardan con medidas de seguridad. Puedes pedir acceso, corrección o eliminación de tus datos.",
        ),
        SeccionLegal(
            "3. Ley N.º 20.584 — Derechos y deberes de los pacientes",
            "Tu ficha e historial de atenciones son confidenciales. Solo pueden verlos tú y los profesionales que te " +
                "atienden; el personal administrativo no accede a las notas clínicas. Tienes derecho a recibir " +
                "información clara sobre tu atención, a un trato digno y a otorgar o revocar tu consentimiento informado.",
        ),
        SeccionLegal(
            "4. Ley N.º 21.719 — Nueva ley de protección de datos personales",
            "Esta ley entra en vigencia el 1 de diciembre de 2026 y la aplicación ya se ajusta a sus principios: " +
                "licitud, finalidad, proporcionalidad, calidad, seguridad, transparencia y confidencialidad. " +
                "Te reconoce los derechos de acceso, rectificación, supresión, oposición, portabilidad y bloqueo, " +
                "y crea la Agencia de Protección de Datos Personales, ante la cual puedes reclamar si no respetamos tus derechos.",
        ),
        SeccionLegal(
            "5. Ubicación",
            "La ubicación es opcional. Solo se usa en tu teléfono para calcular la distancia a la sede y mostrarte " +
                "cómo llegar; no se envía ni se guarda en nuestros servidores. Puedes quitar el permiso cuando quieras " +
                "desde los ajustes de Android.",
        ),
        SeccionLegal(
            "6. Notificaciones",
            "Con tu permiso te enviamos recordatorios de citas, avisos si llevas tiempo sin agendar y promociones " +
                "de la tienda. Puedes desactivarlas desde los ajustes de Android.",
        ),
        SeccionLegal(
            "7. Seguridad y conservación",
            "La comunicación con el servidor usa sesiones cifradas con token (JWT) que expiran. Conservamos tus datos " +
                "mientras tengas una cuenta activa o la ley lo exija para fichas clínicas.",
        ),
        SeccionLegal(
            "8. Cómo ejercer tus derechos",
            "Para acceder, corregir, eliminar o llevarte tus datos, o para retirar tu consentimiento, escríbenos por " +
                "WhatsApp al +56 9 2366 5073 o acércate a la sede. Responderemos dentro de los plazos legales.",
        ),
        SeccionLegal(
            "Aviso de demostración",
            "Esta es una versión académica (DSY1105). Usa solo datos ficticios: no ingreses información de salud real.",
        ),
    )
}
