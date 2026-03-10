package org.rojman.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenderedText(
    @SerialName("rendered")
    val rendered: String = ""
)

@Serializable
data class WpPost(
    val id: Int = 0,
    val date: String = "",
    val link: String = "",
    val title: RenderedText = RenderedText(),
    val excerpt: RenderedText = RenderedText(),
    val content: RenderedText = RenderedText(),
    val categories: List<Int> = emptyList(),
    @SerialName("featured_media")
    val featuredMedia: Int? = null
)

@Serializable
data class WpCategory(
    val id: Int = 0,
    val name: String = "",
    val slug: String = "",
    val count: Int = 0
)
