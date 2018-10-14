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
                    customEnvironment("pyglist", null, "language", "kotlin") {
                        +"""
                   |val a = 1
                   |
                   """.trimIndent()
                    }
                }
            }
        }
        val expected =
                """
\documentclass{beamer}
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

""".trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `test all possible commands`() {
        val expected = """
            \documentclass[12pt]{beamer}
            \usepackage[opt1, opt2, opt3=opt4]{amsmath}
            \usepackage{amssymb}
            \documentclass{article}
            \begin{document}
                \begin{frame}[50pt]{My Frame Title}
                    \begin{math}
            a^2 + b^2 = c^2 + d^2
            a \ne c, a \ne d
                    \end{math}
                \end{frame}
                \begin{flushright}
                    \begin{enumerate}[label={\alph*)}, font={\color{red!50!black}\bfseries}]
                        \item item A
                        \item item B
                        \item item C
                        \begin{itemize}[--]
                            \item one item
                            \item another item
                            \begin{itemize}
                                \item more items
            and other things
                            \end{itemize}
                        \end{itemize}
                    \end{enumerate}
                    \begin{center}
                        \begin{customEnvironment}[opt1]{arg}
                            \begin{oneMoreCustomEnvironment}[opt1]
                                \begin{math}
                                \end{math}
                            \end{oneMoreCustomEnvironment}
                        \end{customEnvironment}
                    \end{center}
                \end{flushright}
            \end{document}

            """.trimIndent()
        val actual = latex {
            documentClass("beamer", "12pt")
            usepackage("amsmath", "opt1", "opt2", "opt3" to "opt4")
            usepackage("amssymb")
            documentClass("article")
            document {
                frame("My Frame Title", "50pt") {
                    math {
                        +"a^2 + b^2 = c^2 + d^2"
                        +"a \\ne c, a \\ne d"
                    }
                }
                alignment(AlignmentOption.FlushRight) {
                    enumerate("label" to "{\\alph*)}", "font" to "{\\color{red!50!black}\\bfseries}") {
                        item { +"item A" }
                        item { +"item B" }
                        item { +"item C" }
                        itemize("--") {
                            item { +"one item" }
                            item { +"another item" }
                            itemize {
                                item { +"more items" }
                                +"and other things"
                            }
                        }
                    }
                    alignment(AlignmentOption.Center) {
                        customEnvironment("customEnvironment", "arg", "opt1") {
                            customEnvironment("oneMoreCustomEnvironment", options = *arrayOf("opt1")) {
                                math { }
                            }
                        }
                    }
                }
            }
        }
        assertEquals(expected, actual)
    }
}