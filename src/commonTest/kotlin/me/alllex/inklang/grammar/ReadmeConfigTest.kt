package me.alllex.inklang.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadmeConfigTest {

    @Test
    fun readmeConfigCanBeParsedAndEvaluated() {
        assertEquals(
            expected = Config(
                server = Server(
                    port = 8080,
                    auth = ServerAuth(
                        token = Token(
                            refresh = "<token>"
                        )
                    )
                ),
                testerIds = listOf(1001, 1002),
                users = listOf(
                    User(
                        name = "Alex",
                        age = 42
                    ),
                    User(
                        name = "Bob",
                        age = 6033
                    )
                )
            ),
            actual = readDocument<Config>(configText)
        )
    }
}
