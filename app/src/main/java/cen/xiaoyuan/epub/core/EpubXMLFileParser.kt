package cen.xiaoyuan.epub.core

import android.graphics.BitmapFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.io.File
import java.util.zip.ZipEntry

class EpubXMLFileParser(
    fileAbsolutePath: String,
    val data: ByteArray,
    private val zipFile: Map<String, Pair<ZipEntry, ByteArray>>
) {
    data class Output(val title: String?, val body: String)

    private val fileParentFolder: File = File(fileAbsolutePath).parentFile ?: File("")

    // Make all local references absolute to the root of the epub for consistent references
    private val absBasePath: String = File("").canonicalPath

    fun parse(): Output {
        val body = Jsoup.parse(data.inputStream(), "UTF-8", "").body()

        val title = body.selectFirst("h1, h2, h3, h4, h5, h6")?.text()?.empty(null)
        body.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()
        return Output(
            title = title,
            body = getNodeStructuredText(body)
        )
    }

    fun parsePlus(ids:List<String>): Output {
        val body = Jsoup.parse(data.inputStream(), "UTF-8", "").body()

        val title = body.selectFirst("h1, h2, h3, h4, h5, h6")?.text()?.empty(null)
        body.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()
        return Output(
            title = title,
            body = getNodeStructuredTextFilter(body,ids)
        )
    }

    private fun String.empty(target:String?):String?{
        val result = trim {
            it==' ' || it=='\n' || it=='\r'
        }
        return result.ifEmpty { target }
    }

    // Rewrites the image node to xml for the next stage.
    private fun declareImgEntry(node: org.jsoup.nodes.Node): String {
        val relPathEncoded = (node as? Element)?.attr("src") ?: return ""
        val absPath = File(fileParentFolder, relPathEncoded.decodedURL).canonicalPath
            .removePrefix(absBasePath)
            .replace("""\""", "/")
            .removePrefix("/")

        // Use run catching so it can be run locally without crash
        val bitmap = zipFile[absPath]?.second?.runCatching {
            BitmapFactory.decodeByteArray(this, 0, this.size)
        }?.getOrNull()

        val text = BookTextMapper.ImgEntry(
            path = absPath,
            yrel = bitmap?.let { it.height.toFloat() / it.width.toFloat() } ?: 1.45f
        ).toXMLString()

        return "\n\n$text\n\n"
    }

    private val headTags = arrayOf("h1","h2","h3","h4","h5","h6")

    private fun getPTraverse(node: org.jsoup.nodes.Node): String {
        fun innerTraverse(node: org.jsoup.nodes.Node): String =
            node.childNodes().joinToString("") { child ->
                when {
                    child.nodeName() == "br" -> "\n"
                    child.nodeName() == "img" -> declareImgEntry(child)
                    child is TextNode -> child.text()
                    else -> innerTraverse(child)
                }
            }

        val paragraph = innerTraverse(node).trim()
        return if (paragraph.isEmpty()) "" else innerTraverse(node).trim() + "\n\n"
    }

    private fun getNodeTextTraverse(node: org.jsoup.nodes.Node): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""

        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                child is TextNode -> {
                    val text = child.text().trim()
                    if (text.isEmpty()) "" else text + "\n\n"
                }
                else -> getNodeTextTraverse(child)
            }
        }
    }

    private fun getNodeTextTraverse(node: org.jsoup.nodes.Node,ids:List<String>): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""

        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                child is TextNode -> {
                    val text = child.text().trim()
                    if (text.isEmpty()) "" else text + "\n\n"
                }
                else -> getNodeTextTraverse(child,ids)
            }
        }
    }

    private fun getNodeStructuredText(node: org.jsoup.nodes.Node): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""

        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                child.nodeName() in headTags && child.hasAttr("id") -> "\n\n[${(child as Element).text()}#id=${child.attr("id")}]\n\n"
                child is TextNode -> child.text().trim()
                else -> getNodeTextTraverse(child)
            }
        }
    }

    private fun getNodeStructuredTextFilter(node: org.jsoup.nodes.Node,ids:List<String>): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""

        return children.joinToString("") { child ->
            when {
                child.hasAttr("id") && child.attr("id") in ids -> "[${(child as Element).text()}#id=${child.attr("id")}]\n\n"
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                //child.nodeName() in headTags && child.hasAttr("id") -> "\n\n[${(child as Element).text()}#id=${child.attr("id")}]\n\n"
                child is TextNode -> child.text().trim()
                else -> getNodeTextTraverse(child,ids)
            }
        }
    }
}