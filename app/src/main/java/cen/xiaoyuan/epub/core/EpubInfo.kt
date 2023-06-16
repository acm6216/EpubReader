package cen.xiaoyuan.epub.core

data class EpubInfo(
    val creator:String,
    val description:String,
    val publisher:String,
    val language:String,
    val type:String,
    val identifier:String,
    val isbn:String,
    val publicationDate:String,
    val epubCreation:String
)