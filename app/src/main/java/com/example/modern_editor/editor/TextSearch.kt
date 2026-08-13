package com.example.modern_editor.editor

fun findMatches(text: String, query: String): List<IntRange> {
    if (query.isEmpty()) return emptyList()
    val matches = mutableListOf<IntRange>()
    var index = text.indexOf(query, ignoreCase = true)
    while (index >= 0) {
        matches.add(index until index + query.length)
        index = text.indexOf(query, index + 1, ignoreCase = true)
    }
    return matches
}
