package eu.kanade.tachiyomi.extension.fr.mangamoins

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale

class MangaMoins : HttpSource() {

    override val name = "Manga Moins"
    override val baseUrl = "https://mangamoins.com"
    override val lang = "fr"
    override val supportsLatest = true

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    override fun popularMangaRequest(page: Int): Request = GET(baseUrl, headers)

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val mangas = document.select("#mangaCarousel .manga-card").mapNotNull { element ->
            val url = element.attr("href").trim()
            val title = element.selectFirst(".manga-info h3")?.text()?.trim().orEmpty()
            val thumbnail = element.selectFirst(".manga-cover img")?.absUrl("src")
                ?.ifBlank { null }

            if (url.isBlank() || title.isBlank()) return@mapNotNull null

            SManga.create().apply {
                this.url = url
                this.title = title
                this.thumbnail_url = thumbnail
            }
        }

        return MangasPage(mangas, false)
    }

    override fun latestUpdatesRequest(page: Int): Request = popularMangaRequest(page)

    override fun latestUpdatesParse(response: Response): MangasPage =
        popularMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val q = query.trim()
        if (q.isBlank()) return GET(baseUrl, headers)
        val slug = q.replace(" ", "+")
        return GET("$baseUrl/manga/$slug", headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val requestPath = response.request.url.encodedPath
        if (requestPath == "/" || requestPath.isBlank()) return popularMangaParse(response)

        val document = response.asJsoup()
        val title = document.selectFirst("#manga-title, .title-display")?.text()?.trim().orEmpty()
        if (title.isBlank()) return MangasPage(emptyList(), false)

        val manga = SManga.create().apply {
            this.title = title
            this.url = response.request.url.encodedPath
            this.thumbnail_url = document.selectFirst("#manga-cover")?.absUrl("src")
                ?.ifBlank { null }
        }

        return MangasPage(listOf(manga), false)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()

        return SManga.create().apply {
            title = document.selectFirst("#manga-title, .title-display")?.text()?.trim().orEmpty()
            author = document.selectFirst("#manga-author")?.text()?.trim()
            description = document.selectFirst("#manga-desc")?.text()?.trim()
            status = parseStatus(document.selectFirst("#manga-status")?.text().orEmpty())
            thumbnail_url = document.selectFirst("#manga-cover")?.absUrl("src")
                ?.ifBlank { null }
        }
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()
        return document.select("#chapters-list .chapter-item").map { element ->
            val numberText = element.selectFirst(".ch-num")?.text()?.trim()
                ?.removePrefix("#")
                .orEmpty()
            val titleText = element.selectFirst(".ch-name")?.text()?.trim().orEmpty()
            val dateText = element.selectFirst(".ch-date")?.text()?.trim().orEmpty()

            SChapter.create().apply {
                url = element.attr("href").trim()
                name = buildString {
                    if (numberText.isNotBlank()) append("Chapitre ").append(numberText)
                    if (titleText.isNotBlank()) {
                        if (isNotEmpty()) append(" - ")
                        append(titleText)
                    }
                }.ifBlank { numberText.ifBlank { titleText } }
                chapter_number = numberText.toFloatOrNull() ?: -1f
                date_upload = parseDate(dateText)
            }
        }
    }

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()
        val urls = document.select("#vertical img").mapNotNull { element ->
            when {
                element.hasAttr("src") && element.attr("src").isNotBlank() ->
                    element.absUrl("src")
                element.hasAttr("data-src") && element.attr("data-src").isNotBlank() ->
                    element.absUrl("data-src")
                else -> null
            }?.ifBlank { null }
        }

        return urls.mapIndexed { index, imageUrl ->
            Page(index, imageUrl = imageUrl)
        }
    }

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException("Not used")

    override fun fetchImageUrl(page: Page): Observable<String> =
        Observable.just(page.imageUrl!!)

    private fun parseStatus(status: String): Int {
        val s = status.trim().lowercase(Locale.FRANCE)
        return when {
            s.contains("en cours") -> SManga.ONGOING
            s.contains("pause") || s.contains("hiatus") -> SManga.HIATUS
            s.contains("termin") || s.contains("fin") -> SManga.COMPLETED
            s.contains("annul") -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
    }

    private fun parseDate(date: String): Long {
        return try {
            dateFormat.parse(date)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}
