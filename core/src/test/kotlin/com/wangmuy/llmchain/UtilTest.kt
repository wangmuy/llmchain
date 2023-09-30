package com.wangmuy.llmchain

import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.prompt.fStringFormat
import com.wangmuy.llmchain.schema.BaseMessage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilTest {
    @Test fun fstringTest() {
        assertEquals("hello world", "{abc} world".fStringFormat(mapOf("abc" to "hello")))
        assertEquals("hello world", "h{abc} world".fStringFormat(mapOf("abc" to "ello")))
        assertEquals("hello world", "hello {abc}".fStringFormat(mapOf("abc" to "world")))
        assertEquals("hello world", "hello {abc}d".fStringFormat(mapOf("abc" to "worl")))
        assertEquals("hello world", "hello world".fStringFormat(mapOf("abc" to "aaa")))
        assertEquals("hello world", "hello {a_bc}".fStringFormat(mapOf("a_bc" to "world")))
        assertEquals("hello null", "hello {abc}".fStringFormat(mapOf("def" to "ggg")))
        assertEquals("hello {abc}", "hello {abc}".fStringFormat(mapOf("def" to "ggg"), replaceNotExist = false))
    }

    @Test fun stringTest() {
        assertEquals("_Sea_rch", "{_Sea_rch}".trim {c -> !c.isLetterOrDigit() && c != '_'})
    }

    @Test fun testPromptTemplate() {
        val args = mapOf(
            "input" to "hello",
            "history" to listOf(BaseMessage("yoyo")))
        val promptTemplate = PromptTemplate(listOf("input", "history"),
            "history={history}\ninput={input}")
        val prompt = promptTemplate.format(args)
        assertEquals("""history=yoyo
input=hello""".trimMargin(), prompt)
    }

    @Test fun jsonTest() {
        val json = buildJsonObject {
            put("name", "get_weather_info")
            put("arguments", buildJsonObject {
                put("location", "Boston, CA")
            })
        }
        println("json=$json")
        val name = json["name"]?.jsonPrimitive?.content
        println("name=${name}")
    }
}