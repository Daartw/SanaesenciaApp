package cl.sanaesencia.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri

object ContactService {
    private const val WHATSAPP_NUMBER = "56923665073"

    fun openWhatsApp(context: Context) {
        val text = Uri.encode("Hola, quiero información sobre Sanaesencia.")
        val uri = Uri.parse("https://wa.me/$WHATSAPP_NUMBER?text=$text")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
