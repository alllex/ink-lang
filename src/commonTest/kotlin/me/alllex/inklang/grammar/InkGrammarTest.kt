package me.alllex.inklang.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

class InkGrammarTest {

    @Test
    fun singleLet() {
        val parsed = InkParser().parseDoc("hey_there_1 = \"string value\"")
        val expected = InkDoc(listOf(InkLet("hey_there_1", "string value")))
        assertEquals(expected, parsed)
    }

    @Test
    fun letWithDeepReceiver() {
        val parsed = InkParser().parseDoc("a.b.c.d = \"deep\"")
        val expected = InkDoc(listOf(InkLet(Receiver("a", "b", "c", "d"), InkString("deep"))))
        assertEquals(expected, parsed)
    }

    @Test
    fun multipleLets() {
        val parsed = InkParser().parseDoc("a = \"b\"\nc = \"d\"")
        val expected = InkDoc(listOf(InkLet("a", "b"), InkLet("c", "d")))
        assertEquals(expected, parsed)
    }

    @Test
    fun singleScope() {
        val parsed = InkParser().parseDoc(
            """
                a {
                    b = "c"
                }
            """.trimIndent()
        )
        val expected = InkDoc(listOf(InkScope("a", InkBlock(InkLet("b", "c")))))
        assertEquals(expected, parsed)
    }

    @Test
    fun deepScope() {
        val parsed = InkParser().parseDoc(
            """
                a.b.c {
                    x = "y"
                }
            """.trimIndent()
        )
        val expected = InkDoc(listOf(InkScope(Receiver("a", "b", "c"), InkBlock(InkLet("x", "y")))))
        assertEquals(expected, parsed)
    }

    @Test
    fun multipleScopes() {
        val parsed = InkParser().parseDoc(
            """
                a {
                    b = "c"
                }

                d {
                    e = "f"
                }
            """.trimIndent()
        )
        val expected = InkDoc(
            listOf(
                InkScope("a", InkBlock(InkLet("b", "c"))),
                InkScope("d", InkBlock(InkLet("e", "f")))
            )
        )
        assertEquals(expected, parsed)
    }

    @Test
    fun nestedScope() {
        val parsed = InkParser().parseDoc(
            """
                a {
                    b.c {
                        x = "y"
                    }
                }
            """.trimIndent()
        )
        val expected = InkDoc(
            listOf(
                InkScope(Receiver("a"), InkBlock(InkScope(Receiver("b", "c"), InkLet("x", "y"))))
            )
        )
        assertEquals(expected, parsed)
    }

    @Test
    fun letInt() {
        val parsed = InkParser().parseDoc("a = 42")
        val expected = InkDoc(listOf(InkLet("a", 42)))
        assertEquals(expected, parsed)
    }

    @Test
    fun letDouble() {
        val parsed = InkParser().parseDoc("a = -42.42")
        val expected = InkDoc(listOf(InkLet("a", -42.42)))
        assertEquals(expected, parsed)
    }

    @Test
    fun letBooleanTrue() {
        val parsed = InkParser().parseDoc("a = true")
        val expected = InkDoc(listOf(InkLet("a", true)))
        assertEquals(expected, parsed)
    }

    @Test
    fun letBooleanFalse() {
        val parsed = InkParser().parseDoc("a = false")
        val expected = InkDoc(listOf(InkLet("a", false)))
        assertEquals(expected, parsed)
    }

    @Test
    fun singleLetOnMultipleLines() {
        val parsed = InkParser().parseDoc(
            """
                a
                    =
                        42
            """.trimIndent()
        )
        val expected = InkDoc(listOf(InkLet("a", 42)))
        assertEquals(expected, parsed)
    }

    @Test
    fun singleLineComment() {
        val parsed = InkParser().parseDoc(
            """
                // comment
                a = 42
            """.trimIndent()
        )
        val expected = InkDoc(listOf(InkLet("a", 42)))
        assertEquals(expected, parsed)
    }

    @Test
    fun commentsInBlock() {
        val parsed = InkParser().parseDoc(
            """
                // long comment
                a // comment!
                {
                    // comment
                    b = "c"
                    // comment
                    d // comment
                        = // comment
                        "e" // comment
                    // e = "f"
                } // comment
                // comment
            """.trimIndent()
        )
        val expected = InkDoc(
            listOf(
                InkScope(
                    "a", InkBlock(
                        InkLet("b", "c"),
                        InkLet("d", "e")
                    )
                )
            )
        )
        assertEquals(expected, parsed)
    }

    @Test
    fun listOfNumbers() {
        val parsed = InkParser().parseDoc(
            """
                a = listOf(1, 2, 3)
            """.trimIndent()
        )
        val expected = InkDoc(listOf(InkLet("a", InkCall("listOf", InkInt(1), InkInt(2), InkInt(3)))))
        assertEquals(expected, parsed)
    }

    @Test
    fun listOfNumbersTrailingComma() {
        val parsed = InkParser().parseDoc(
            """
                a = listOf(1, 2, 3, )
            """.trimIndent()
        )
        val expected = InkDoc(listOf(InkLet("a", InkCall("listOf", InkInt(1), InkInt(2), InkInt(3)))))
        assertEquals(expected, parsed)
    }

}
