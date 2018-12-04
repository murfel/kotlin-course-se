package ru.hse.spb

import java.io.OutputStream
import java.io.OutputStreamWriter

@DslMarker
annotation class LatexElementMarker

enum class AlignmentOption {
    FlushLeft,
    Center,
    FlushRight;

    override fun toString(): String {
        return name.toLowerCase()
    }
}

@LatexElementMarker
abstract class Element {
    val indentStep = "    "
    fun toOutputStream(outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        toWriter(writer, "")
        writer.close()
    }

    abstract fun toWriter(writer: OutputStreamWriter, indent: String)
}

class TextElement(private val text: String) : Element() {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("$text\n")
    }
}

abstract class ElementWithChildren : Element() {
    val children = arrayListOf<Element>()
    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        children.add(tag)
        tag.init()
        return tag
    }
}

abstract class ElementWithNameAndTextChildren(val name: String) : ElementWithChildren() {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

/**
 * Item is a switch command in Latex.
 *
 * This library doesn't implement a general representation for a switch command for simplicity.
 *
 * The item switch command can have text children. This behaviour doesn't exactly mimic the Latex syntax but it is
 * adapted for simplicity.
 */
class Item(private val bullet: String? = null) : ElementWithNameAndTextChildren("item") {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("$indent\\$name")
        if (bullet != null) {
            writer.write("[$bullet]")
        }
        writer.write(" ")
        for (child in children) {
            child.toWriter(writer, "")
        }
    }
}

/**
 * A command is a Latex command in the form of:
 * \commandname[option1,option2,...]{argument}
 *
 * In Latex, commands can have multiple arguments in the form of {argument1}{argument2}...
 * but this feature is ignored for simplicity.
 *
 * A command cannot have children commands.
 */
abstract class Command(name: String, private val argument: String? = null, private vararg val options: String) :
        ElementWithNameAndTextChildren(name) {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("$indent\\$name")
        if (options.isNotEmpty()) {
            writer.write("[${options.joinToString(", ")}]")
        }
        if (argument != null) {
            writer.write("{$argument}\n")
        }
    }
}

/**
 * An environment is a Latex command in the form of:
 * \begin{environmentName}[option1,option2, ...]{argument}
 *      ...
 * \end{environmentName}
 *
 * In Latex, environments can have multiple arguments in the form of {argumnet1}{argumnet2}...
 * but this feature is ignored for simplicity.
 *
 * The ... can be replaced by child commands out of frame, math, alignment, customEnvironment, itemize, enumerate.
 */
abstract class Environment(name: String, private val argument: String? = null, vararg val options: String) :
        ElementWithNameAndTextChildren(name) {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        writer.write("$indent\\begin{$name}")
        if (options.isNotEmpty()) {
            writer.write("[${options.joinToString(", ")}]")
        }
        if (argument != null) {
            writer.write("{$argument}")
        }
        writer.write("\n")
        for (child in children) {
            child.toWriter(writer, indent + indentStep)
        }
        writer.write("$indent\\end{$name}\n")
    }

    fun frame(frameTitle: String? = null, vararg options: String, init: Frame.() -> Unit) =
            initTag(Frame(frameTitle, *options), init)

    fun math(init: Math.() -> Unit) = initTag(Math(), init)
    fun alignment(alignmentOption: AlignmentOption, init: Alignment.() -> Unit) =
            initTag(Alignment(alignmentOption), init)

    fun customEnvironment(customEnvironmentName: String, argument: String? = null, vararg options: String,
                          init: CustomEnvironment.() -> Unit) =
            initTag(CustomEnvironment(customEnvironmentName, argument, *options), init)

    fun itemize(vararg options: String, init: Itemize.() -> Unit) = initTag(Itemize(*options), init)
    fun enumerate(vararg options: String, init: Enumerate.() -> Unit) = initTag(Enumerate(*options), init)
}

/**
 * An environment with items can contain the \item command as a child.
 *
 * Enumerate, and Itemize are examples of such environments.
 */
abstract class EnvironmentWithItems(name: String, vararg options: String) : Environment(name, options = *options) {
    fun item(init: Item.() -> Unit) = initTag(Item(), init)
}

/**
 * An artificial command to indicate the root of a Latex DSL when writing in Kotlin.
 */
class Latex : ElementWithChildren() {
    override fun toWriter(writer: OutputStreamWriter, indent: String) {
        for (child in children) {
            child.toWriter(writer, indent)
        }
    }

    fun document(init: Document.() -> Unit) = initTag(Document(), init)
    fun documentClass(documentClassName: String, vararg options: String) =
            initTag(DocumentClass(documentClassName, *options)) {}

    fun usepackage(packageName: String, vararg options: String) = initTag(UsePackage(packageName, *options)) {}
}

class Document : Environment("document")

class DocumentClass(documentClassName: String, vararg options: String) :
        Command("documentclass", documentClassName, *options)

class UsePackage(packageName: String, vararg options: String) : Command("usepackage", packageName, *options)

class Frame(frameTitle: String? = null, vararg options: String) : Environment("frame", frameTitle, *options)

class Math : Environment("math")

class Alignment(alignmentOption: AlignmentOption) : Environment(alignmentOption.toString())

class Itemize(vararg options: String) : EnvironmentWithItems("itemize", *options)

class Enumerate(vararg options: String) : EnvironmentWithItems("enumerate", *options)

class CustomEnvironment(customEnvironmentName: String, argument: String? = null, vararg options: String) :
        Environment(customEnvironmentName, argument, *options)

fun latex(init: Latex.() -> Unit): Latex {
    return Latex().apply(init)
}

/**
 * The to infix operator should be used in options for commands or environments.
 *
 * E.g. "option1" to "option2" translates to the string "option1=option2".
 */
infix fun String.to(rightOperand: String): String {
    return "$this=$rightOperand"
}