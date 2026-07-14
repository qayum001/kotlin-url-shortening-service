package org.example.mock.service

import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QrService(
    @Value("\${app.base-url}") private val baseUrl: String,
) {
    private companion object {
        const val QUIET_ZONE = 4
    }

    fun svgForCode(code: String): String {
        val target = "${baseUrl.trimEnd('/')}/$code"
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = Encoder.encode(target, ErrorCorrectionLevel.M, hints).matrix
        val dim = matrix.width + QUIET_ZONE * 2

        val path = StringBuilder()
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix.get(x, y).toInt() == 1) {
                    path.append("M${x + QUIET_ZONE} ${y + QUIET_ZONE}h1v1h-1z")
                }
            }
        }

        return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $dim $dim" """ +
            """shape-rendering="crispEdges"><rect width="$dim" height="$dim" fill="#ffffff"/>""" +
            """<path fill="#000000" d="$path"/></svg>"""
    }
}
