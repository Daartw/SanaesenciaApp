package cl.sanaesencia.app.dobles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Reemplaza `Dispatchers.Main` por un dispatcher de prueba.
 *
 * Los ViewModels usan `viewModelScope`, que despacha en el hilo principal de Android;
 * ese hilo no existe en una prueba unitaria de la JVM. Con esta regla las corrutinas
 * se ejecutan de inmediato y en el mismo hilo, así que cada prueba puede comprobar el
 * estado justo después de llamar al ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
