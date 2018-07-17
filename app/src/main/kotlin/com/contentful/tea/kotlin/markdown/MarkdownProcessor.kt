package com.contentful.tea.kotlin.markdown

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Document
import org.commonmark.node.Image
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlNodeRendererFactory
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.html.HtmlWriter

class MarkdownProcessor(context: Context) {
    private val parser: Parser = Parser.builder().build()
    private val applicationContext: Context = context.applicationContext
    private val renderer = HtmlRenderer.builder()
        .nodeRendererFactory(ImageNodeFactory())
        .build()

    inner class FlattenMDHierarchy : AbstractVisitor() {
        override fun visit(paragraph: Paragraph) {
            val parent = paragraph.parent
            if (parent is Document) {
                if (parent.getFirstChild() == paragraph && parent.getLastChild() == paragraph) {
                    while (paragraph.firstChild != null) {
                        paragraph.insertBefore(paragraph.firstChild)
                    }
                    paragraph.unlink()
                }
            }
        }
    }

    inner class ImageNodeFactory : HtmlNodeRendererFactory {
        override fun create(context: HtmlNodeRendererContext): NodeRenderer {
            return ImageNodeRenderer(context)
        }

        inner class ImageNodeRenderer(context: HtmlNodeRendererContext) :
            NodeRenderer {
            private val html: HtmlWriter = context.writer

            override fun getNodeTypes(): MutableSet<Class<out Node>> {
                return mutableSetOf(Image::class.java)
            }

            override fun render(node: Node?) {
                val image = node as Image
                val width = smallestScreenDimension()
                html.raw("<img src=\"https:${image.destination}?fm=webp&w=$width\">")
            }
        }
    }

    private fun smallestScreenDimension(): Int = Math.min(
        Resources.getSystem().displayMetrics.widthPixels,
        Resources.getSystem().displayMetrics.heightPixels
    )

    inner class ContentfulImageGetter : Html.ImageGetter {
        override fun getDrawable(url: String): Drawable {
            val bytes =
                runBlocking {
                    async {
                        OkHttpClient.Builder().build()
                            .newCall(Request.Builder().get().url(url).build()).execute().body()!!
                            .bytes()
                    }.await()
                }

            var bitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size, BitmapFactory.Options())

            if (url.contains(".svg?")) {
                val width = smallestScreenDimension() * .9f
                val height = (bitmap.height / bitmap.width.toFloat()) * width
                bitmap = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), true)
            }

            val drawable = BitmapDrawable(applicationContext.resources, bitmap)

            return drawable.apply {
                setBounds(
                    0,
                    0,
                    bitmap.width,
                    bitmap.height
                )
            }
        }
    }

    fun parse(input: CharSequence): CharSequence {
        val document = parser.parse(input.toString())
        document.accept(FlattenMDHierarchy())

        val html = renderer.render(document)
        return Html.fromHtml(html, 0, ContentfulImageGetter(), null)
    }
}