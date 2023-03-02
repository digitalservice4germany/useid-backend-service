package de.bund.digitalservice.useid.config

import io.micrometer.core.instrument.Tag
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsContributor
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CustomWebClientExchangeTagsProvider : WebMvcTagsContributor {

    override fun getTags(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        handler: Any?,
        exception: Throwable?,
    ): MutableIterable<Tag> {
        val tags = mutableListOf<Tag>()
        request?.let { r ->
            val id: Any? = r.getAttribute("tenantId")
            id?.let {
                val tag = Tag.of("tenant_id", it.toString())
                tags.add(tag)
            }
        }
        return tags
    }

    override fun getLongRequestTags(request: HttpServletRequest?, handler: Any?): MutableIterable<Tag> {
        return mutableListOf()
    }
}