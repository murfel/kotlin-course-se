package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

class DocumentTest {
    fun assertEquals(expected: String, actual: Latex) {
        val outputStream = ByteArrayOutputStream()
        actual.toOutputStream(outputStream)
        assertEquals(expected, outputStream.toString())
    }

    @Test
    fun empty() {
        assertEquals("", latex { })
    }

    @Test
    fun emptyDocument() {
        assertEquals("""
            \begin{document}
            \end{document}

            """.trimIndent(), latex { document { } })
    }

    @Test
    fun usepackage() {
        assertEquals("""
            \usepackage[arg1, arg2=arg3]{beamer}

        """.trimIndent(), latex { usepackage("beamer", "arg1", "arg2" to "arg3") })
    }

    @Test
    fun documentAndFrame() {
        assertEquals("""
            \begin{document}
                \begin{frame}
                \end{frame}
            \end{document}

        """.trimIndent(), latex { document { frame {} } })
    }

    @Test
    fun documentAndText() {
        assertEquals("""
            \begin{document}
            meow
            \end{document}

        """.trimIndent(), latex { document { +"meow" } })
    }

    @Test
    fun sample() {
        val actual = latex {
            documentClass("beamer")
            usepackage("babel", "russian")
            document {
                frame("frametitle", "arg1" to "arg2", "arg3") {
                    itemize {
                        item { +"my text" }
                        item { +"more of my text" }
                    }

                    // begin{pyglist}[language=kotlin]...\end{pyglist}
                    customTag("pyglist", null, "language", "kotlin") {
                        +"""
                   |val a = 1
                   |
                   """.trimIndent()
                    }
                }
            }
        }
        val expected =
                """\documentclass{beamer}
\usepackage[russian]{babel}
\begin{document}
    \begin{frame}[arg1=arg2, arg3]{frametitle}
        \begin{itemize}
            \item my text
            \item more of my text
        \end{itemize}
        \begin{pyglist}[language, kotlin]
|val a = 1
|
        \end{pyglist}
    \end{frame}
\end{document}
"""
        assertEquals(expected, actual)
    }
}