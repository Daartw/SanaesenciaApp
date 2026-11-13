package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.dobles.CatalogRepositorioFalso
import cl.sanaesencia.app.dobles.MainDispatcherRule
import cl.sanaesencia.app.domain.model.Producto
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** IE 3.2.1 — Pruebas del catálogo (RF-03), que se lee de Room y se refresca del backend. */
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun al_crearse_sincroniza_con_el_backend() {
        val repo = CatalogRepositorioFalso()

        val vm = CatalogViewModel(repo)

        assertEquals(1, repo.refrescos)
        assertTrue(vm.state.value is UiState.Exito)
    }

    @Test
    fun sin_conexion_el_estado_queda_en_error() {
        val repo = CatalogRepositorioFalso(error = IOException())

        val vm = CatalogViewModel(repo)

        val estado = vm.state.value
        assertTrue(estado is UiState.Error)
        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            (estado as UiState.Error).mensaje,
        )
    }

    @Test
    fun refrescar_a_mano_vuelve_a_consultar() {
        val repo = CatalogRepositorioFalso()
        val vm = CatalogViewModel(repo)

        vm.refresh()

        assertEquals(2, repo.refrescos)
    }

    @Test
    fun los_productos_se_exponen_desde_la_base_local() = runBlocking {
        val repo = CatalogRepositorioFalso()
        repo.productosState.value = listOf(
            Producto(1, "Bruma de lavanda", "Relajante", 8990, 10, listOf("relajación")),
        )

        val vm = CatalogViewModel(repo)

        assertEquals("Bruma de lavanda", vm.productos.first().first().nombre)
    }
}
