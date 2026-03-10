package org.rojman.app.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
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

    suspend fun getLatestPosts(page: Int = 1, perPage: Int = 10) =
        api.getPosts(page = page, perPage = perPage)

    suspend fun getCategories() =
        api.getCategories()

    suspend fun getFactCheckPosts(page: Int = 1, perPage: Int = 10) =
        api.getFactCheckPosts(page = page, perPage = perPage)

    suspend fun getFavorites() =
        favoritesDao.getAll()

    suspend fun addFavorite(item: FavoriteEntity) =
        favoritesDao.insert(item)

    suspend fun removeFavorite(item: FavoriteEntity) =
        favoritesDao.delete(item)
}
