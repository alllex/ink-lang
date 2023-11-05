package me.alllex.inklang.grammar

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

@Suppress("MemberVisibilityCanBePrivate")
class InkGrammar : Grammar<InkDoc>(debugMode = true) {

    @Suppress("unused")
    val ws by regexToken("\\s+", ignored = true)

    @Suppress("unused")
    val slc by regexToken("//[^\\r\\n]*([\\r\\n]|$)", ignored = true)
    val nl by regexToken("[\\r\\n]+")

    val eq by literalToken("=")
    val cm by literalToken(",")
    val ob by literalToken("{")
    val cb by literalToken("}")
    val op by literalToken("(")
    val cp by literalToken(")")
    val dot by literalToken(".")

    val id by regexToken("[a-zA-Z_][a-zA-Z0-9_]*") map { it.text }

    val receiver by separated(id, -dot, allowEmpty = false) map { Receiver(it) }

    val string by regexToken("\"[^\"]*\"") map { InkString(it.text.substring(1, it.text.lastIndex)) }
    val int by regexToken("[-+]?[0-9]+") map { InkInt(it.text.toLong()) }
    val real by regexToken("[-+]?[0-9]+\\.[0-9]+") map { InkReal(it.text.toDouble()) }

    val value by string or real or int

    val arg by ref(::expr)

    val args by separated(arg, -cm, allowEmpty = true, trailingSeparator = true)

    val call by receiver * -op * args * -cp map { (receiver, args) ->
        InkCall(receiver, args)
    }

    val block by -ob * ref(::stmts) * -cb map { InkBlock(it) }

    val expr: Parser<InkExpr> by value or call or ref(::block)

    val let by receiver * -eq * expr map { (id, v) -> InkLet(id, v) }

    val scope by receiver * block map { (id, block) ->
        InkScope(id, block)
    }

    val stmt: Parser<InkStmt> by let or scope

    // TODO: how to make errors better, when trailing separator was present, but not expected? Currently, we backtrack and fail with "unmatched token"
    val stmts by separated(stmt, nl, trailingSeparator = true)

    val doc by stmts map { InkDoc(InkBlock(it)) }

    override val root by doc
}
