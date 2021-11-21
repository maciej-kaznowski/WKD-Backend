package com.innercirclesoftware.wkd_core.utils

import okhttp3.HttpUrl
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.InputStream
import java.nio.charset.Charset
import org.jsoup.Jsoup as ActualJsoup

object Jsoup {

    fun requireParse(stream: InputStream, charset: Charset, url: HttpUrl): Document {
        return ActualJsoup.parse(stream, charset.name(), url.toString())
    }

}

internal fun Element.requireElementsByClass(className: String): Elements {
    return requireNotNull(getElementsByClass(className)) { "Null elements for className=$className" }
}

internal fun Element.requireSelect(cssQuery: String): Elements {
    return requireNotNull(select(cssQuery)) { "Null elements for cssQuery=$cssQuery" }
}
