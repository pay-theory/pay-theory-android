package com.paytheory.android.sdk.view

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.paytheory.android.sdk.BarcodeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class PayTheoryBarcode @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    fun displayBarcode(barcodeResult: BarcodeResult) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        retrieveSvgFromUrl(barcodeResult.barcodeUrl) { svgData ->
            if (svgData != null) {
                try {
                    val svg = SVG.getFromString(svgData)
                    val drawable = PictureDrawable(svg.renderToPicture())
                    setImageDrawable(drawable)
                } catch (e: SVGParseException) {
                    // Handle SVG parsing error
                }
            } else {
                // Handle error loading SVG
            }
        }
    }

    private fun retrieveSvgFromUrl(url: String, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val svgData = response.body?.string()
                    withContext(Dispatchers.Main) { onComplete(svgData) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(null) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(null) }
            }
        }
    }
}




