package me.alllex.inklang.grammar

import kotlin.jvm.JvmInline


data class InkDoc(
    val block: InkBlock,
) {
    constructor(stmts: List<InkStmt>) : this(InkBlock(stmts))
}

sealed interface InkStmt

data class InkBlock(
    val stmts: List<InkStmt>,
) : InkExpr {
    constructor(vararg stmts: InkStmt) : this(stmts.toList())
}

@JvmInline
value class Receiver(
    val ids: List<String>,
) {
    constructor(id: String) : this(listOf(id))
    constructor(vararg ids: String) : this(ids.toList())
}

sealed interface InkExpr

sealed interface InkValue : InkExpr

@JvmInline
value class InkString(val value: String) : InkValue

@JvmInline
value class InkInt(val value: Long) : InkValue

@JvmInline
value class InkReal(val value: Double) : InkValue

data class InkCall(
    val receiver: Receiver,
    val args: List<InkExpr>,
) : InkExpr {
    constructor(id: String, vararg args: InkExpr) : this(Receiver(id), args.toList())
}

data class InkLet(
    val receiver: Receiver,
    val expr: InkExpr,
) : InkStmt {
    constructor(id: String, value: String) : this(Receiver(id), InkString(value))
    constructor(id: String, value: Int) : this(Receiver(id), InkInt(value.toLong()))
    constructor(id: String, value: Double) : this(Receiver(id), InkReal(value))
    constructor(id: String, value: InkExpr) : this(Receiver(id), value)
}

data class InkScope(
    val receiver: Receiver,
    val block: InkBlock,
) : InkStmt {
    constructor(id: String, block: InkBlock) : this(Receiver(id), block)
    constructor(receiver: Receiver, stmt: InkStmt) : this(receiver, InkBlock(stmt))
}
