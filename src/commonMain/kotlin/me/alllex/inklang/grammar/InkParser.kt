package me.alllex.inklang.grammar

class InkParser {

    private val grammar = InkGrammar()

    fun parseDoc(s: String): InkDoc {
        return grammar.parseOrThrow(s)
    }

}
