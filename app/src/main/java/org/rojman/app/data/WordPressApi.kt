package org.rojman.app.data

import org.rojman.app.data.model.WpCategory
import org.rojman.app.data.model.WpPost
import retrofit2.http.GET
import retrofit2.http.Query

interface WordPressApi {

    @GET("wp/v2/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("categories") categoryId: Int? = null
    ): List<WpPost>

    @GET("wp/v2/categories")
    suspend fun getCategories(
        @Query("per_page") perPage: Int = 100
    ): List<WpCategory>
}
