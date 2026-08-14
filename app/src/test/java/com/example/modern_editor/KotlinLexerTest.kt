package com.example.modern_editor

import com.example.modern_editor.editor.kotlin.KotlinLexer
import com.example.modern_editor.editor.kotlin.KotlinToken
import com.example.modern_editor.editor.kotlin.TokenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers TC5.1–TC5.5 against the exact sample from docs/components.md, plus the edge
 * cases a hand-written scanner is most likely to get wrong.
 */
class KotlinLexerTest {

    private val sample = """
        class Student {
            private val name: String = "Yasuru"

            // Hello
            fun greet() {
                println("Hello")
            }
        }
    """.trimIndent()

    private fun lex(source: String): List<Pair<TokenType, String>> =
        KotlinLexer.tokenize(source).map { it.type to source.substring(it.start, it.end) }

    private fun textsOf(source: String, type: TokenType): List<String> =
        lex(source).filter { it.first == type }.map { it.second }

    @Test
    fun `TC5_1 keywords are recognised`() {
        val keywords = textsOf(sample, TokenType.KEYWORD)
        assertTrue("class missing", "class" in keywords)
        assertTrue("private missing", "private" in keywords)
        assertTrue("val missing", "val" in keywords)
        assertTrue("fun missing", "fun" in keywords)
    }

    @Test
    fun `TC5_2 strings are recognised`() {
        val strings = textsOf(sample, TokenType.STRING)
        assertTrue("\"Yasuru\" missing", "\"Yasuru\"" in strings)
        assertTrue("\"Hello\" missing", "\"Hello\"" in strings)
    }

    @Test
    fun `TC5_3 line comments are recognised`() {
        assertEquals(listOf("// Hello"), textsOf(sample, TokenType.COMMENT))
    }

    @Test
    fun `TC5_4 annotations are recognised`() {
        assertEquals(listOf("@Override"), textsOf("@Override fun f() {}", TokenType.ANNOTATION))
        // Use-site targets are part of the annotation token.
        assertEquals(listOf("@file:JvmName"), textsOf("@file:JvmName(\"X\")", TokenType.ANNOTATION))
    }

    @Test
    fun `TC5_5 ordinary identifiers are not keywords`() {
        val keywords = textsOf(sample, TokenType.KEYWORD)
        assertFalse("Student was treated as a keyword", "Student" in keywords)
        assertFalse("name was treated as a keyword", "name" in keywords)
        assertFalse("greet was treated as a keyword", "greet" in keywords)
    }

    @Test
    fun `soft keywords that are common variable names stay identifiers`() {
        // `value`, `field`, `get` and `set` are soft keywords, but a lexer cannot tell
        // them apart from ordinary variables without a parser, so they must not be
        // painted as keywords.
        val keywords = textsOf("val value = 1\nval field = 2\nval get = 3", TokenType.KEYWORD)
        assertEquals(listOf("val", "val", "val"), keywords)
    }

    @Test
    fun `function declaration name is separated from the keyword`() {
        assertEquals(listOf("greet"), textsOf("fun greet() {}", TokenType.FUNCTION_NAME))
        // A call is not a declaration.
        assertTrue(textsOf("greet()", TokenType.FUNCTION_NAME).isEmpty())
    }

    @Test
    fun `block comments nest as Kotlin allows`() {
        val source = "/* outer /* inner */ still outer */ val x = 1"
        assertEquals(
            listOf("/* outer /* inner */ still outer */"),
            textsOf(source, TokenType.COMMENT)
        )
        // The code after the comment is still lexed.
        assertEquals(listOf("val"), textsOf(source, TokenType.KEYWORD))
    }

    @Test
    fun `unterminated string stops at end of line`() {
        val source = "val a = \"oops\nval b = 2"
        assertEquals(listOf("\"oops"), textsOf(source, TokenType.STRING))
        // The next line must still be highlighted, not swallowed by the open quote.
        assertEquals(listOf("val", "val"), textsOf(source, TokenType.KEYWORD))
    }

    @Test
    fun `raw strings and escapes are handled`() {
        assertEquals(listOf("\"\"\"a \" b\"\"\""), textsOf("val s = \"\"\"a \" b\"\"\"", TokenType.STRING))
        assertEquals(listOf("\"a \\\" b\""), textsOf("val s = \"a \\\" b\"", TokenType.STRING))
    }

    @Test
    fun `numbers are recognised including hex and suffixes`() {
        val numbers = textsOf("val a = 42; val b = 0xFF; val c = 1_000L; val d = 3.14f", TokenType.NUMBER)
        assertEquals(listOf("42", "0xFF", "1_000L", "3.14f"), numbers)
    }

    @Test
    fun `tokens are ordered and never overlap`() {
        val tokens: List<KotlinToken> = KotlinLexer.tokenize(sample)
        var previousEnd = 0
        for (token in tokens) {
            assertTrue("token starts before previous ended", token.start >= previousEnd)
            assertTrue("token range inverted", token.end > token.start)
            assertTrue("token past end of text", token.end <= sample.length)
            previousEnd = token.end
        }
    }
}
