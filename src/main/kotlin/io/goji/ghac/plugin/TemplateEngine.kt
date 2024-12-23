package io.goji.ghac.plugin

import com.github.mustachejava.DefaultMustacheFactory
import java.io.StringReader
import java.io.StringWriter

class TemplateEngine {
    private val mustacheFactory = DefaultMustacheFactory()

    fun render(template: String, context: Map<String, Any>): String {
        val mustache = mustacheFactory.compile(StringReader(template), "template")

        return StringWriter().use { writer ->
            mustache.execute(writer, context)
            writer.toString()
        }
    }
}
