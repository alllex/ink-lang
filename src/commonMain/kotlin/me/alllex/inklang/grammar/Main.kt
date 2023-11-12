package me.alllex.inklang.grammar

import kotlinx.serialization.json.*


inline fun <reified T : Any> readDocument(s: String): T {
    val jsonObject = evalInkToJson(s)
    val result = Json.decodeFromJsonElement<T>(jsonObject)
    return result
}

fun evalInkToJson(s: String): JsonObject {
    val parsed = InkParser().parseDoc(s)
    return parsed.toJson()
}

data class NestedId(
    val ids: List<String>,
) {
    constructor(vararg ids: String) : this(ids.toList())

    fun isEmpty() = ids.isEmpty()

    fun chip(): Pair<String, NestedId> {
        val head = ids.first()
        val tail = NestedId(ids.drop(1))
        return head to tail
    }
}

typealias FlatProps = Map<NestedId, JsonElement>

private fun InkDoc.toJson(): JsonObject {
    val flatProps = toFlatProps()
    return flatProps.treeify()
}

private fun FlatProps.treeify(): JsonObject {
    var result = JsonObject(emptyMap())
    for ((nestedId, value) in this) {
        val newResult = result.deepMerge(nestedId, value)
        if (newResult is JsonObject) {
            result = newResult
        } else {
            error("Expected object, but got $newResult")
        }
    }
    return result
}

private fun JsonObject.deepMerge(nestedId: NestedId, value: JsonElement): JsonElement {
    if (nestedId.isEmpty()) {
        return value
    }

    val (head, tail) = nestedId.chip()

    val curHeadObject = when (val curHeadValue = this[head]) {
        null -> JsonObject(emptyMap())
        is JsonObject -> curHeadValue
        else -> error("Expected object, but got $curHeadValue")
    }

    val newHeadObject = curHeadObject.deepMerge(tail, value)

    return JsonObject(this + (head to newHeadObject))
}

private fun InkDoc.toFlatProps(): FlatProps {
    return block.toFlatProps(NestedId())
}

private fun getPropEffects(parentId: NestedId, stmt: InkStmt): FlatProps = when (stmt) {
    is InkLet -> stmt.toFlatProps(parentId)
    is InkScope -> stmt.toFlatProps(parentId)
}

private fun InkScope.toFlatProps(parentId: NestedId): FlatProps {
    val newParentId = NestedId(parentId.ids + receiver.ids)
    return block.toFlatProps(newParentId)
}

private fun InkBlock.toFlatProps(parentId: NestedId): FlatProps {
    val props = mutableMapOf<NestedId, JsonElement>()
    for (stmt in stmts) {
        val propEffects = getPropEffects(parentId, stmt)
        props += propEffects
    }
    return props
}

private fun InkLet.toFlatProps(parentId: NestedId): FlatProps {
    val deepPropId = NestedId(parentId.ids + receiver.ids)
    val expr = expr
    val value = expr.toJson()
    return mapOf(deepPropId to value)
}

private fun InkExpr.toJson(): JsonElement = when (this) {
    is InkBoolean -> JsonPrimitive(value)
    is InkInt -> JsonPrimitive(value)
    is InkReal -> JsonPrimitive(value)
    is InkString -> JsonPrimitive(value)
    is InkCall -> {
        val receiver = receiver
        val callee = receiver.ids.single()
        when (callee) {
            "listOf" -> {
                val convertedArgs = args.map { it.toJson() }
                JsonArray(convertedArgs)
            }

            else -> error("Unknown callee: $callee")
        }
    }

    is InkBlock -> {
        val flatProps = toFlatProps(NestedId())
        flatProps.treeify()
    }
}
