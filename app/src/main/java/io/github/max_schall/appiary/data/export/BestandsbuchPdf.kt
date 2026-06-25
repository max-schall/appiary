package io.github.max_schall.appiary.data.export

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.net.toUri
import java.io.File
import java.io.OutputStream

/** One row of the Bestandsbuch (all fields already localized/formatted). */
data class BestandsbuchEntry(
    val date: String,
    val medicine: String,
    val supplier: String,
    val quantity: String,
    val duration: String,
    val withdrawal: String,
    val identity: String,
    val vet: String,
)

/** Everything the PDF needs (assembled by the ViewModel). */
data class BestandsbuchExport(
    val title: String,
    val apiaryLine: String,
    val generatedLine: String,
    val columnHeaders: List<String>, // 8 headers, order matches BestandsbuchEntry
    val entries: List<BestandsbuchEntry>,
    val receiptCaption: String,      // template with one %d
    val receiptPhotoUris: List<String>,
)

/**
 * Renders a Bestandsbuch PDF (landscape A4) with the treatment table and one page
 * per attached receipt photo. Uses the platform [PdfDocument] — no dependency,
 * fully local/offline. Writes to a caller-provided [OutputStream] (e.g. SAF).
 */
object BestandsbuchPdf {
    private const val W = 842
    private const val H = 595
    private const val MARGIN = 30f
    private val colWidths = floatArrayOf(70f, 92f, 170f, 70f, 100f, 64f, 96f, 120f) // sums ≈ 782

    fun write(out: OutputStream, export: BestandsbuchExport) {
        val doc = PdfDocument()
        var pageNum = 1

        val title = Paint().apply { color = Color.BLACK; textSize = 16f; isFakeBoldText = true }
        val meta = Paint().apply { color = Color.DKGRAY; textSize = 9f }
        val header = Paint().apply { color = Color.BLACK; textSize = 8.5f; isFakeBoldText = true }
        val cell = Paint().apply { color = Color.BLACK; textSize = 8.5f }
        val line = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }

        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNum++).create())
        var canvas = page.canvas
        var y = MARGIN

        canvas.drawText(export.title, MARGIN, y + 12f, title); y += 26f
        canvas.drawText(export.apiaryLine, MARGIN, y, meta); y += 13f
        canvas.drawText(export.generatedLine, MARGIN, y, meta); y += 18f

        fun drawHeaderRow() {
            var x = MARGIN
            export.columnHeaders.forEachIndexed { i, h ->
                canvas.drawText(truncate(h, colWidths[i], header), x, y, header)
                x += colWidths[i]
            }
            y += 4f
            canvas.drawLine(MARGIN, y, MARGIN + colWidths.sum(), y, line)
            y += 12f
        }
        drawHeaderRow()

        export.entries.forEach { e ->
            if (y > H - MARGIN) {
                doc.finishPage(page)
                page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNum++).create())
                canvas = page.canvas
                y = MARGIN
                drawHeaderRow()
            }
            val cells = listOf(e.date, e.medicine, e.supplier, e.quantity, e.duration, e.withdrawal, e.identity, e.vet)
            var x = MARGIN
            cells.forEachIndexed { i, c ->
                canvas.drawText(truncate(c, colWidths[i], cell), x, y, cell)
                x += colWidths[i]
            }
            y += 16f
        }
        doc.finishPage(page)

        // One page per receipt photo.
        export.receiptPhotoUris.forEachIndexed { idx, uri ->
            val bmp = decodeScaled(uri, 1400) ?: return@forEachIndexed
            val p = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNum++).create())
            val c = p.canvas
            c.drawText(export.receiptCaption.format(idx + 1), MARGIN, MARGIN, meta)
            val maxW = W - 2 * MARGIN
            val maxH = H - 2 * MARGIN - 20f
            val scale = minOf(maxW / bmp.width, maxH / bmp.height, 1f)
            val dw = bmp.width * scale
            val dh = bmp.height * scale
            val dst = android.graphics.RectF(MARGIN, MARGIN + 16f, MARGIN + dw, MARGIN + 16f + dh)
            c.drawBitmap(bmp, null, dst, null)
            doc.finishPage(p)
            bmp.recycle()
        }

        doc.writeTo(out)
        doc.close()
    }

    private fun truncate(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth - 4f) return text
        var t = text
        while (t.isNotEmpty() && paint.measureText("$t…") > maxWidth - 4f) t = t.dropLast(1)
        return "$t…"
    }

    private fun decodeScaled(uri: String, maxDim: Int): android.graphics.Bitmap? {
        val path = runCatching { uri.toUri().path }.getOrNull() ?: return null
        val file = File(path)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(file.path, opts)
    }
}
