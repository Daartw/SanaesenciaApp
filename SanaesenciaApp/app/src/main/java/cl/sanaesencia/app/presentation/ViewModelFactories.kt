package cl.sanaesencia.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.sanaesencia.app.data.remote.SanaesenciaApi
import cl.sanaesencia.app.data.repository.AuthRepository
import cl.sanaesencia.app.data.repository.CartRepository
import cl.sanaesencia.app.data.repository.CatalogRepository
import cl.sanaesencia.app.data.repository.CitaRepository
import cl.sanaesencia.app.data.repository.PedidoRepository
import cl.sanaesencia.app.domain.usecase.AgendarCitaUseCase
import cl.sanaesencia.app.ui.admin.AdminViewModel

class LoginFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(repo) as T
}

class CatalogFactory(private val repo: CatalogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CatalogViewModel(repo) as T
}

class CartFactory(
    private val repo: CartRepository,
    private val pedidos: PedidoRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CartViewModel(repo, pedidos) as T
}

class CitasFactory(
    private val repo: CitaRepository,
    private val agendar: AgendarCitaUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CitasViewModel(repo, agendar) as T
}

class PerfilFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PerfilViewModel(repo) as T
}

class AgendaFactory(private val repo: CitaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AgendaViewModel(repo) as T
}

class AdminFactory(
    private val api: SanaesenciaApi,
    private val stockBajo: cl.sanaesencia.app.domain.usecase.VerificarStockBajoUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminViewModel(api, stockBajo) as T
}
