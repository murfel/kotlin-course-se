package ru.hse.spb

import org.junit.Test
import java.io.ByteArrayOutputStream

class DocumentTest {

    @Test
    fun sample() {
        val actual = ByteArrayOutputStream()
        latex {
            documentClass("beamer")
            usepackage("babel", "russian" /* varargs */)
            document {
                frame("frametitle", "arg1", "arg2") {
                    itemize {
                        item { +"my text" }
                        item { +"more of my text" }
                    }

                    // begin{pyglist}[language=kotlin]...\end{pyglist}
                    customTag("pyglist", "language", "kotlin") {
                        +"""
                   |val a = 1
                   |
                    """
                    }
                }
            }
        }.toOutputStream(actual)
        val expected =
                """\documentclass{beamer}
\usepackage[russian]{babel}
\begin{document}
    \begin{frame}
        \frametitle{frametitle}["arg1", "arg2"]
        \begin{itemize}
            \item my text
            \item more of my text
        \end{itemize}
        \begin{pyglist}["language", "kotlin"]

            |val a = 1
            |

        \end{pyglist}
    \end{frame}
\end{document}"""
//        println(expected)
//        println("=================================================================")
//        println(actual)
        assert(expected == actual.toString())
    }
}