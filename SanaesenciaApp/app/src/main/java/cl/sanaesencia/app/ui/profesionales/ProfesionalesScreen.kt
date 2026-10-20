package cl.sanaesencia.app.ui.profesionales

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.domain.model.Profesional
import cl.sanaesencia.app.presentation.CatalogViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.BotonPrimario
import cl.sanaesencia.app.ui.common.PantallaEstado
import cl.sanaesencia.app.ui.common.TarjetaBase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalesScreen(
    vm: CatalogViewModel,
    onAgendar: (Long) -> Unit,
) {
    val data by vm.profesionales.collectAsState(
        initial = emptyList(),
    )

    val state by vm.state.collectAsState()

    var specialty by remember {
        mutableStateOf("Todas")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {

        TopAppBar(
            title = {
                Text("Profesionales")
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState(),
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Todas",
                    "Psicología",
                    "Fonoaudiología",
                ).forEach { value ->

                    FilterChip(
                        selected = specialty == value,
                        onClick = {
                            specialty = value
                        },
                        label = {
                            Text(value)
                        },
                        modifier = Modifier.heightIn(
                            min = 48.dp,
                        ),
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp),
            )
        }

        /*
         * Box ocupa todo el espacio restante.
         *
         * No usamos modifier en PantallaEstado porque
         * PantallaEstado no recibe ese parámetro.
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {

            PantallaEstado(
                loading = state is UiState.Cargando,
                error = (state as? UiState.Error)?.mensaje,
                empty = data.isEmpty(),
                onRetry = vm::refresh,
            ) {

                val profesionalesFiltrados = data.filter {
                    specialty == "Todas" ||
                            it.categoriaEspecialidad.equals(
                                specialty,
                                ignoreCase = true,
                            )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(
                        10.dp,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp,
                    ),
                ) {

                    items(
                        items = profesionalesFiltrados,
                        key = { profesional ->
                            "profesional_${profesional.id}"
                        },
                    ) { profesional ->

                        ProfesionalCard(
                            profesional = profesional,
                            onClick = {
                                onAgendar(profesional.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfesionalCard(
    profesional: Profesional,
    onClick: () -> Unit,
) {
    TarjetaBase(
        modifier = Modifier.animateContentSize(),
    ) {

        Text(
            text = profesional.titulo,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = profesional.categoriaEspecialidad,
        )

        Text(
            text = profesional.descripcionEnfoque,
        )

        AssistChip(
            onClick = {},
            enabled = false,
            label = {
                Text(
                    profesional.estado.name,
                )
            },
        )

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        BotonPrimario(
            texto = "Ver disponibilidad / Agendar",
            onClick = onClick,
        )
    }
}