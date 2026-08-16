package com.example.modern_editor.ui.screens.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.editor.markdown.MdAlign
import com.example.modern_editor.editor.markdown.MdBlock
import com.example.modern_editor.editor.markdown.MdInline
import com.example.modern_editor.editor.markdown.MdListItem
import com.example.modern_editor.editor.markdown.MermaidGraph
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.EditorSurface
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.InterFontFamily
import com.example.modern_editor.ui.theme.JetBrainsMonoFamily
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.SyntaxColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun MarkdownDocument(
    blocks: List<MdBlock>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        blocks.forEach { MarkdownBlock(it) }
    }
}

@Composable
private fun MarkdownBlock(block: MdBlock, modifier: Modifier = Modifier) {
    when (block) {
        is MdBlock.Heading -> HeadingBlock(block, modifier)
        is MdBlock.Paragraph -> ParagraphBlock(block.inlines, modifier)
        is MdBlock.ListBlock -> ListBlock(block, modifier)
        is MdBlock.BlockQuote -> QuoteBlock(block, modifier)
        is MdBlock.CodeBlock -> CodeBlock(block, modifier)
        is MdBlock.Mermaid -> MermaidBlock(block, modifier)
        is MdBlock.Table -> TableBlock(block, modifier)
        MdBlock.HorizontalRule -> Box(
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(1.dp)
                .background(InactiveSurface)
        )
    }
}

@Composable
private fun HeadingBlock(block: MdBlock.Heading, modifier: Modifier = Modifier) {
    val size = when (block.level) {
        1 -> 32.sp
        2 -> 26.sp
        3 -> 22.sp
        4 -> 18.sp
        5 -> 16.sp
        else -> 14.sp
    }
    Text(
        text = remember(block.inlines) { inlinesToAnnotated(block.inlines) },
        color = PrimaryText,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        fontFamily = InterFontFamily,
        lineHeight = size * 1.25f,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (block.level <= 2) 8.dp else 0.dp)
    )
}

@Composable
private fun ParagraphBlock(inlines: List<MdInline>, modifier: Modifier = Modifier) {
    val segments = remember(inlines) { splitAroundImages(inlines) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        segments.forEach { segment ->
            when (segment) {
                is InlineSegment.Image -> ImagePlaceholder(segment.image)
                is InlineSegment.TextRun -> if (segment.inlines.isNotEmpty()) {
                    Text(
                        text = remember(segment.inlines) { inlinesToAnnotated(segment.inlines) },
                        color = PrimaryText,
                        fontSize = 16.sp,
                        fontFamily = InterFontFamily,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ListBlock(block: MdBlock.ListBlock, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        block.items.forEachIndexed { index, item ->
            ListItemRow(
                item = item,
                ordered = block.ordered,
                number = block.start + index
            )
        }
    }
}

@Composable
private fun ListItemRow(item: MdListItem, ordered: Boolean, number: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (item.checked != null) {
            TaskBox(checked = item.checked, modifier = Modifier.padding(top = 3.dp))
        } else {
            Text(
                text = if (ordered) "$number." else "•",
                color = PrimaryText,
                fontSize = 16.sp,
                fontFamily = InterFontFamily,
                modifier = Modifier.width(24.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.blocks.forEach { MarkdownBlock(it) }
        }
    }
}

@Composable
private fun TaskBox(checked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .border(1.dp, ButtonText, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(PrimaryText, RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
private fun QuoteBlock(block: MdBlock.BlockQuote, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(GutterText, RoundedCornerShape(1.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            block.children.forEach { child ->
                MarkdownBlock(child)
            }
        }
    }
}

@Composable
private fun CodeBlock(block: MdBlock.CodeBlock, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EditorSurface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        if (!block.language.isNullOrBlank()) {
            Text(
                text = block.language,
                color = GutterText,
                fontSize = 11.sp,
                fontFamily = InterFontFamily,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Text(
            text = block.code,
            color = SyntaxColors.CodeText,
            fontSize = 14.sp,
            fontFamily = JetBrainsMonoFamily,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MermaidBlock(block: MdBlock.Mermaid, modifier: Modifier = Modifier) {
    val graph = remember(block.source) { MermaidGraph.parse(block.source) }
    if (graph == null) {
        CodeBlock(MdBlock.CodeBlock(language = "mermaid", code = block.source), modifier)
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(EditorSurface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        MermaidDiagram(graph = graph, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TableBlock(block: MdBlock.Table, modifier: Modifier = Modifier) {
    val colCount = max(
        block.headers.size,
        block.rows.maxOfOrNull { it.size } ?: 0
    ).coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .border(1.dp, InactiveSurface, RoundedCornerShape(8.dp))
    ) {
        TableRowCells(
            cells = block.headers,
            aligns = block.aligns,
            colCount = colCount,
            header = true
        )
        block.rows.forEach { row ->
            Box(Modifier.fillMaxWidth().height(1.dp).background(InactiveSurface))
            TableRowCells(
                cells = row,
                aligns = block.aligns,
                colCount = colCount,
                header = false
            )
        }
    }
}

@Composable
private fun TableRowCells(
    cells: List<List<MdInline>>,
    aligns: List<MdAlign>,
    colCount: Int,
    header: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (header) ButtonSurface else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        repeat(colCount) { index ->
            val cell = cells.getOrNull(index).orEmpty()
            val align = aligns.getOrNull(index) ?: MdAlign.LEFT
            val textAlign = when (align) {
                MdAlign.LEFT -> TextAlign.Start
                MdAlign.CENTER -> TextAlign.Center
                MdAlign.RIGHT -> TextAlign.End
            }
            Text(
                text = remember(cell) { inlinesToAnnotated(cell) },
                color = PrimaryText,
                fontSize = 14.sp,
                fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
                fontFamily = InterFontFamily,
                textAlign = textAlign,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun ImagePlaceholder(image: MdInline.Image) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditorSurface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = image.alt.ifBlank { "Image" },
            color = PrimaryText,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = InterFontFamily
        )
        val caption = buildString {
            if (!image.title.isNullOrBlank()) append(image.title)
            if (image.url.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(image.url)
            }
        }
        if (caption.isNotEmpty()) {
            Text(
                text = caption,
                color = ButtonText,
                fontSize = 12.sp,
                fontFamily = InterFontFamily
            )
        }
    }
}

private sealed class InlineSegment {
    data class TextRun(val inlines: List<MdInline>) : InlineSegment()
    data class Image(val image: MdInline.Image) : InlineSegment()
}

private fun splitAroundImages(inlines: List<MdInline>): List<InlineSegment> {
    val out = mutableListOf<InlineSegment>()
    val buf = mutableListOf<MdInline>()
    fun flush() {
        if (buf.isNotEmpty()) {
            out += InlineSegment.TextRun(buf.toList())
            buf.clear()
        }
    }
    for (inline in inlines) {
        if (inline is MdInline.Image) {
            flush()
            out += InlineSegment.Image(inline)
        } else {
            buf += inline
        }
    }
    flush()
    return out
}

private fun inlinesToAnnotated(inlines: List<MdInline>) = buildAnnotatedString {
    appendInlines(inlines)
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlines(inlines: List<MdInline>) {
    for (inline in inlines) {
        when (inline) {
            is MdInline.Text -> append(inline.value)
            MdInline.HardBreak -> append('\n')
            is MdInline.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlines(inline.children)
            }
            is MdInline.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlines(inline.children)
            }
            is MdInline.Strike -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                appendInlines(inline.children)
            }
            is MdInline.Code -> withStyle(
                SpanStyle(
                    color = SyntaxColors.StringLiteral,
                    fontFamily = JetBrainsMonoFamily,
                    background = Color(0xFF080A14)
                )
            ) {
                append(inline.value)
            }
            is MdInline.Link -> withLink(LinkAnnotation.Url(inline.url)) {
                withStyle(
                    SpanStyle(
                        color = SyntaxColors.MarkdownLink,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    appendInlines(inline.children)
                }
            }
            is MdInline.Image -> append(inline.alt)
        }
    }
}

@Composable
internal fun MermaidDiagram(
    graph: MermaidGraph,
    modifier: Modifier = Modifier
) {
    val layers = remember(graph) { graph.layers() }
    val measurer = rememberTextMeasurer()
    val nodeFill = ButtonSurface
    val nodeStroke = InactiveSurface
    val labelColor = PrimaryText
    val edgeColor = ButtonText
    val rowHeight = if (graph.direction == MermaidGraph.Direction.TD) 108.dp else 96.dp
    val canvasHeight: Dp = when (graph.direction) {
        MermaidGraph.Direction.TD -> rowHeight * layers.size.coerceAtLeast(1) + 8.dp
        MermaidGraph.Direction.LR -> 220.dp
    }
    val labelStyle = TextStyle(
        color = labelColor,
        fontSize = 13.sp,
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
    val edgeLabelStyle = TextStyle(
        color = ButtonText,
        fontSize = 11.sp,
        fontFamily = InterFontFamily,
        textAlign = TextAlign.Center
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
    ) {
        if (layers.isEmpty()) return@Canvas
        val positions = linkedMapOf<String, NodeBox>()
        when (graph.direction) {
            MermaidGraph.Direction.TD -> {
                layers.forEachIndexed { li, layer ->
                    val y = 16.dp.toPx() + li * rowHeight.toPx() + 28.dp.toPx()
                    val n = layer.size
                    layer.forEachIndexed { ni, node ->
                        val x = size.width * (ni + 1).toFloat() / (n + 1).toFloat()
                        positions[node.id] = nodeBox(node, x, y)
                    }
                }
            }
            MermaidGraph.Direction.LR -> {
                layers.forEachIndexed { li, layer ->
                    val x = 24.dp.toPx() + li * 160.dp.toPx() + 60.dp.toPx()
                    val n = layer.size
                    layer.forEachIndexed { ni, node ->
                        val y = size.height * (ni + 1).toFloat() / (n + 1).toFloat()
                        positions[node.id] = nodeBox(node, x, y)
                    }
                }
            }
        }
        for (edge in graph.edges) {
            val from = positions[edge.from] ?: continue
            val to = positions[edge.to] ?: continue
            val start: Offset
            val end: Offset
            if (graph.direction == MermaidGraph.Direction.TD) {
                start = Offset(from.center.x, from.bottom)
                end = Offset(to.center.x, to.top)
            } else {
                start = Offset(from.right, from.center.y)
                end = Offset(to.left, to.center.y)
            }
            drawArrow(start, end, edgeColor, 2.dp.toPx())
            val label = edge.label
            if (!label.isNullOrBlank()) {
                val layout: TextLayoutResult = measurer.measure(label, edgeLabelStyle)
                val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(mid.x - layout.size.width / 2f, mid.y - layout.size.height - 4.dp.toPx())
                )
            }
        }
        for (node in graph.nodes) {
            val box = positions[node.id] ?: continue
            drawMermaidNode(box, node.shape, nodeFill, nodeStroke)
            val layout = measurer.measure(node.label, labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    box.center.x - layout.size.width / 2f,
                    box.center.y - layout.size.height / 2f
                )
            )
        }
    }
}

private data class NodeBox(
    val center: Offset,
    val width: Float,
    val height: Float
) {
    val left: Float get() = center.x - width / 2f
    val right: Float get() = center.x + width / 2f
    val top: Float get() = center.y - height / 2f
    val bottom: Float get() = center.y + height / 2f
}

private fun nodeBox(node: MermaidGraph.MermaidNode, x: Float, y: Float): NodeBox {
    val diamond = node.shape == MermaidGraph.Shape.DIAMOND
    val circle = node.shape == MermaidGraph.Shape.CIRCLE
    val w = when {
        diamond -> 108f
        circle -> 72f
        else -> 128f
    }
    val h = when {
        diamond -> 72f
        circle -> 72f
        else -> 48f
    }
    return NodeBox(Offset(x, y), w, h)
}

private fun DrawScope.drawMermaidNode(
    box: NodeBox,
    shape: MermaidGraph.Shape,
    fill: Color,
    stroke: Color
) {
    val strokeWidth = 2.dp.toPx()
    when (shape) {
        MermaidGraph.Shape.DIAMOND -> {
            val path = Path().apply {
                moveTo(box.center.x, box.top)
                lineTo(box.right, box.center.y)
                lineTo(box.center.x, box.bottom)
                lineTo(box.left, box.center.y)
                close()
            }
            drawPath(path, fill)
            drawPath(path, stroke, style = Stroke(width = strokeWidth))
        }
        MermaidGraph.Shape.CIRCLE -> {
            drawCircle(fill, radius = box.width / 2f, center = box.center)
            drawCircle(stroke, radius = box.width / 2f, center = box.center, style = Stroke(width = strokeWidth))
        }
        MermaidGraph.Shape.ROUND -> {
            drawRoundRect(
                color = fill,
                topLeft = Offset(box.left, box.top),
                size = Size(box.width, box.height),
                cornerRadius = CornerRadius(box.height / 2f, box.height / 2f)
            )
            drawRoundRect(
                color = stroke,
                topLeft = Offset(box.left, box.top),
                size = Size(box.width, box.height),
                cornerRadius = CornerRadius(box.height / 2f, box.height / 2f),
                style = Stroke(width = strokeWidth)
            )
        }
        MermaidGraph.Shape.RECT -> {
            drawRoundRect(
                color = fill,
                topLeft = Offset(box.left, box.top),
                size = Size(box.width, box.height),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            drawRoundRect(
                color = stroke,
                topLeft = Offset(box.left, box.top),
                size = Size(box.width, box.height),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

private fun DrawScope.drawArrow(from: Offset, to: Offset, color: Color, stroke: Float) {
    drawLine(color, from, to, strokeWidth = stroke, cap = StrokeCap.Round)
    val angle = atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble())
    val len = 12.dp.toPx()
    val a1 = angle + Math.PI * 0.8
    val a2 = angle - Math.PI * 0.8
    drawLine(
        color,
        to,
        Offset(to.x + (len * cos(a1)).toFloat(), to.y + (len * sin(a1)).toFloat()),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color,
        to,
        Offset(to.x + (len * cos(a2)).toFloat(), to.y + (len * sin(a2)).toFloat()),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}
