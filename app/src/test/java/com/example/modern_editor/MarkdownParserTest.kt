package com.example.modern_editor

import com.example.modern_editor.editor.markdown.MdAlign
import com.example.modern_editor.editor.markdown.MdBlock
import com.example.modern_editor.editor.markdown.MdInline
import com.example.modern_editor.editor.markdown.MarkdownParser
import com.example.modern_editor.editor.markdown.MermaidGraph
import com.example.modern_editor.editor.markdown.plainText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TC6.7 — preview parser: rendered AST corresponds to source, with markers stripped.
 */
class MarkdownParserTest {

    @Test
    fun `TC6_7 heading markers are stripped`() {
        val blocks = MarkdownParser.parse("# Hello")
        val heading = blocks.single() as MdBlock.Heading
        assertEquals(1, heading.level)
        assertEquals("Hello", heading.plainText())
        assertFalse(heading.plainText().contains("#"))
    }

    @Test
    fun `heading levels 1 2 and 6`() {
        val blocks = MarkdownParser.parse(
            "# This is a Heading h1\n## This is a Heading h2\n###### This is a Heading h6"
        )
        val headings = blocks.filterIsInstance<MdBlock.Heading>()
        assertEquals(listOf(1, 2, 6), headings.map { it.level })
        assertEquals(
            listOf("This is a Heading h1", "This is a Heading h2", "This is a Heading h6"),
            headings.map { it.plainText() }
        )
    }

    @Test
    fun `bold and italic markers are stripped`() {
        val italic = MarkdownParser.parse("*This text will be italic*").single() as MdBlock.Paragraph
        assertTrue(italic.inlines.single() is MdInline.Italic)
        assertEquals("This text will be italic", italic.plainText())

        val bold = MarkdownParser.parse("**This text will be bold**").single() as MdBlock.Paragraph
        assertTrue(bold.inlines.single() is MdInline.Bold)
        assertEquals("This text will be bold", bold.plainText())

        val underscore = MarkdownParser.parse("__This will also be bold__").single() as MdBlock.Paragraph
        assertTrue(underscore.inlines.single() is MdInline.Bold)
        assertEquals("This will also be bold", underscore.plainText())
    }

    @Test
    fun `combined emphasis nests bold inside italic`() {
        val para = MarkdownParser.parse("_You **can** combine them_").single() as MdBlock.Paragraph
        val italic = para.inlines.single() as MdInline.Italic
        val bold = italic.children.filterIsInstance<MdInline.Bold>().single()
        assertEquals("can", bold.plainText())
        assertEquals("You can combine them", para.plainText())
        assertFalse(para.plainText().contains("*") || para.plainText().contains("_"))
    }

    @Test
    fun `triple emphasis is bold italic`() {
        val para = MarkdownParser.parse("***Bold and italic***").single() as MdBlock.Paragraph
        val bold = para.inlines.single() as MdInline.Bold
        assertTrue(bold.children.single() is MdInline.Italic)
        assertEquals("Bold and italic", para.plainText())
    }

    @Test
    fun `strikethrough markers are stripped`() {
        val para = MarkdownParser.parse("~~gone~~").single() as MdBlock.Paragraph
        assertTrue(para.inlines.single() is MdInline.Strike)
        assertEquals("gone", para.plainText())
    }

    @Test
    fun `unordered list nests by indent`() {
        val src = """
            * Item 1
            * Item 2
            * Item 2a
            * Item 2b
                * Item 3a
                * Item 3b
        """.trimIndent()
        val list = MarkdownParser.parse(src).single() as MdBlock.ListBlock
        assertFalse(list.ordered)
        assertEquals(4, list.items.size)
        assertEquals("Item 1", list.items[0].blocks.plainText())
        val nested = list.items[3].blocks.filterIsInstance<MdBlock.ListBlock>().single()
        assertEquals(listOf("Item 3a", "Item 3b"), nested.items.map { it.blocks.plainText() })
        assertFalse(list.plainText().contains("*"))
    }

    @Test
    fun `ordered list nests by indent and keeps start number`() {
        val src = """
            1. Item 1
            2. Item 2
            3. Item 3
                1. Item 3a
                2. Item 3b
        """.trimIndent()
        val list = MarkdownParser.parse(src).single() as MdBlock.ListBlock
        assertTrue(list.ordered)
        assertEquals(1, list.start)
        assertEquals(3, list.items.size)
        val nested = list.items[2].blocks.filterIsInstance<MdBlock.ListBlock>().single()
        assertTrue(nested.ordered)
        assertEquals(listOf("Item 3a", "Item 3b"), nested.items.map { it.blocks.plainText() })
    }

    @Test
    fun `task list items capture checked state`() {
        val src = "- [ ] Todo\n- [x] Completed"
        val list = MarkdownParser.parse(src).single() as MdBlock.ListBlock
        assertEquals(false, list.items[0].checked)
        assertEquals(true, list.items[1].checked)
        assertEquals("Todo", list.items[0].blocks.plainText())
        assertEquals("Completed", list.items[1].blocks.plainText())
    }

    @Test
    fun `image alt url and title`() {
        val para = MarkdownParser.parse(
            "![This is an alt text.](/image/Markdown-mark.svg \"This is a sample image.\")"
        ).single() as MdBlock.Paragraph
        val image = para.inlines.single() as MdInline.Image
        assertEquals("This is an alt text.", image.alt)
        assertEquals("/image/Markdown-mark.svg", image.url)
        assertEquals("This is a sample image.", image.title)
    }

    @Test
    fun `link label is rendered without brackets`() {
        val para = MarkdownParser.parse(
            "You may be using [Markdown Live Preview](https://markdownlivepreview.com/)."
        ).single() as MdBlock.Paragraph
        val link = para.inlines.filterIsInstance<MdInline.Link>().single()
        assertEquals("Markdown Live Preview", link.plainText())
        assertEquals("https://markdownlivepreview.com/", link.url)
        assertEquals("You may be using Markdown Live Preview.", para.plainText())
    }

    @Test
    fun `nested blockquote strips quote markers`() {
        val src = """
            > Markdown is a lightweight markup language with plain-text-formatting syntax, created in 2004 by John Gruber with Aaron Swartz.
            >
            >> Markdown is often used to format readme files, for writing messages in online discussion forums, and to create rich text using a plain text editor.
        """.trimIndent()
        val quote = MarkdownParser.parse(src).single() as MdBlock.BlockQuote
        assertTrue(quote.children.any { it is MdBlock.Paragraph })
        val nested = quote.children.filterIsInstance<MdBlock.BlockQuote>().single()
        assertTrue(nested.plainText().contains("readme files"))
        assertFalse(quote.plainText().contains(">"))
    }

    @Test
    fun `GFM table has alignments and three data rows`() {
        val src = """
            | Left columns  | Right columns |
            | ------------- |:-------------:|
            | left foo      | right foo     |
            | left bar      | right bar     |
            | left baz      | right baz     |
        """.trimIndent()
        val table = MarkdownParser.parse(src).single() as MdBlock.Table
        assertEquals(listOf("Left columns", "Right columns"), table.headers.map { it.plainText() })
        assertEquals(listOf(MdAlign.LEFT, MdAlign.CENTER), table.aligns)
        assertEquals(3, table.rows.size)
        assertEquals("left foo", table.rows[0][0].plainText())
        assertEquals("right baz", table.rows[2][1].plainText())
        assertFalse(table.plainText().contains("---"))
    }

    @Test
    fun `fenced code hides fence markers and is not inline-parsed`() {
        val src = """
            ```
            let message = 'Hello world';
            alert(message);
            ```
        """.trimIndent()
        val code = MarkdownParser.parse(src).single() as MdBlock.CodeBlock
        assertNull(code.language)
        assertEquals("let message = 'Hello world';\nalert(message);", code.code)
        assertFalse(code.code.contains("```"))
    }

    @Test
    fun `markdown inside a non-mermaid fence stays code`() {
        val src = "```\n# not a heading\n**not bold**\n```"
        val code = MarkdownParser.parse(src).single() as MdBlock.CodeBlock
        assertEquals("# not a heading\n**not bold**", code.code)
    }

    @Test
    fun `mermaid fence is a mermaid block not a code listing`() {
        val src = """
            ```mermaid
            graph TD
              A[Start] --> B{Decision}
            ```
        """.trimIndent()
        val mermaid = MarkdownParser.parse(src).single() as MdBlock.Mermaid
        assertTrue(mermaid.source.contains("graph TD"))
        assertFalse(mermaid.source.contains("```"))
    }

    @Test
    fun `inline code strips backticks`() {
        val para = MarkdownParser.parse(
            "This web site is using `markedjs/marked`."
        ).single() as MdBlock.Paragraph
        val code = para.inlines.filterIsInstance<MdInline.Code>().single()
        assertEquals("markedjs/marked", code.value)
        assertEquals("This web site is using markedjs/marked.", para.plainText())
    }

    @Test
    fun `horizontal rule is not literal dashes`() {
        val blocks = MarkdownParser.parse("before\n\n---\n\nafter")
        assertTrue(blocks.any { it is MdBlock.HorizontalRule })
        assertEquals("before", (blocks[0] as MdBlock.Paragraph).plainText())
        assertEquals("after", (blocks.last() as MdBlock.Paragraph).plainText())
    }

    @Test
    fun `hard line break from two trailing spaces`() {
        val para = MarkdownParser.parse("left  \nright").single() as MdBlock.Paragraph
        assertTrue(para.inlines.any { it is MdInline.HardBreak })
        assertEquals("left\nright", para.plainText())
    }

    @Test
    fun `TC6_7 full sample fixture`() {
        val blocks = MarkdownParser.parse(SAMPLE)
        assertTrue(blocks.filterIsInstance<MdBlock.Heading>().any { it.plainText() == "Markdown syntax guide" })
        assertTrue(blocks.filterIsInstance<MdBlock.Heading>().none { it.plainText().startsWith("#") })

        val table = blocks.filterIsInstance<MdBlock.Table>().single()
        assertEquals(3, table.rows.size)

        val mermaid = blocks.filterIsInstance<MdBlock.Mermaid>().single()
        assertTrue(mermaid.source.contains("graph TD"))

        val code = blocks.filterIsInstance<MdBlock.CodeBlock>().single()
        assertTrue(code.code.contains("Hello world"))

        val image = blocks.filterIsInstance<MdBlock.Paragraph>()
            .flatMap { it.inlines }
            .filterIsInstance<MdInline.Image>()
            .single()
        assertEquals("This is an alt text.", image.alt)

        val link = blocks.filterIsInstance<MdBlock.Paragraph>()
            .flatMap { it.inlines }
            .filterIsInstance<MdInline.Link>()
            .single()
        assertEquals("https://markdownlivepreview.com/", link.url)

        val quote = blocks.filterIsInstance<MdBlock.BlockQuote>().single()
        assertTrue(quote.children.any { it is MdBlock.BlockQuote })

        val lists = blocks.filterIsInstance<MdBlock.ListBlock>()
        assertTrue(lists.any { !it.ordered && it.items.size == 4 })
        assertTrue(lists.any { it.ordered && it.items.size == 3 })
    }

    @Test
    fun `mermaid graph TD sample parses nodes and labeled edges`() {
        val graph = MermaidGraph.parse(
            """
            graph TD
              A[Start] --> B{Decision}
              B -->|Yes| C[Finish]
              B -->|No| D[Alternate]
            """.trimIndent()
        )
        assertNotNull(graph)
        assertEquals(MermaidGraph.Direction.TD, graph!!.direction)
        assertEquals(listOf("A", "B", "C", "D"), graph.nodes.map { it.id })
        assertEquals("Start", graph.nodes.first { it.id == "A" }.label)
        assertEquals(MermaidGraph.Shape.DIAMOND, graph.nodes.first { it.id == "B" }.shape)
        assertEquals(3, graph.edges.size)
        assertEquals("Yes", graph.edges.first { it.to == "C" }.label)
        assertEquals("No", graph.edges.first { it.to == "D" }.label)
        val layers = graph.layers()
        assertEquals("A", layers[0].single().id)
        assertEquals("B", layers[1].single().id)
        assertEquals(setOf("C", "D"), layers[2].map { it.id }.toSet())
    }

    @Test
    fun `unsupported mermaid types are not graphs`() {
        assertNull(MermaidGraph.parse("sequenceDiagram\nAlice->>Bob: Hi"))
        assertNull(MermaidGraph.parse(""))
    }

    companion object {
        private val SAMPLE = """
            # Markdown syntax guide

            ## Headers

            # This is a Heading h1
            ## This is a Heading h2
            ###### This is a Heading h6

            ## Emphasis

            *This text will be italic*  
            _This will also be italic_

            **This text will be bold**  
            __This will also be bold__

            _You **can** combine them_

            ## Lists

            ### Unordered

            * Item 1
            * Item 2
            * Item 2a
            * Item 2b
                * Item 3a
                * Item 3b

            ### Ordered

            1. Item 1
            2. Item 2
            3. Item 3
                1. Item 3a
                2. Item 3b

            ## Images

            ![This is an alt text.](/image/Markdown-mark.svg "This is a sample image.")

            ## Links

            You may be using [Markdown Live Preview](https://markdownlivepreview.com/).

            ## Blockquotes

            > Markdown is a lightweight markup language with plain-text-formatting syntax, created in 2004 by John Gruber with Aaron Swartz.
            >
            >> Markdown is often used to format readme files, for writing messages in online discussion forums, and to create rich text using a plain text editor.

            ## Tables

            | Left columns  | Right columns |
            | ------------- |:-------------:|
            | left foo      | right foo     |
            | left bar      | right bar     |
            | left baz      | right baz     |

            ## Blocks of code

            ```
            let message = 'Hello world';
            alert(message);
            ```

            ## Mermaid diagrams
            ```mermaid
            graph TD
              A[Start] --> B{Decision}
              B -->|Yes| C[Finish]
              B -->|No| D[Alternate]
            ```

            ## Inline code

            This web site is using `markedjs/marked`.
        """.trimIndent()
    }
}
