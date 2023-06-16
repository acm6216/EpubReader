package cen.xiaoyuan.epub.core

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

private val NodeList.elements get() = (0..length).asSequence().mapNotNull { item(it) as? Element }
private val Node.childElements get() = childNodes.elements
private fun Document.selectFirstTag(tag: String): Node? = getElementsByTagName(tag).item(0)
private fun Node.selectFirstChildTag(tag: String) = childElements.find { it.tagName == tag }
private fun Node.selectChildTag(tag: String) = childElements.filter { it.tagName == tag }
private fun Node.selectFirstChildTagAttr(tag: String,attr:String) = selectChildTag(tag).find { it.hasAttribute(attr) }
private fun Node.selectFirstChildTagAttrValue(tag: String,attr:String,value:String) = selectChildTag(tag).find { it.getAttribute(attr)==value }
private fun Node.getAttributeValue(attribute: String): String? =
    attributes?.getNamedItem(attribute)?.textContent

private fun parseXMLFile(inputSteam: InputStream): Document? =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSteam)

private fun parseXMLFile(byteArray: ByteArray): Document? = parseXMLFile(byteArray.inputStream())
private fun parseXMLText(text: String): Document? = text.reader().runCatching {
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(this))
}.getOrNull()

val String.decodedURL: String get() = URLDecoder.decode(this, "UTF-8")
private fun String.asFileName(): String = this.replace("/", "_")

private fun ZipInputStream.entries() = generateSequence { nextEntry }

fun createEpubBook(inputStream: InputStream,block:((EpubBook?,Boolean,String)->Unit)) {
    val zipFile = ZipInputStream(inputStream).use { zipInputStream ->
        zipInputStream
            .entries()
            .filterNot { it.isDirectory }
            .associate { it.name to (it to zipInputStream.readBytes()) }
    }

    fun Exception.callback(block:((EpubBook?,Boolean,String)->Unit)):Throwable{
        block.invoke(null,false,message.toString())
        return this
    }

    val container =
        zipFile["META-INF/container.xml"] ?: throw Exception("META-INF/container.xml file missing").callback(block)

    val opfFilePath = parseXMLFile(container.second)
        ?.selectFirstTag("rootfile")
        ?.getAttributeValue("full-path")
        ?.decodedURL ?: throw Exception("Invalid container.xml file").callback(block)

    val opfFile = zipFile[opfFilePath] ?: throw Exception(".opf file missing").callback(block)

    val document = parseXMLFile(opfFile.second) ?: throw Exception(".opf file failed to parse data").callback(block)
    val metadata =
        document.selectFirstTag("metadata") ?: throw Exception(".opf file metadata section missing").callback(block)
    val manifest =
        document.selectFirstTag("manifest") ?: throw Exception(".opf file manifest section missing").callback(block)
    val spine =
        document.selectFirstTag("spine") ?: throw Exception(".opf file spine section missing").callback(block)

    val bookTitle = metadata.selectFirstChildTag("dc:title")?.textContent
        ?: throw Exception(".opf metadata title tag missing")
    val bookUrl = bookTitle.asFileName()
    val rootPath = File(opfFilePath).parentFile ?: File("")
    fun String.absPath() = File(rootPath, this).path.replace("""\""", "/").removePrefix("/")

    fun Map<String,EpubManifestItem>.coverByProperties():EpubImage?
        = values.asSequence()
            .filter { it.mediaType.startsWith("image/") }
            .find { it.properties == "cover-image" }
            ?.let { zipFile[it.href.absPath()] }
            ?.let { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val covers = arrayOf("cover","cover-image")
    val chapterExtensions = listOf("xhtml", "xml", "html").map { ".$it" }

    fun String.filePrefix():String{
        val index = indexOf(".")
        return if(index!=-1) substring(0,index) else this
    }
    fun String.filePostfix():String{
        val index = lastIndexOf(".")
        return if(index!=-1 && index<length) substring(index,length) else this
    }

    fun Map<String,Pair<ZipEntry,ByteArray>>.cover():EpubImage?
         = keys.filter {
            it.filePostfix() !in chapterExtensions
        }.find {
            File(it).name.filePrefix().contains("cover")
        }?.let{ EpubImage(path = it,this[it]!!.second)}

    fun Map<String,EpubManifestItem>.coverById():EpubImage?
        = values.asSequence()
            .filter { it.mediaType.startsWith("image/") }
            .find { it.id in covers }
            ?.let { zipFile[it.href.absPath()] }
            ?.let { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val items = manifest.selectChildTag("item").map {
        EpubManifestItem(
            id = it.getAttribute("id"),
            href = it.getAttribute("href").decodedURL,
            mediaType = it.getAttribute("media-type"),
            properties = it.getAttribute("properties")
        )
    }.associateBy { it.id }

    val idRef = spine.selectChildTag("itemref").map { it.getAttribute("idref") }

    val ncxDocument = items["ncx"]?.href?.absPath()
        ?.let { zipFile[it] }
        ?.let { parseXMLText(String(it.second,Charsets.UTF_8)) }

    /**
     * 如果ncx文件存在‘&’，会出现‘unterminated entity ref’的异常，原因是xml解析把&当作转义字符。
     * **/
    /*val ncxDocument = items["ncx"]?.href?.absPath()
        ?.let { zipFile[it] }
        ?.let { parseXMLFile(it.second) }*/

    fun String.sub(block:((String)->String)) = block(this)

    fun Element.navPoint(index:Int,isSubNavPoint:Boolean = false):EpubNavPoint {
        val url = selectFirstChildTag("content")?.getAttribute("src")
            ?.decodedURL?:""
        val src = url.sub {
            if(it.contains("#")) it.substring(0,it.indexOf("#"))
            else it
        }
        val urlPath = src.absPath()
        val body = EpubXMLFileParser(urlPath, zipFile[urlPath]!!.second, zipFile).parse().body
        val id = url.sub {
            if (it.contains("#")) it.substring(it.indexOf("#") + 1, it.length)
            else ""
        }
        return EpubNavPoint(
            id = getAttributeValue("id") ?: "",
            playOrder = getAttributeValue("playOrder") ?: "",
            title = selectFirstChildTag("navLabel")?.textContent?.toString()
                ?.trim { it == ' ' || it == '\n' || it == '\r' },
            url = src,
            body = body,
            chapterIndex = index,
            isSubNavPoint = isSubNavPoint,
            subNavPointId = id
        )
    }

    val chapterNav = ArrayList<EpubNavPoint>()

    ncxDocument?.let { it.selectFirstTag("navMap")?:throw Exception(".ncx file navMap section missing").callback(block) }
        ?.selectChildTag("navPoint")
        ?.forEach { element ->
            val index = chapterNav.size
            val subChapter = element.getElementsByTagName("navPoint")?.elements
            if(subChapter==null) chapterNav.add(element.navPoint(index))
            else {
                chapterNav.add(element.navPoint(index))
                chapterNav.addAll(subChapter.map { it.navPoint(index = index,isSubNavPoint = true) })
            }
        }

    /*fun TempEpubNavPoint.toEpubNavPoint(ids:List<String>):EpubNavPoint{
        val urlAbsPath = url.absPath()
        val body = if(!isSubNavPoint) EpubXMLFileParser(urlAbsPath, zipFile[urlAbsPath]!!.second, zipFile).parsePlus(ids).body else ""
        return EpubNavPoint(
            id = id,
            playOrder = playOrder,
            url = url,
            body = body,
            title = title,
            chapterIndex = chapterIndex,
            isSubNavPoint = isSubNavPoint,
            subNavPointId = subNavPointId
        )
    }

    val subChapterNavPoint = chapterNav.filter { it.isSubNavPoint }.map { it.subNavPointId }
    val contentChapterNavPoint = chapterNav.filter { !it.isSubNavPoint }.map { it.toEpubNavPoint(subChapterNavPoint) }

    Log.d("TAG", "createEpubBook: sub $subChapterNavPoint")
    Log.d("TAG", "createEpubBook: ${contentChapterNavPoint.map { it.title }}")

    val chapters = contentChapterNavPoint.mapIndexed { index, nav ->
        TempEpubChapter(
                url = nav.url,
                title = nav.title,
                body = nav.body,
                chapterIndex = index,
            )
        }.groupBy {
            it.chapterIndex
        }.map { (index, list) ->
            EpubChapter(
                url = list.first().url,
                title = list.first().title!!,
                body = list.joinToString("\n\n") { it.body },
                index = index
            )
        }.filter {
            it.body.isNotBlank()
        }*/

    var chapterIndex = 0
    val chapters = idRef
        .mapNotNull { items[it] }
        .filter { item -> chapterExtensions.any { item.href.endsWith(it, ignoreCase = true) } }
        .mapNotNull { zipFile[it.href.absPath()] }
        .mapIndexed { index, (entry, byteArray) ->
            val res = EpubXMLFileParser(entry.name, byteArray, zipFile).parse()
            val chapterTitle = res.title ?: if (index == 0) bookTitle else null
            if (chapterTitle != null)
                chapterIndex += 1

            TempEpubChapter(
                url = "$bookUrl/${entry.name}",
                title = chapterTitle,
                body = res.body,
                chapterIndex = chapterIndex,
            )
        }
        .groupBy { it.chapterIndex }
        .map { (index, list) ->
            EpubChapter(
                url = list.first().url,
                title = list.first().title!!,
                body = list.joinToString("\n\n") { it.body },
                index = index
            )
        }.filter { it.body.isNotBlank() }

    val imageExtensions = listOf("png", "gif", "raw", "png", "jpg", "jpeg", "webp", "bmp").map { ".$it" }

    val listedImages = items.values.asSequence()
        .filter { it.mediaType.startsWith("image/") }
        .mapNotNull { zipFile[it.href.absPath()] }
        .map { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val unlistedImages = zipFile.values.asSequence()
        .filterNot { (entry, _) -> entry.isDirectory }
        .filter { (entry, _) -> imageExtensions.any { entry.name.endsWith(it, ignoreCase = true) } }
        .map { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val images = (listedImages + unlistedImages).distinctBy { it.path }

    val coverImage = items.coverByProperties()?:items.coverById()?:zipFile.cover()

    val epubInfo = EpubInfo(
        creator = metadata.selectFirstChildTag("dc:creator")?.textContent?:"",
        description = metadata.selectFirstChildTag("dc:description")?.textContent?:"",
        publisher = metadata.selectFirstChildTag("dc:publisher")?.textContent?:"",
        language = metadata.selectFirstChildTag("dc:language")?.textContent?:"",
        type = metadata.selectFirstChildTag("dc:type")?.textContent?:"",
        identifier = metadata.selectFirstChildTagAttr("dc:identifier","id")?.textContent?:"",
        isbn = metadata.selectFirstChildTagAttrValue("dc:identifier","opf:scheme","ISBN")?.textContent?:"",
        publicationDate = metadata.selectFirstChildTagAttrValue("dc:date","opf:event","publication")?.textContent?:"",
        epubCreation = metadata.selectFirstChildTagAttrValue("dc:date","opf:event","creation")?.textContent?:""
    )

    EpubBook(
        fileName = bookUrl,
        title = bookTitle,
        coverImagePath = coverImage?.path ?: "",
        chapters = chapters,
        chapterNavPoints = chapterNav,
        images = images.toList(),
        info = epubInfo
    ).let { block(it,true,"no abnormalities") }
}