package ru.hse.spb

import java.io.OutputStream
import java.io.OutputStreamWriter

enum class AlignmentOption(val option: String) {
    FLUSHLEFT("flushleft"),
    CENTER("center"),
    FLUSHRIGHT("flushright")
}

interface Element {
    fun toOutputStream(outputStream: OutputStream)
    fun toWriter(writer: OutputStreamWriter, indent: String)
}

abstract class Command(val name: String) : Element {
    val indentStep = "    "
    val children = arrayListOf<Element>()
    val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        children.add(tag)
        tag.init()
//        println(tag.javaClass)
//        println(children)
        return tag
    }

    override fun toOutputStream(outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        toWriter(writer, "")
        writer.close()
    }
}

class Latex : Command("Latex") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        for (child in children) {
            child.toWriter(writer, indent)
        }
    }

    fun documentClass(className: String): DocumentClass {
        val documentClassObject = DocumentClass("beamer")
        children.add(documentClassObject)
        return documentClassObject
    }

    fun usepackage(packageName: String, vararg packageOptions: String) = initTag(Usepackage(packageName, *packageOptions), {})

    fun document(init: Document.() -> Unit): Document = initTag(Document(), init)
}

class Document : Command("Document") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("$indent\\begin{document}\n")
        for (child in children) {
            child.toWriter(writer, indentStep + indent)
        }
        writer.write("$indent\\end{document}\n")
    }

    fun frame(frameTitle: String, vararg arguments: String, init: Frame.() -> Unit) = initTag(Frame(frameTitle, *arguments), init)

    fun itemize(vararg arguments: String, init: Itemize.() -> Unit) = initTag(Itemize(*arguments), init)

    fun item(init: Item.() -> Unit) = initTag(Item(), init)

    fun customTag(name: String, vararg arguments: String, init: CustomTag.() -> Unit) = initTag(CustomTag(name, *arguments), init)

    fun enumerate(init: Enumerate.() -> Unit) = initTag(Enumerate(), init)

    fun math(init: Math.() -> Unit) = initTag(Math(), init)

    fun alignment(alignmentOption: AlignmentOption, init: Alignment.() -> Unit) = initTag(Alignment(alignmentOption), init)
}

class DocumentClass(val className: String) : Command("documentClass") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\documentclass{$className}\n")
    }
}

class Usepackage(val packageName: String, vararg val packageOptions: String) : Command("usepackage") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\usepackage")
        if (!packageOptions.isEmpty()) {
            writer.write("[${packageOptions.joinToString(", ")}]")
        }
        writer.write("{$packageName}\n")
    }
}

class Frame(val frameTitle: String, vararg val arguments: String) : Command("Frame") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\begin{frame}")
        if (!arguments.isEmpty()) {
            writer.write("[${arguments.joinToString(", ")}]\n")
        }
        writer.write("${indent + indentStep}\\frametitle{$frameTitle}\n")
        for (child in children) {
            child.toWriter(writer, indentStep + indent)
        }
        writer.write("${indent}\\end{frame}\n")
    }
}

class Itemize(vararg arguments: String) : Command("itemize") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\begin{itemize}\n")
        for (child in children) {
            child.toWriter(writer, indentStep + indent)
        }
        writer.write("${indent}\\end{itemize}\n")
    }
}

class Item : Command("item") {
    var text = ""

    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\item $text\n")
    }

    operator fun String.unaryPlus() {
        text = this
    }
}

class CustomTag(val tagName: String, vararg val arguments: String) : Command("customTag") {
    var text = ""

    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\begin{$tagName}")
        if (!arguments.isEmpty()) {
            writer.write("[${arguments.joinToString(", ")}]\n")
        }
        writer.write("${text}\n")
        writer.write("${indent}\\end{$tagName}\n")
    }

    operator fun String.unaryPlus() {
        text = this
    }
}

class Enumerate(vararg arguments: String) : Command("enumerate") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\begin{enumerate}\n")
        for (child in children) {
            child.toWriter(writer, indentStep + indent)
        }
        writer.write("${indent}\\end{enumerate}\n")
    }
}

class Math : Command("enumerate") {
    var text = ""
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\begin{math}\n")
        writer.write(text.replace("\n", "\n${indentStep + indent}"))
        writer.write("${indent}\\end{math}\n")
    }

    operator fun String.unaryPlus() {
        text = this
    }
}

class Alignment(val alignmentOption: AlignmentOption) : Command("enumerate") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("${indent}\\begin{$alignmentOption}\n")
        for (child in children) {
            child.toWriter(writer, indentStep + indent)
        }
        writer.write("${indent}\\end{$alignmentOption}\n")
    }
}

fun latex(init: Latex.() -> Unit): Latex {
    val latex = Latex()
    latex.init()
    return latex
}