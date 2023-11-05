# Ink

Ink is a configuration language that is a subset of Kotlin

```kotlin
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
```

## Configuration

The primary use-case of Ink is configuration files.

The above example document can be interpreted as the following config:

```kotlin

fun main() {
    val text = readFile("config.ink")
    val config = readDocument<Config>(text)
    println(config)
}

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
```

The Ink document is evaluated into JSON value, taking nesting and overrides into account.
Then `kotlinx.serialization` is used to convert the resulting JSON into a Kotlin object of a given type.

## License

Distributed under the MIT License. See `LICENSE` for more information.

