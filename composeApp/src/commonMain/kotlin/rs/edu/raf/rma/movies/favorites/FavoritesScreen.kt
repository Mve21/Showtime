package rs.edu.raf.rma.movies.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.domain.Movie

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesContract.UiState,
    onEvent: (FavoritesContract.UiEvent) -> Unit,
    sideEffect: Flow<FavoritesContract.SideEffect>,
    onNavigateToDetail: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                is FavoritesContract.SideEffect.NavigateToDetail ->
                    onNavigateToDetail(effect.imdbId)
                is FavoritesContract.SideEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Omiljeni") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading && state.movies.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.movies.isEmpty() -> {
                    Text(
                        text = "Nemaš omiljenih filmova",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items = state.movies, key = { it.imdbId }) { movie ->
                            FavoriteMovieCard(
                                movie = movie,
                                onMovieClick = {
                                    onEvent(FavoritesContract.UiEvent.MovieClicked(movie.imdbId))
                                },
                                onRemoveClick = {
                                    onEvent(FavoritesContract.UiEvent.RemoveFavorite(movie.imdbId))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteMovieCard(
    movie: Movie,
    onMovieClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMovieClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = "$TMDB_IMAGE_BASE${movie.posterPath}",
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(60.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp)),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                movie.year?.toString(),
                movie.imdbRating?.let { "${"%.1f".format(it)} ★" },
            ).joinToString("  ·  ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Ukloni iz omiljenih",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
