package cl.sanaesencia.app.ui.profesionales

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
    val data by vm.profesionales.collectAsState(initial = emptyList())
    val state by vm.state.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("Todas") }

    val specialtiesList = listOf("Todas", "Psicología", "Fonoaudiología", "Nutrición", "Terapias")

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text(
                    "Profesionales",
                    fontWeight = FontWeight.Bold,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // SEARCH FIELD
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o especialidad...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // CATEGORY CHIPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                specialtiesList.forEach { value ->
                    FilterChip(
                        selected = specialty == value,
                        onClick = { specialty = value },
                        label = { Text(value) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
            }
        }

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
                val profesionalesFiltrados = data.filter { prof ->
                    val matchesSpecialty = specialty == "Todas" ||
                            prof.categoriaEspecialidad.contains(specialty, ignoreCase = true)
                    val matchesSearch = searchQuery.isBlank() ||
                            prof.titulo.contains(searchQuery, ignoreCase = true) ||
                            prof.categoriaEspecialidad.contains(searchQuery, ignoreCase = true) ||
                            prof.descripcionEnfoque.contains(searchQuery, ignoreCase = true)

                    matchesSpecialty && matchesSearch
                }

                if (profesionalesFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No se encontraron profesionales para esta búsqueda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 20.dp,
                        ),
                    ) {
                        items(
                            items = profesionalesFiltrados,
                            key = { "profesional_${it.id}" },
                        ) { profesional ->
                            ProfesionalCard(
                                profesional = profesional,
                                onClick = { onAgendar(profesional.id) },
                            )
                        }
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profesional.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verificado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Text(
                    text = profesional.categoriaEspecialidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Text(
            text = profesional.descripcionEnfoque,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Atención Integrativa") },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )

            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(profesional.estado.name) },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        BotonPrimario(
            texto = "Ver disponibilidad / Agendar",
            onClick = onClick,
        )
    }
}
