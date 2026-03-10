package org.rojman.app.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.jsoup.Jsoup
import org.rojman.app.data.model.RenderedText
import org.rojman.app.data.model.WpCategory
import org.rojman.app.data.model.WpPost
import retrofit2.Retrofit

class AppRepository(
    private val api: WordPressApi,
    private val favoritesDao: FavoritesDao
) {

    companion object {
        fun create(favoritesDao: FavoritesDao): AppRepository {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

            val retrofit = Retrofit.Builder()
                .baseUrl("https://rojman.org/wp-json/")
                .addConverterFactory(
                    json.asConverterFactory("application/json".toMediaType())
                )
                .build()

            val api = retrofit.create(WordPressApi::class.java)
            return AppRepository(api, favoritesDao)
        }
    }

    suspend fun getPosts(
        page: Int = 1,
        perPage: Int = 10,
        categoryId: Int? = null
    ): List<WpPost> {
        return api.getPosts(page = page, perPage = perPage, categoryId = categoryId)
    }

    suspend fun getCategories(): List<WpCategory> {
        return api.getCategories()
    }

    suspend fun getFactChecks(): List<WpPost> {
        return try {
            val doc = Jsoup.connect("https://rojman.org/factcheck").get()

            doc.select("article").take(10).mapIndexed { index, element ->
                val titleEl = element.selectFirst("h2 a, h3 a, .entry-title a, .post-title a, a")
                val excerptEl = element.selectFirst("p, .entry-summary, .post-excerpt")

                WpPost(
                    id = titleEl?.attr("href")?.hashCode() ?: index,
                    link = titleEl?.attr("href") ?: "https://rojman.org/factcheck",
                    title = RenderedText(titleEl?.text().orEmpty()),
                    excerpt = RenderedText(excerptEl?.text().orEmpty())
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun observeFavorites(): Flow<List<FavoriteEntity>> {
        return favoritesDao.observeAll()
    }

    suspend fun getFavorites(): List<FavoriteEntity> {
        return favoritesDao.getAll()
    }

    suspend fun toggleFavorite(post: WpPost) {
        val existing = favoritesDao.getById(post.id)
        if (existing == null) {
            favoritesDao.insert(
                FavoriteEntity(
                    id = post.id,
                    title = post.title.rendered,
                    link = post.link,
                    excerpt = post.excerpt.rendered
                )
            )
        } else {
            favoritesDao.deleteById(post.id)
        }
    }
}
