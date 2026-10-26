package cl.sanaesencia.app

import android.content.Context
import androidx.room.Room
import cl.sanaesencia.app.data.local.AppDatabase
import cl.sanaesencia.app.data.remote.RetrofitClient
import cl.sanaesencia.app.data.repository.AuthRepository
import cl.sanaesencia.app.data.repository.CartRepository
import cl.sanaesencia.app.data.repository.CatalogRepository
import cl.sanaesencia.app.data.repository.CitaRepository
import cl.sanaesencia.app.data.repository.PedidoRepository
import cl.sanaesencia.app.data.session.SessionManager
import cl.sanaesencia.app.domain.usecase.AgendarCitaUseCase
import cl.sanaesencia.app.domain.usecase.DetectarDesercionUseCase
import cl.sanaesencia.app.domain.usecase.VerificarStockBajoUseCase
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.CartViewModel
import cl.sanaesencia.app.presentation.CatalogViewModel
import cl.sanaesencia.app.presentation.CitasViewModel
import cl.sanaesencia.app.presentation.LoginViewModel
import cl.sanaesencia.app.presentation.PerfilViewModel
import cl.sanaesencia.app.services.Notificaciones
import cl.sanaesencia.app.services.RecordatoriosWorker
import cl.sanaesencia.app.ui.admin.AdminViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    init {
        RetrofitClient.initialize(applicationContext)
        // RF-13: canales de notificación y revisión periódica de recordatorios.
        Notificaciones.crearCanales(applicationContext)
        runCatching { RecordatoriosWorker.programar(applicationContext) }
    }

    val sessionManager = SessionManager(applicationContext)

    val db: AppDatabase = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java,
        "sanaesencia.db"
    )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()

    val api = RetrofitClient.api

    val auth = AuthRepository(api, sessionManager)
    val catalog = CatalogRepository(api, db)
    val citas = CitaRepository(api, db, sessionManager)
    val cart = CartRepository(db.carrito())
    val pedidos = PedidoRepository(api)

    val agendarCita = AgendarCitaUseCase(citas)
    val detectarDesercion = DetectarDesercionUseCase()
    val verificarStockBajo = VerificarStockBajoUseCase()

    // El catálogo (profesionales y productos) es público: se comparte entre sesiones.
    val catalogViewModel = CatalogViewModel(catalog)

    // Estos ViewModels guardan datos de la persona que inició sesión.
    // Se vuelven a crear al cerrar sesión para que la siguiente cuenta no vea datos de la anterior.
    var loginViewModel = LoginViewModel(auth); private set
    var cartViewModel = CartViewModel(cart, pedidos); private set
    var citasViewModel = CitasViewModel(citas, agendarCita); private set
    var agendaViewModel = AgendaViewModel(citas); private set
    var perfilViewModel = PerfilViewModel(auth); private set
    var adminViewModel = AdminViewModel(api, verificarStockBajo, catalog); private set

    private fun recrearViewModelsDeSesion() {
        loginViewModel = LoginViewModel(auth)
        cartViewModel = CartViewModel(cart, pedidos)
        citasViewModel = CitasViewModel(citas, agendarCita)
        agendaViewModel = AgendaViewModel(citas)
        perfilViewModel = PerfilViewModel(auth)
        adminViewModel = AdminViewModel(api, verificarStockBajo, catalog)
    }

    /**
     * Cierra la sesión por completo: borra el token, el carrito y las citas guardadas
     * en el teléfono, y deja los ViewModels limpios para la próxima cuenta.
     */
    suspend fun cerrarSesion() {
        auth.logout()
        withContext(Dispatchers.IO) {
            runCatching { db.carrito().clear() }
            runCatching { db.citas().clear() }
        }
        recrearViewModelsDeSesion()
    }
}
