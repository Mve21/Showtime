package rs.edu.raf.rma.movies.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.core.format1d
import rs.edu.raf.rma.movies.domain.Genre
import rs.edu.raf.rma.movies.domain.Movie

private val SORT_OPTIONS = listOf(
    "imdbRating" to "Ocena",
    "year" to "Godina",
    "title" to "Naziv",
)

@Composable
fun MoviesListScreen(
    onMovieClick: (String) -> Unit,
    viewModel: MoviesListViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is MoviesListContract.SideEffect.NavigateToDetail -> onMovieClick(sideEffect.imdbId)
            }
        }
    }

    MoviesListContent(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesListContent(
    state: MoviesListContract.UiState,
    eventPublisher: (MoviesListContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Showtime") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.hasActiveFilters) Badge()
                        }
                    ) {
                        IconButton(onClick = { eventPublisher(MoviesListContract.UiEvent.ToggleFilterSheet) }) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter",
                                tint = if (state.hasActiveFilters)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { eventPublisher(MoviesListContract.UiEvent.SearchQueryChanged(it)) },
                placeholder = { Text("Pretraži filmove...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = {
                            eventPublisher(MoviesListContract.UiEvent.SearchQueryChanged(""))
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Obriši pretragu")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.movies.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.error != null && state.movies.isEmpty() -> {
                        Text(
                            text = "Greška: ${state.error}",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    state.movies.isEmpty() -> {
                        Text(
                            text = "Nema filmova za zadati filter",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(items = state.movies, key = { it.imdbId }) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = {
                                        eventPublisher(MoviesListContract.UiEvent.MovieClicked(movie.imdbId))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isFilterSheetVisible) {
            FilterBottomSheet(
                state = state,
                eventPublisher = eventPublisher,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    state: MoviesListContract.UiState,
    eventPublisher: (MoviesListContract.UiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pendingGenreId by remember(state.selectedGenreId) { mutableStateOf(state.selectedGenreId) }
    var pendingMinYear by remember(state.minYear) { mutableStateOf(state.minYear?.toString() ?: "") }
    var pendingMaxYear by remember(state.maxYear) { mutableStateOf(state.maxYear?.toString() ?: "") }
    var pendingMinRating by remember(state.minRating) { mutableFloatStateOf(state.minRating ?: 0f) }
    var pendingSortBy by remember(state.sortBy) { mutableStateOf(state.sortBy) }
    var pendingSortOrder by remember(state.sortOrder) { mutableStateOf(state.sortOrder ?: "desc") }

    ModalBottomSheet(
        onDismissRequest = { eventPublisher(MoviesListContract.UiEvent.ToggleFilterSheet) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Filter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            if (state.genres.isNotEmpty()) {
                Text("Žanr", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = pendingGenreId == null,
                        onClick = { pendingGenreId = null },
                        label = { Text("Svi") },
                    )
                    state.genres.forEach { genre ->
                        FilterChip(
                            selected = pendingGenreId == genre.id,
                            onClick = {
                                pendingGenreId = if (pendingGenreId == genre.id) null else genre.id
                            },
                            label = { Text(genre.name) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = pendingMinYear,
                    onValueChange = { pendingMinYear = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Min godina") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = pendingMaxYear,
                    onValueChange = { pendingMaxYear = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Max godina") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Column {
                val ratingLabel = if (pendingMinRating > 0f)
                    "Min ocena: ${pendingMinRating.format1d()}"
                else
                    "Min ocena: bez ograničenja"
                Text(ratingLabel, style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = pendingMinRating,
                    onValueChange = { pendingMinRating = it },
                    valueRange = 0f..9f,
                    steps = 17,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sortiraj po", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = pendingSortBy == null,
                        onClick = { pendingSortBy = null },
                        label = { Text("Podrazumevano") },
                    )
                    SORT_OPTIONS.forEach { (value, label) ->
                        FilterChip(
                            selected = pendingSortBy == value,
                            onClick = { pendingSortBy = if (pendingSortBy == value) null else value },
                            label = { Text(label) },
                        )
                    }
                }
            }

            if (pendingSortBy != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Redosled", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = pendingSortOrder == "desc",
                            onClick = { pendingSortOrder = "desc" },
                            label = { Text("Opadajuće") },
                        )
                        FilterChip(
                            selected = pendingSortOrder == "asc",
                            onClick = { pendingSortOrder = "asc" },
                            label = { Text("Rastuće") },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { eventPublisher(MoviesListContract.UiEvent.FilterCleared) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Poništi filter")
                }
                Button(
                    onClick = {
                        eventPublisher(
                            MoviesListContract.UiEvent.FilterApplied(
                                genreId = pendingGenreId,
                                minYear = pendingMinYear.toIntOrNull(),
                                maxYear = pendingMaxYear.toIntOrNull(),
                                minRating = if (pendingMinRating > 0f) pendingMinRating else null,
                                sortBy = pendingSortBy,
                                sortOrder = if (pendingSortBy != null) pendingSortOrder else null,
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Primeni")
                }
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clickable { onClick() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        ),
                    )
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            ) {
                Column {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        movie.year?.let {
                            Text(
                                text = it.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }
                        movie.imdbRating?.let {
                            Text(
                                text = it.format1d(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
