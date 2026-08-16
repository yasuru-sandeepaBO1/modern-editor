package com.example.modern_editor.editor.markdown

/**
 * Turns Markdown source into an AST with markers stripped.
 *
 * This is a practical CommonMark/GFM subset for preview (headings, emphasis, lists,
 * quotes, tables, fences, images, links, task items, thematic breaks). It is not a
 * highlighter — [MarkdownLexer] stays responsible for in-editor colouring.
 */
class MarkdownParser private constructor(private val lines: List<String>) {

    private var i = 0

    fun parseDocument(): List<MdBlock> {
        val blocks = mutableListOf<MdBlock>()
        while (i < lines.size) {
            if (lines[i].isBlank()) {
                i++
                continue
            }
            blocks += parseBlock()
        }
        return blocks
    }

    private fun parseBlock(): MdBlock {
        val line = lines[i]
        matchFence(line)?.let { return parseFence(it) }
        matchHeading(line)?.let { heading ->
            i++
            return heading
        }
        if (isHr(line)) {
            i++
            return MdBlock.HorizontalRule
        }
        if (isTableAt(i)) return parseTable()
        if (matchListItem(line) != null) return parseList()
        if (isBlockQuote(line)) return parseQuote()
        return parseParagraph()
    }

    private fun parseFence(open: Fence): MdBlock {
        i++
        val body = StringBuilder()
        while (i < lines.size) {
            val line = lines[i]
            if (isClosingFence(line, open)) {
                i++
                break
            }
            if (body.isNotEmpty()) body.append('\n')
            body.append(stripIndent(line, open.indent))
            i++
        }
        val code = body.toString()
        val lang = open.lang
        return if (lang != null && lang.equals("mermaid", ignoreCase = true)) {
            MdBlock.Mermaid(code)
        } else {
            MdBlock.CodeBlock(lang, code)
        }
    }

    private fun parseTable(): MdBlock.Table {
        val headers = splitCells(lines[i]).map { parseInlines(it) }
        i++
        val aligns = splitCells(lines[i]).map { cell ->
            val left = cell.startsWith(':')
            val right = cell.endsWith(':')
            when {
                left && right -> MdAlign.CENTER
                right -> MdAlign.RIGHT
                else -> MdAlign.LEFT
            }
        }
        i++
        val rows = mutableListOf<List<List<MdInline>>>()
        while (i < lines.size && !lines[i].isBlank() && looksLikeTableRow(lines[i])) {
            if (matchFence(lines[i]) != null || matchHeading(lines[i]) != null) break
            rows += splitCells(lines[i]).map { parseInlines(it) }
            i++
        }
        return MdBlock.Table(headers, aligns, rows)
    }

    private fun parseList(): MdBlock.ListBlock {
        val first = matchListItem(lines[i])!!
        val items = mutableListOf<MdListItem>()
        val listIndent = first.indent
        val ordered = first.ordered
        val startNum = first.start
        while (i < lines.size) {
            if (lines[i].isBlank()) {
                val next = indexOfNonBlank(i) ?: break
                val m = matchListItem(lines[next])
                if (m != null && m.indent == listIndent && m.ordered == ordered) {
                    i = next
                    continue
                }
                break
            }
            val m = matchListItem(lines[i]) ?: break
            if (m.indent != listIndent || m.ordered != ordered) break
            items += parseListItem(listIndent)
        }
        return MdBlock.ListBlock(ordered = ordered, start = startNum, items = items)
    }

    private fun parseListItem(listIndent: Int): MdListItem {
        val m = matchListItem(lines[i])!!
        val markerWidth = m.markerEndCol - m.indent
        val contentIndent = m.indent + markerWidth
        val bodyLines = mutableListOf(m.content)
        i++
        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank()) {
                val next = indexOfNonBlank(i + 1) ?: break
                val nm = matchListItem(lines[next])
                if (nm != null && nm.indent <= listIndent) break
                bodyLines += ""
                i++
                continue
            }
            val nm = matchListItem(line)
            if (nm != null) {
                if (nm.indent <= listIndent) break
                bodyLines += stripIndent(line, contentIndent)
                i++
                continue
            }
            val ind = leadingSpaces(line)
            if (ind >= contentIndent) {
                bodyLines += stripIndent(line, contentIndent)
                i++
                continue
            }
            if (!isInterruptingBlock(line, i) && bodyLines.any { it.isNotBlank() }) {
                bodyLines += line.trimStart()
                i++
                continue
            }
            break
        }
        val inner = MarkdownParser(bodyLines).parseDocument()
        val blocks = inner.ifEmpty {
            listOf(MdBlock.Paragraph(parseInlines(m.content)))
        }
        return MdListItem(checked = m.checked, blocks = blocks)
    }

    private fun parseQuote(): MdBlock.BlockQuote {
        val inner = mutableListOf<String>()
        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank()) break
            if (!isBlockQuote(line)) {
                if (!isInterruptingBlock(line, i)) {
                    inner += line
                    i++
                    continue
                }
                break
            }
            inner += stripQuotePrefix(line)
            i++
        }
        return MdBlock.BlockQuote(MarkdownParser(inner).parseDocument())
    }

    private fun parseParagraph(): MdBlock.Paragraph {
        val inlines = mutableListOf<MdInline>()
        var first = true
        var prevHard = false
        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank()) break
            if (!first && isInterruptingBlock(line, i)) break
            val hard = line.endsWith("  ")
            val trimmed = stripIndent(line, leadingSpaces(line).coerceAtMost(3)).trimEnd()
            if (!first) {
                if (prevHard) inlines += MdInline.HardBreak
                else inlines += MdInline.Text(" ")
            }
            inlines += parseInlines(trimmed)
            prevHard = hard
            first = false
            i++
        }
        return MdBlock.Paragraph(mergeInlines(inlines))
    }

    private fun isInterruptingBlock(line: String, index: Int): Boolean {
        if (matchFence(line) != null) return true
        if (matchHeading(line) != null) return true
        if (isHr(line)) return true
        if (isBlockQuote(line)) return true
        if (matchListItem(line) != null) return true
        if (isTableAt(index)) return true
        return false
    }

    private fun isTableAt(index: Int): Boolean {
        if (index + 1 >= lines.size) return false
        if (!looksLikeTableRow(lines[index])) return false
        return isTableSeparator(lines[index + 1])
    }

    private fun indexOfNonBlank(from: Int): Int? {
        var j = from
        while (j < lines.size && lines[j].isBlank()) j++
        return j.takeIf { it < lines.size }
    }

    private data class Fence(val char: Char, val length: Int, val lang: String?, val indent: Int)

    companion object {
        fun parse(source: String): List<MdBlock> =
            MarkdownParser(splitLines(source)).parseDocument()

        internal fun splitLines(source: String): List<String> =
            source.split('\n').map { line ->
                if (line.endsWith('\r')) line.dropLast(1) else line
            }

        internal fun parseInlines(text: String): List<MdInline> {
            val out = mutableListOf<MdInline>()
            val buf = StringBuilder()
            fun flush() {
                if (buf.isNotEmpty()) {
                    out += MdInline.Text(buf.toString())
                    buf.clear()
                }
            }
            var i = 0
            while (i < text.length) {
                when {
                    text[i] == '`' -> {
                        val span = parseCodeSpan(text, i)
                        if (span != null) {
                            flush()
                            out += span.first
                            i = span.second
                        } else {
                            buf.append(text[i])
                            i++
                        }
                    }
                    text.startsWith("![", i) -> {
                        val img = parseImage(text, i)
                        if (img != null) {
                            flush()
                            out += img.first
                            i = img.second
                        } else {
                            buf.append(text[i])
                            i++
                        }
                    }
                    text[i] == '[' -> {
                        val link = parseLink(text, i)
                        if (link != null) {
                            flush()
                            out += link.first
                            i = link.second
                        } else {
                            buf.append(text[i])
                            i++
                        }
                    }
                    text.startsWith("~~", i) -> {
                        val strike = parseWrapped(text, i, '~', 2) { MdInline.Strike(it) }
                        if (strike != null) {
                            flush()
                            out += strike.first
                            i = strike.second
                        } else {
                            buf.append(text[i])
                            i++
                        }
                    }
                    text[i] == '*' || text[i] == '_' -> {
                        val emphasis = parseEmphasis(text, i)
                        if (emphasis != null) {
                            flush()
                            out += emphasis.first
                            i = emphasis.second
                        } else {
                            buf.append(text[i])
                            i++
                        }
                    }
                    else -> {
                        buf.append(text[i])
                        i++
                    }
                }
            }
            flush()
            return mergeInlines(out)
        }

        private fun parseEmphasis(text: String, start: Int): Pair<MdInline, Int>? {
            val delim = text[start]
            val run = runLength(text, start, delim)
            val tryCounts = when {
                run >= 3 -> intArrayOf(3, 2, 1)
                run == 2 -> intArrayOf(2)
                else -> intArrayOf(1)
            }
            for (count in tryCounts) {
                val close = findClosingRun(text, start + count, delim, count)
                if (close >= start + count) {
                    val inner = text.substring(start + count, close)
                    if (inner.isEmpty()) continue
                    val children = parseInlines(inner)
                    val node = when (count) {
                        3 -> MdInline.Bold(listOf(MdInline.Italic(children)))
                        2 -> MdInline.Bold(children)
                        else -> MdInline.Italic(children)
                    }
                    return node to (close + count)
                }
            }
            return null
        }

        private fun parseWrapped(
            text: String,
            start: Int,
            delim: Char,
            count: Int,
            wrap: (List<MdInline>) -> MdInline
        ): Pair<MdInline, Int>? {
            val close = findClosingRun(text, start + count, delim, count)
            if (close < start + count) return null
            val inner = text.substring(start + count, close)
            if (inner.isEmpty()) return null
            return wrap(parseInlines(inner)) to (close + count)
        }

        private fun parseCodeSpan(text: String, start: Int): Pair<MdInline.Code, Int>? {
            val run = runLength(text, start, '`')
            var i = start + run
            while (i < text.length) {
                if (text[i] == '`') {
                    val closeRun = runLength(text, i, '`')
                    if (closeRun == run) {
                        var content = text.substring(start + run, i)
                        if (content.length >= 2 && content.startsWith(' ') && content.endsWith(' ')) {
                            content = content.substring(1, content.length - 1)
                        }
                        return MdInline.Code(content) to (i + closeRun)
                    }
                    i += closeRun
                } else {
                    i++
                }
            }
            return null
        }

        private fun parseLink(text: String, start: Int): Pair<MdInline.Link, Int>? {
            if (text[start] != '[') return null
            val closeBracket = findUnescaped(text, start + 1, ']') ?: return null
            if (closeBracket + 1 >= text.length || text[closeBracket + 1] != '(') return null
            val closeParen = findUnescaped(text, closeBracket + 2, ')') ?: return null
            val label = text.substring(start + 1, closeBracket)
            val dest = text.substring(closeBracket + 2, closeParen).trim()
            val url = parseDestTitle(dest).first
            return MdInline.Link(url, parseInlines(label)) to (closeParen + 1)
        }

        private fun parseImage(text: String, start: Int): Pair<MdInline.Image, Int>? {
            if (!text.startsWith("![", start)) return null
            val closeBracket = findUnescaped(text, start + 2, ']') ?: return null
            if (closeBracket + 1 >= text.length || text[closeBracket + 1] != '(') return null
            val closeParen = findUnescaped(text, closeBracket + 2, ')') ?: return null
            val alt = text.substring(start + 2, closeBracket)
            val dest = text.substring(closeBracket + 2, closeParen).trim()
            val (url, title) = parseDestTitle(dest)
            return MdInline.Image(alt, url, title) to (closeParen + 1)
        }

        internal fun parseDestTitle(dest: String): Pair<String, String?> {
            val trimmed = dest.trim()
            if (trimmed.isEmpty()) return "" to null
            val quote = when {
                trimmed.endsWith('"') -> '"'
                trimmed.endsWith('\'') -> '\''
                else -> null
            }
            if (quote != null) {
                val open = trimmed.indexOf(quote)
                if (open > 0 && open < trimmed.lastIndex) {
                    val url = trimmed.substring(0, open).trim()
                    val title = trimmed.substring(open + 1, trimmed.lastIndex)
                    return url to title
                }
            }
            return trimmed to null
        }

        private fun findUnescaped(text: String, from: Int, target: Char): Int? {
            var i = from
            while (i < text.length) {
                if (text[i] == target) return i
                i++
            }
            return null
        }

        private fun findClosingRun(text: String, from: Int, delim: Char, count: Int): Int {
            var i = from
            while (i < text.length) {
                if (text[i] == '`') {
                    val end = endOfCodeSpan(text, i)
                    i = if (end > i) end else i + 1
                    continue
                }
                if (text[i] == delim) {
                    val run = runLength(text, i, delim)
                    if (run == count) return i
                    i += run
                } else {
                    i++
                }
            }
            return -1
        }

        private fun endOfCodeSpan(text: String, start: Int): Int {
            val parsed = parseCodeSpan(text, start) ?: return start
            return parsed.second
        }

        private fun runLength(text: String, start: Int, c: Char): Int {
            var i = start
            while (i < text.length && text[i] == c) i++
            return i - start
        }

        internal fun mergeInlines(list: List<MdInline>): List<MdInline> {
            val out = mutableListOf<MdInline>()
            for (node in list) {
                val last = out.lastOrNull()
                if (node is MdInline.Text && last is MdInline.Text) {
                    out[out.lastIndex] = MdInline.Text(last.value + node.value)
                } else {
                    out += node
                }
            }
            return out.filter { it !is MdInline.Text || it.value.isNotEmpty() }
        }

        internal fun matchHeading(line: String): MdBlock.Heading? {
            if (leadingSpaces(line) > 3) return null
            val indent = leadingSpaces(line).coerceAtMost(3)
            var i = skipSpaces(line, 0, indent)
            var hashes = 0
            while (i < line.length && line[i] == '#') {
                hashes++
                i++
            }
            if (hashes !in 1..6) return null
            if (i < line.length && line[i] != ' ' && line[i] != '\t') return null
            var content = if (i < line.length) line.substring(i).trim() else ""
            content = TRAILING_HASHES.replace(content, "").trim()
            return MdBlock.Heading(hashes, parseInlines(content))
        }

        private fun matchFence(line: String): Fence? {
            val spaces = leadingSpaces(line)
            if (spaces > 3) return null
            val indentChars = skipSpaces(line, 0, spaces)
            if (indentChars >= line.length) return null
            val c = line[indentChars]
            if (c != '`' && c != '~') return null
            var n = 0
            var i = indentChars
            while (i < line.length && line[i] == c) {
                n++
                i++
            }
            if (n < 3) return null
            val info = line.substring(i).trim()
            if (c == '`' && info.contains('`')) return null
            val lang = info.split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotEmpty() }
            return Fence(c, n, lang, spaces)
        }

        private fun isClosingFence(line: String, open: Fence): Boolean {
            val spaces = leadingSpaces(line)
            if (spaces > 3) return false
            val start = skipSpaces(line, 0, spaces)
            if (start >= line.length || line[start] != open.char) return false
            var n = 0
            var i = start
            while (i < line.length && line[i] == open.char) {
                n++
                i++
            }
            if (n < open.length) return false
            return line.substring(i).isBlank()
        }

        internal fun isHr(line: String): Boolean {
            val s = line.trim()
            if (s.length < 3) return false
            val c = s[0]
            if (c != '-' && c != '*' && c != '_') return false
            var count = 0
            for (ch in s) {
                when (ch) {
                    c -> count++
                    ' ', '\t' -> {}
                    else -> return false
                }
            }
            return count >= 3
        }

        internal fun isBlockQuote(line: String): Boolean {
            val spaces = leadingSpaces(line)
            if (spaces > 3) return false
            val i = skipSpaces(line, 0, spaces)
            return i < line.length && line[i] == '>'
        }

        internal fun stripQuotePrefix(line: String): String {
            val spaces = leadingSpaces(line).coerceAtMost(3)
            var i = skipSpaces(line, 0, spaces)
            if (i < line.length && line[i] == '>') {
                i++
                if (i < line.length && (line[i] == ' ' || line[i] == '\t')) i++
            }
            return line.substring(i)
        }

        internal data class ListMarkerMatch(
            val indent: Int,
            val ordered: Boolean,
            val start: Int,
            val markerEndCol: Int,
            val content: String,
            val checked: Boolean?
        )

        internal fun matchListItem(line: String): ListMarkerMatch? {
            val indent = leadingSpaces(line)
            var i = skipSpaces(line, 0, indent)
            if (i >= line.length) return null
            val ordered: Boolean
            val start: Int
            when {
                line[i] == '-' || line[i] == '+' || line[i] == '*' -> {
                    if (i + 1 >= line.length) return null
                    if (line[i + 1] != ' ' && line[i + 1] != '\t') return null
                    ordered = false
                    start = 1
                    i += 2
                }
                line[i].isDigit() -> {
                    val numStart = i
                    while (i < line.length && line[i].isDigit()) i++
                    if (i >= line.length) return null
                    if (line[i] != '.' && line[i] != ')') return null
                    val afterMark = i + 1
                    if (afterMark < line.length && line[afterMark] != ' ' && line[afterMark] != '\t') {
                        return null
                    }
                    ordered = true
                    start = line.substring(numStart, i).toInt()
                    i = afterMark
                    if (i < line.length && (line[i] == ' ' || line[i] == '\t')) i++
                }
                else -> return null
            }
            var content = if (i <= line.length) line.substring(i) else ""
            var checked: Boolean? = null
            val task = TASK_ITEM.matchAt(content, 0)
            if (task != null) {
                checked = task.groupValues[1] != " "
                content = content.substring(task.range.last + 1).trimStart()
            }
            val markerEndCol = indent + (i - skipSpaces(line, 0, indent))
            return ListMarkerMatch(indent, ordered, start, markerEndCol, content, checked)
        }

        internal fun looksLikeTableRow(line: String): Boolean {
            val t = line.trim()
            return t.contains('|')
        }

        internal fun isTableSeparator(line: String): Boolean {
            val cells = splitCells(line)
            if (cells.isEmpty()) return false
            return cells.all { cell ->
                cell.isNotEmpty() && cell.all { it == '-' || it == ':' } && cell.contains('-')
            }
        }

        internal fun splitCells(line: String): List<String> {
            val t = line.trim()
            if (t.isEmpty()) return emptyList()
            var inner = t
            if (inner.startsWith('|')) inner = inner.substring(1)
            if (inner.endsWith('|')) inner = inner.dropLast(1)
            return inner.split('|').map { it.trim() }
        }

        internal fun leadingSpaces(line: String): Int {
            var n = 0
            for (c in line) {
                when (c) {
                    ' ' -> n++
                    '\t' -> n += 4 - (n % 4)
                    else -> return n
                }
            }
            return n
        }

        internal fun skipSpaces(line: String, from: Int, spaceBudget: Int): Int {
            var i = from
            var counted = 0
            while (i < line.length && counted < spaceBudget) {
                when (line[i]) {
                    ' ' -> {
                        counted++
                        i++
                    }
                    '\t' -> {
                        counted += 4 - (counted % 4)
                        i++
                    }
                    else -> return i
                }
            }
            return i
        }

        internal fun stripIndent(line: String, n: Int): String {
            if (n <= 0) return line
            var removed = 0
            var i = 0
            while (i < line.length && removed < n) {
                when (line[i]) {
                    ' ' -> {
                        removed++
                        i++
                    }
                    '\t' -> {
                        removed += 4 - (removed % 4)
                        i++
                    }
                    else -> break
                }
            }
            return line.substring(i)
        }

        private val TRAILING_HASHES = Regex("""\s+#+\s*$""")
        private val TASK_ITEM = Regex("""^\[([ xX])](\s+|$)""")
    }
}
