package me.alllex.inklang.grammar

import kotlinx.serialization.Serializable

fun main() {
//    demo()
}

@Suppress("unused")
fun demo() {
    val text = readFile("config.ink")
    val config = readDocument<Config>(text)
    println(config)
}

@Suppress("UNUSED_PARAMETER")
private fun readFile(file: String): String {
    return configText
}

val configText = """
    // Flat properties style
    server.port = 80

    // Nested scoping style
    server {
        // Overrides the value above!
        port = 8080

        // Mixed style
        auth {
            token.refresh = "<token>"
        }
    }

    // Human readable list syntax
    testerIds = listOf(1001, 1002)

    // Builder style
    users = listOf({
        name = "Alex"
        age = 42
    }, {
        name = "Bob"
        age = 6033
    })
""".trimIndent()

@Serializable
data class Config(
    val server: Server,
    val testerIds: List<Int>,
    val users: List<User>,
)

@Serializable
data class Server(val port: Int, val auth: ServerAuth)

@Serializable
data class ServerAuth(val token: Token)

@Serializable
data class Token(val refresh: String)

@Serializable
data class User(val name: String, val age: Int)
