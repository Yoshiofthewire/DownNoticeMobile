package com.downnotice.mobile.data.parser

import com.downnotice.mobile.data.model.FeedEntry
import com.downnotice.mobile.data.model.IncidentStatus
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ParsedFeed(
    val title: String,
    val items: List<FeedEntry>
)

object FeedParser {

    private val rssDateFormats = arrayOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss z",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )

    fun parse(xml: String): ParsedFeed {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                return when (parser.name) {
                    "rss" -> parseRss(parser)
                    "feed" -> parseAtom(parser)
                    else -> {
                        // Skip until we find rss or feed
                        eventType = parser.next()
                        continue
                    }
                }
            }
            eventType = parser.next()
        }
        throw IllegalArgumentException("Unknown feed format")
    }

    private fun parseRss(parser: XmlPullParser): ParsedFeed {
        var feedTitle = "Unknown Feed"
        val items = mutableListOf<FeedEntry>()
        var inChannel = false
        var inItem = false
        var currentTitle = ""
        var currentDescription = ""
        var currentLink = ""
        var currentPubDate = ""

        var eventType = parser.next()
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "channel" -> inChannel = true
                        "item" -> {
                            inItem = true
                            currentTitle = ""
                            currentDescription = ""
                            currentLink = ""
                            currentPubDate = ""
                        }
                        "title" -> {
                            val text = parser.nextText() ?: ""
                            if (inItem) currentTitle = text
                            else if (inChannel && feedTitle == "Unknown Feed") feedTitle = text
                        }
                        "description" -> {
                            if (inItem) currentDescription = parser.nextText() ?: ""
                        }
                        "link" -> {
                            if (inItem) currentLink = parser.nextText() ?: ""
                        }
                        "pubDate" -> {
                            if (inItem) currentPubDate = parser.nextText() ?: ""
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "item" -> {
                            val isoDate = parseDate(currentPubDate)
                            items.add(
                                FeedEntry(
                                    title = currentTitle,
                                    description = stripHtml(currentDescription),
                                    link = currentLink,
                                    pubDate = isoDate,
                                    status = detectStatus(currentTitle, currentDescription, isoDate)
                                )
                            )
                            inItem = false
                        }
                        "channel" -> inChannel = false
                    }
                }
            }
            eventType = parser.next()
        }
        return ParsedFeed(feedTitle, items)
    }

    private fun parseAtom(parser: XmlPullParser): ParsedFeed {
        var feedTitle = "Unknown Feed"
        val items = mutableListOf<FeedEntry>()
        var inEntry = false
        var currentTitle = ""
        var currentContent = ""
        var currentLink = ""
        var currentUpdated = ""
        var inFeedTitle = false

        var eventType = parser.next()
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "entry" -> {
                            inEntry = true
                            currentTitle = ""
                            currentContent = ""
                            currentLink = ""
                            currentUpdated = ""
                        }
                        "title" -> {
                            val text = parser.nextText() ?: ""
                            if (inEntry) currentTitle = text
                            else if (feedTitle == "Unknown Feed") feedTitle = text
                        }
                        "content", "summary" -> {
                            if (inEntry && currentContent.isEmpty()) {
                                currentContent = parser.nextText() ?: ""
                            }
                        }
                        "link" -> {
                            if (inEntry && currentLink.isEmpty()) {
                                val href = parser.getAttributeValue(null, "href") ?: ""
                                if (href.isNotEmpty()) {
                                    currentLink = href
                                } else {
                                    val text = parser.nextText() ?: ""
                                    if (text.isNotEmpty()) currentLink = text
                                }
                            }
                        }
                        "updated", "published" -> {
                            if (inEntry && currentUpdated.isEmpty()) {
                                currentUpdated = parser.nextText() ?: ""
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "entry") {
                        val isoDate = parseDate(currentUpdated)
                        items.add(
                            FeedEntry(
                                title = currentTitle,
                                description = stripHtml(currentContent),
                                link = currentLink,
                                pubDate = isoDate,
                                status = detectStatus(currentTitle, currentContent, isoDate)
                            )
                        )
                        inEntry = false
                    }
                }
            }
            eventType = parser.next()
        }
        return ParsedFeed(feedTitle, items)
    }

    fun detectStatus(title: String, description: String, isoDate: String): IncidentStatus {
        // Items with a future date should be Green
        try {
            val date = Instant.parse(isoDate)
            if (date.isAfter(Instant.now())) return IncidentStatus.SCHEDULED
        } catch (_: Exception) {}

        val titleLower = title.lowercase()
        val descLower = description.lowercase()

        // Check for strong/bold markers in description (status page convention)
        val strongPattern = Regex("<strong>\\s*([^<]+)\\s*</strong>", RegexOption.IGNORE_CASE)
        val entityPattern = Regex("&lt;strong&gt;\\s*([^&]+)\\s*&lt;/strong&gt;", RegexOption.IGNORE_CASE)
        val marker = (strongPattern.find(descLower)?.groupValues?.get(1)
            ?: entityPattern.find(descLower)?.groupValues?.get(1) ?: "").trim()

        // If latest update marker indicates resolved/completed/scheduled, trust it
        val resolvedMarkers = listOf("resolved", "completed", "fixed", "recovered", "restored", "postmortem")
        val scheduledMarkers = listOf("scheduled", "this is a scheduled event")

        for (m in scheduledMarkers) {
            if (marker.contains(m)) return IncidentStatus.SCHEDULED
        }
        for (m in resolvedMarkers) {
            if (marker.contains(m)) return IncidentStatus.RESOLVED
        }

        val fullText = "$titleLower $descLower"

        // Check green/resolved first
        val greenKeywords = listOf("resolved", "completed", "scheduled", "fixed", "recovered", "restored")
        // Only match if title explicitly says resolved
        for (kw in greenKeywords) {
            if (titleLower.contains(kw)) return IncidentStatus.RESOLVED
        }

        // Down keywords (from prompt)
        val downKeywords = listOf("disruption", "unavailable", "unavalible", "down", "interrupted", "interupted", "outage", "major", "failure", "emergency", "critical")
        for (kw in downKeywords) {
            if (fullText.contains(kw)) return IncidentStatus.DOWN
        }

        // Degraded keywords (from prompt)
        val degradedKeywords = listOf("degraded", "intermittent", "issues", "partial", "elevated", "delays", "slow", "investigating")
        for (kw in degradedKeywords) {
            if (fullText.contains(kw)) return IncidentStatus.DEGRADED
        }

        // Maintenance
        val maintenanceKeywords = listOf("maintenance", "scheduled")
        for (kw in maintenanceKeywords) {
            if (fullText.contains(kw)) return IncidentStatus.SCHEDULED
        }

        return IncidentStatus.UNKNOWN
    }

    private fun parseDate(dateStr: String): String {
        if (dateStr.isBlank()) return Instant.now().toString()

        // Try ISO 8601 directly
        try {
            return Instant.parse(dateStr).toString()
        } catch (_: Exception) {}

        // Try common RSS date formats
        for (format in rssDateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(dateStr)
                if (date != null) return date.toInstant().toString()
            } catch (_: Exception) {}
        }

        return Instant.now().toString()
    }

    private fun stripHtml(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }
}
