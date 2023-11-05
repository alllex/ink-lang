package me.alllex.inklang.grammar

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ConfigTest {

    @Test
    fun configOfPrimitives() {
        assertEquals(
            expected = ConfigOfPrimitives(
                number = 42,
                string = "hello",
            ),
            actual = readDocument<ConfigOfPrimitives>(
                """
                    number = 42
                    string = "hello"
                """.trimIndent()
            )
        )
    }

    @Test
    fun configOfInDifferentOrder() {
        assertEquals(
            expected = ConfigOfPrimitives(
                number = 42,
                string = "hello",
            ),
            actual = readDocument<ConfigOfPrimitives>(
                """
                    string = "hello"
                    number = 42
                """.trimIndent()
            )
        )
    }

    @Test
    fun configOfPrimitivesWithWrongTypeValue() {
        val e = assertFailsWith<IllegalArgumentException>() {
            readDocument<ConfigOfPrimitives>(
                """
                number = "oops"
                string = "hello"
            """.trimIndent()
            )
        }

        assertEquals(
            expected = """
                Failed to parse literal as 'int' value
                JSON input: {"number":"oops","string":"hello"}
            """.trimIndent(),
            actual = e.message
        )
    }

    @Test
    fun configWithNullable() {
        assertEquals(
            expected = ConfigWithNullablePrimitive(
                required = "hello",
                maybe = 42,
            ),
            actual = readDocument<ConfigWithNullablePrimitive>(
                """
                    required = "hello"
                    maybe = 42
                """.trimIndent()
            )
        )

        assertEquals(
            expected = ConfigWithNullablePrimitive(
                required = "hello",
                maybe = null,
            ),
            actual = readDocument<ConfigWithNullablePrimitive>(
                """
                    required = "hello"
                """.trimIndent()
            )
        )
    }

    @Test
    fun compositeConfigWithNullableConfig() {
        assertEquals(
            expected = CompositeConfigWithNullableConfig(
                required = "hello",
                maybe = ConfigOfPrimitives(number = 42, string = "there"),
            ),
            actual = readDocument<CompositeConfigWithNullableConfig>(
                """
                    required = "hello"
                    maybe.number = 42
                    maybe.string = "there"
                """.trimIndent()
            )
        )

        assertEquals(
            expected = CompositeConfigWithNullableConfig(
                required = "hello",
                maybe = null
            ),
            actual = readDocument<CompositeConfigWithNullableConfig>(
                """
                    required = "hello"
                """.trimIndent()
            )
        )
    }

    @Test
    fun configWithList() {
        assertEquals(
            expected = ConfigWithList(
                list = listOf(1, 2, 3),
            ),
            actual = readDocument<ConfigWithList>(
                """
                    list = listOf(1, 2, 3)
                """.trimIndent()
            )
        )
    }

    @Test
    fun configWithListOfConfigs() {
        assertEquals(
            expected = ConfigWithListOfConfigs(
                list = listOf(ConfigWithNullablePrimitive(required = "a", maybe = 1), ConfigWithNullablePrimitive(required = "b")),
            ),
            actual = readDocument<ConfigWithListOfConfigs>(
                """
                    list = listOf({
                        required = "a"
                        maybe = 1
                    }, {
                        required = "b"
                    })
                """.trimIndent()
            )
        )
    }

    @Test
    fun compositeConfigFromFlatProps() {
        assertEquals(
            expected = CompositeConfig(
                one = ConfigOfPrimitives(
                    number = 0,
                    string = "hello",
                ),
                two = ConfigWithNullablePrimitive(
                    required = "there",
                    maybe = 1,
                ),
            ),
            actual = readDocument<CompositeConfig>(
                """
                    one.number = 0
                    one.string = "hello"
                    two.required = "there"
                    two.maybe = 1
                """.trimIndent()
            )
        )
    }

    @Test
    fun compositeConfigFromScopedProps() {
        assertEquals(
            expected = CompositeConfig(
                one = ConfigOfPrimitives(
                    number = 0,
                    string = "hello",
                ),
                two = ConfigWithNullablePrimitive(
                    required = "there",
                    maybe = 1,
                ),
            ),
            actual = readDocument<CompositeConfig>(
                """
                    one {
                        number = 0
                        string = "hello"
                    }
                    two {
                        required = "there"
                        maybe = 1
                    }
                """.trimIndent()
            )
        )
    }

    @Test
    fun compositeConfigWithBlockAssign() {
        assertEquals(
            expected = CompositeConfig(
                one = ConfigOfPrimitives(
                    number = 0,
                    string = "hello",
                ),
                two = ConfigWithNullablePrimitive(
                    required = "there",
                    maybe = 1,
                ),
            ),
            actual = readDocument<CompositeConfig>(
                """
                    one = {
                        number = 0
                        string = "hello"
                    }
                    two = {
                        required = "there"
                        maybe = 1
                    }
                """.trimIndent()
            )
        )
    }

    @Serializable
    data class ConfigOfPrimitives(
        val number: Int,
        val string: String,
    )

    @Serializable
    data class ConfigWithNullablePrimitive(
        val required: String,
        val maybe: Int? = null,
    )

    @Serializable
    data class ConfigWithList(
        val list: List<Int>,
    )

    @Serializable
    data class ConfigWithListOfConfigs(
        val list: List<ConfigWithNullablePrimitive>,
    )

    @Serializable
    data class CompositeConfig(
        val one: ConfigOfPrimitives,
        val two: ConfigWithNullablePrimitive,
    )

    @Serializable
    data class CompositeConfigWithNullableConfig(
        val required: String,
        val maybe: ConfigOfPrimitives? = null,
    )
}
