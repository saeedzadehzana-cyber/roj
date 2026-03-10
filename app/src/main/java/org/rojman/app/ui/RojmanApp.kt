package org.rojman.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jsoup.Jsoup
import org.rojman.app.data.FavoriteEntity
import org.rojman.app.data.model.WpCategory
import org.rojman.app.data.model.WpPost
import androidx.compose.runtime.collectAsState

private enum class Screen(val label: String) {
    HOME("خبرها"),
    FACT_CHECK("فکت‌چک"),
    FAVORITES("دلخواه"),
    SUBMIT("ارسال")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RojmanApp(viewModel: MainViewModel) {
    val context = LocalContext.current

    val posts by viewModel.posts.collectAsState()
    val factChecks by viewModel.factChecks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()

    var currentScreen by remember { mutableIntStateOf(0) }
    val screens = Screen.entries

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            when (screens[currentScreen]) {
                                Screen.HOME -> "Rojman"
                                Screen.FACT_CHECK -> "فکت‌چک"
                                Screen.FAVORITES -> "موارد دلخواه"
                                Screen.SUBMIT -> "ارسال مطلب"
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refreshAll() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "بازخوانی")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    screens.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            selected = currentScreen == index,
                            onClick = { currentScreen = index },
                            icon = {
                                when (screen) {
                                    Screen.HOME -> Icon(Icons.Default.Refresh, null)
                                    Screen.FACT_CHECK -> Icon(Icons.Default.Refresh, null)
                                    Screen.FAVORITES -> Icon(Icons.Default.Favorite, null)
                                    Screen.SUBMIT -> Icon(Icons.Default.Email, null)
                                }
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        ) { padding ->
            when (screens[currentScreen]) {
                Screen.HOME -> HomeScreen(
                    padding = padding,
                    posts = posts,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    isLoading = isLoading,
                    isFavorite = { id -> favorites.any { it.id == id } },
                    onCategorySelected = { viewModel.setCategory(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenPost = { url ->
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        )
                    }
                )

                Screen.FACT_CHECK -> FactCheckScreen(
                    padding = padding,
                    posts = factChecks,
                    isFavorite = { id -> favorites.any { it.id == id } },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenPost = { url ->
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        )
                    }
                )

                Screen.FAVORITES -> FavoritesScreen(
                    padding = padding,
                    favorites = favorites,
                    onOpenPost = { url ->
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        )
                    }
                )

                Screen.SUBMIT -> SubmitScreen(
                    padding = padding,
                    onSendMail = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:info@rojman.org")
                            putExtra(Intent.EXTRA_SUBJECT, "ارسال مطلب / خبر برای روژمان")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "سلام،\n\nمتن خبر/مطلب:\n\n"
                            )
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    posts: List<WpPost>,
    categories: List<WpCategory>,
    selectedCategoryId: Int?,
    isLoading: Boolean,
    isFavorite: (Int) -> Boolean,
    onCategorySelected: (Int?) -> Unit,
    onToggleFavorite: (WpPost) -> Unit,
    onOpenPost: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        CategoryRow(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            PostList(
                posts = posts,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onOpenPost = onOpenPost
            )
        }
    }
}

@Composable
private fun FactCheckScreen(
    padding: PaddingValues,
    posts: List<WpPost>,
    isFavorite: (Int) -> Boolean,
    onToggleFavorite: (WpPost) -> Unit,
    onOpenPost: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Text(
            text = "۱۰ مطلب آخر فکت‌چک",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        PostList(
            posts = posts,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onOpenPost = onOpenPost
        )
    }
}

@Composable
private fun FavoritesScreen(
    padding: PaddingValues,
    favorites: List<FavoriteEntity>,
    onOpenPost: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favorites) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPost(item.link) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.excerpt,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmitScreen(
    padding: PaddingValues,
    onSendMail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "برای ارسال خبر یا مطلب، روی دکمه زیر بزن.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        ElevatedAssistChip(
            onClick = onSendMail,
            label = { Text("ارسال به info@rojman.org") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )
    }
}

@Composable
private fun CategoryRow(
    categories: List<WpCategory>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedAssistChip(
                    onClick = { onCategorySelected(null) },
                    label = { Text("همه") }
                )
                categories.take(12).forEach { category ->
                    ElevatedAssistChip(
                        onClick = { onCategorySelected(category.id) },
                        label = {
                            Text(
                                text = if (selectedCategoryId == category.id) {
                                    "✓ ${category.name}"
                                } else {
                                    category.name
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PostList(
    posts: List<WpPost>,
    isFavorite: (Int) -> Boolean,
    onToggleFavorite: (WpPost) -> Unit,
    onOpenPost: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts) { post ->
            PostCard(
                post = post,
                favorite = isFavorite(post.id),
                onToggleFavorite = { onToggleFavorite(post) },
                onOpenPost = { onOpenPost(post.link) }
            )
        }
    }
}

@Composable
private fun PostCard(
    post: WpPost,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenPost: () -> Unit
) {
    val cleanExcerpt = remember(post.excerpt.rendered) {
        Jsoup.parse(post.excerpt.rendered).text()
    }
    val cleanTitle = remember(post.title.rendered) {
        Jsoup.parse(post.title.rendered).text()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPost() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cleanTitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cleanExcerpt,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
