package cl.sanaesencia.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas del polimorfismo del catálogo.
 *
 * [Producto] (tienda, RF-03) y [Servicio] (terapias) comparten la interfaz
 * [ItemCatalogo], de modo que la tienda puede listarlos sin preguntar por su tipo.
 */
class CatalogoTest {

    @Test
    fun un_producto_y_un_servicio_son_tratados_como_items_del_catalogo() {
        val items: List<ItemCatalogo> = listOf(producto(), servicio())

        assertEquals(listOf(8990L, 35000L), items.map { it.precioFinal() })
        assertTrue(items.all { it.nombre.isNotBlank() })
    }

    @Test
    fun la_descripcion_corta_de_un_producto_se_recorta_a_ochenta_caracteres() {
        val largo = producto(descripcion = "x".repeat(200))

        assertEquals(80, largo.descripcionCorta().length)
    }

    @Test
    fun una_descripcion_breve_se_mantiene_completa() {
        assertEquals("Relajante", producto().descripcionCorta())
    }

    @Test
    fun la_descripcion_corta_de_un_servicio_antepone_su_duracion() {
        assertEquals("50 min · Primera sesión", servicio().descripcionCorta())
    }

    @Test
    fun el_servicio_recorta_su_descripcion_a_sesenta_caracteres() {
        val largo = servicio(descripcion = "y".repeat(200))

        assertEquals("50 min · ${"y".repeat(60)}", largo.descripcionCorta())
    }

    private fun producto(descripcion: String = "Relajante") = Producto(
        id = 1,
        nombre = "Bruma de lavanda",
        descripcion = descripcion,
        precio = 8990,
        stock = 10,
        etiquetas = listOf("relajación"),
    )

    private fun servicio(descripcion: String = "Primera sesión") = Servicio(
        id = 2,
        nombre = "Evaluación inicial",
        precio = 35000,
        descripcion = descripcion,
        duracionMinutos = 50,
    )
}
