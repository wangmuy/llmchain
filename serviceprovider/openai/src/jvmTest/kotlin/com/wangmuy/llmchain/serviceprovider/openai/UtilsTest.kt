package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.outputparser.JsonOutputParser
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class UtilsTest {
    @BeforeTest fun prepare() {
        JsonOutputParser.PATTERN_JSON_QUOTED = Regex(JsonOutputParser.PTNSTR_JSON_QUOTED, RegexOption.DOT_MATCHES_ALL)
    }

    @Test fun jsonQuotedParseTest() {
        val jsonQuoted = """  ```json
{
"k1": "v1",
"k2": "v2"
}
```""".trimMargin()
        var result: MatchResult? = null
        result = JsonOutputParser.PATTERN_JSON_QUOTED.find(jsonQuoted)
        assertNotNull(result)
        println("result1=${result?.groupValues?.get(2)}")
        val quoted2 = "\n$jsonQuoted\n"
        result = JsonOutputParser.PATTERN_JSON_QUOTED.find(quoted2)
        assertNotNull(result)
        println("result2=${result?.groupValues?.get(2)}")
    }
}