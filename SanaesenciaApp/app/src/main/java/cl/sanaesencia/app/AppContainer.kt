
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
import cl.sanaesencia.app.presentation.LoginViewModel
import cl.sanaesencia.app.presentation.CatalogViewModel
import cl.sanaesencia.app.presentation.CartViewModel
import cl.sanaesencia.app.presentation.CitasViewModel
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.PerfilViewModel
import cl.sanaesencia.app.ui.admin.AdminViewModel

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    init {
        RetrofitClient.initialize(applicationContext)
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

    val auth = AuthRepository(
        api,
        sessionManager
    )

    val loginViewModel = LoginViewModel(auth)

    val catalog = CatalogRepository(
        api,
        db
    )

    val citas = CitaRepository(
        api,
        db
    )

    val cart = CartRepository(
        db.carrito()
    )

    val pedidos = PedidoRepository(
        api
    )

    val agendarCita = AgendarCitaUseCase(
        citas
    )

    val detectarDesercion = DetectarDesercionUseCase()

    val verificarStockBajo = VerificarStockBajoUseCase()

    val catalogViewModel = CatalogViewModel(catalog)
    val cartViewModel = CartViewModel(cart, pedidos)
    val citasViewModel = CitasViewModel(citas, agendarCita)
    val agendaViewModel = AgendaViewModel(citas)
    val perfilViewModel = PerfilViewModel(auth)
    val adminViewModel = AdminViewModel(api, verificarStockBajo, catalog)
}
