package com.example.modern_editor.editor.markdown

/**
 * A flowchart subset of Mermaid (`graph` / `flowchart` + TD/LR) used by Markdown preview.
 * Other diagram types return null so the renderer can fall back to a code block.
 */
data class MermaidGraph(
    val direction: Direction,
    val nodes: List<MermaidNode>,
    val edges: List<MermaidEdge>
) {
    enum class Direction { TD, LR }

    enum class Shape { RECT, ROUND, CIRCLE, DIAMOND }

    data class MermaidNode(
        val id: String,
        val label: String,
        val shape: Shape
    )

    data class MermaidEdge(
        val from: String,
        val to: String,
        val label: String?
    )

    /** Nodes grouped into layout ranks (top→bottom for TD, left→right for LR). */
    fun layers(): List<List<MermaidNode>> {
        if (nodes.isEmpty()) return emptyList()
        val outgoing = mutableMapOf<String, MutableList<String>>()
        val incomingCount = mutableMapOf<String, Int>()
        for (node in nodes) {
            outgoing.putIfAbsent(node.id, mutableListOf())
            incomingCount.putIfAbsent(node.id, 0)
        }
        for (edge in edges) {
            outgoing.getOrPut(edge.from) { mutableListOf() }.add(edge.to)
            incomingCount[edge.to] = (incomingCount[edge.to] ?: 0) + 1
            incomingCount.putIfAbsent(edge.from, incomingCount[edge.from] ?: 0)
        }
        val depth = mutableMapOf<String, Int>()
        fun visit(id: String, d: Int) {
            val current = depth[id] ?: -1
            if (d <= current) return
            depth[id] = d
            for (next in outgoing[id].orEmpty()) visit(next, d + 1)
        }
        val roots = nodes.map { it.id }.filter { (incomingCount[it] ?: 0) == 0 }
        val start = roots.ifEmpty { listOf(nodes.first().id) }
        for (id in start) visit(id, 0)
        for (node in nodes) depth.putIfAbsent(node.id, 0)
        val max = depth.values.maxOrNull() ?: 0
        val byId = nodes.associateBy { it.id }
        return (0..max).map { rank ->
            depth.filter { it.value == rank }.keys.mapNotNull { byId[it] }
        }.filter { it.isNotEmpty() }
    }

    companion object {
        fun parse(source: String): MermaidGraph? {
            val lines = source.split('\n').map { it.trim() }.filter { line ->
                line.isNotEmpty() && !line.startsWith("%%")
            }
            if (lines.isEmpty()) return null
            val header = HEADER.find(lines.first()) ?: return null
            val direction = when (header.groupValues[2].uppercase()) {
                "LR", "RL" -> Direction.LR
                else -> Direction.TD
            }
            val nodes = linkedMapOf<String, MermaidNode>()
            val edges = mutableListOf<MermaidEdge>()

            fun remember(id: String, label: String?, shape: Shape?) {
                val existing = nodes[id]
                val resolvedLabel = label?.takeIf { it.isNotEmpty() } ?: existing?.label ?: id
                val resolvedShape = shape ?: existing?.shape ?: Shape.RECT
                nodes[id] = MermaidNode(id, resolvedLabel, resolvedShape)
            }

            for (line in lines.drop(1)) {
                val edgeMatch = EDGE.find(line)
                if (edgeMatch != null) {
                    val left = parseNodeToken(edgeMatch.groupValues[1])
                    val right = parseNodeToken(edgeMatch.groupValues[3])
                    val label = edgeMatch.groupValues[2].takeIf { it.isNotEmpty() }
                    remember(left.id, left.label, left.shape)
                    remember(right.id, right.label, right.shape)
                    edges += MermaidEdge(left.id, right.id, label)
                    continue
                }
                val nodeMatch = NODE_TOKEN.matchEntire(line)
                if (nodeMatch != null) {
                    val node = parseNodeToken(line)
                    remember(node.id, node.label, node.shape)
                }
            }
            if (nodes.isEmpty()) return null
            return MermaidGraph(direction, nodes.values.toList(), edges)
        }

        private data class NodeToken(val id: String, val label: String?, val shape: Shape?)

        private fun parseNodeToken(token: String): NodeToken {
            val m = NODE_TOKEN.matchEntire(token.trim()) ?: return NodeToken(token.trim(), null, null)
            val id = m.groupValues[1]
            val rect = m.groupValues[2]
            val diamond = m.groupValues[3]
            val circle = m.groupValues[4]
            val round = m.groupValues[5]
            return when {
                rect.isNotEmpty() -> NodeToken(id, rect, Shape.RECT)
                diamond.isNotEmpty() -> NodeToken(id, diamond, Shape.DIAMOND)
                circle.isNotEmpty() -> NodeToken(id, circle, Shape.CIRCLE)
                round.isNotEmpty() -> NodeToken(id, round, Shape.ROUND)
                else -> NodeToken(id, null, null)
            }
        }

        private val HEADER = Regex(
            """^(graph|flowchart)\s+(TD|TB|BT|DT|LR|RL)\b""",
            RegexOption.IGNORE_CASE
        )

        private const val NODE = """[A-Za-z][\w]*(?:\[[^\]]*]|\{[^}]*}|\(\([^)]*\)\)|\([^)]*\))?"""

        private val NODE_TOKEN = Regex(
            """^([A-Za-z][\w]*)(?:\[([^\]]*)]|\{([^}]*)}|\(\(([^)]*)\)\)|\(([^)]*)\))?$"""
        )

        private val EDGE = Regex(
            """^($NODE)\s*(?:-->|---|-\.->|==>|-->>)\s*(?:\|([^|]*)\|)?\s*($NODE)$"""
        )
    }
}
