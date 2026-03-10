package org.rojman.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.rojman.app.data.AppDatabase
import org.rojman.app.data.AppRepository
import org.rojman.app.data.FavoriteEntity
import org.rojman.app.data.model.WpCategory
import org.rojman.app.data.model.WpPost

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository by lazy {
        val dao = AppDatabase.getInstance(application).favoritesDao()
        AppRepository.create(dao)
    }

    private val _posts = MutableStateFlow<List<WpPost>>(emptyList())
    val posts: StateFlow<List<WpPost>> = _posts.asStateFlow()

    private val _factChecks = MutableStateFlow<List<WpPost>>(emptyList())
    val factChecks: StateFlow<List<WpPost>> = _factChecks.asStateFlow()

    private val _categories = MutableStateFlow<List<WpCategory>>(emptyList())
    val categories: StateFlow<List<WpCategory>> = _categories.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val favorites: StateFlow<List<FavoriteEntity>> = _favorites.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeFavorites()
        refreshAll()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collect { items ->
                _favorites.value = items
            }
        }
    }

    fun refreshAll() {
        loadCategories()
        loadPosts(_selectedCategoryId.value)
        loadFactChecks()
    }

    fun setCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        loadPosts(categoryId)
    }

    fun loadPosts(categoryId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _posts.value = repository.getPosts(
                    page = 1,
                    perPage = 20,
                    categoryId = categoryId
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "خطا در دریافت خبرها"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFactChecks() {
        viewModelScope.launch {
            try {
                _factChecks.value = repository.getFactChecks()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "خطا در دریافت فکت‌چک"
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repository.getCategories()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "خطا در دریافت دسته‌بندی‌ها"
            }
        }
    }

    fun toggleFavorite(post: WpPost) {
        viewModelScope.launch {
            repository.toggleFavorite(post)
        }
    }

    fun isFavorite(postId: Int): Boolean {
        return _favorites.value.any { it.id == postId }
    }

    fun clearError() {
        _errorMessage.update { null }
    }
}
