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


/**
 * Custom ImageView to display SVG barcode from a URL.
 */
class PayTheoryBarcode @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    /**
     * Displays the barcode image from the provided BarcodeResult.
     *
     * It retrieves the SVG data from the barcode URL, parses it, and sets it as the image
     * for the ImageView.
     *
     * @param barcodeResult The BarcodeResult containing the barcode URL.
     */
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

    /**
     * Retrieves the SVG data from the specified URL using OkHttp.
     *
     * This function runs on a background thread and calls the onComplete callback with the SVG
     * data or null if an error occurs.
     *
     * @param url The URL of the SVG image.
     * @param onComplete Callback function to be executed when the SVG data is retrieved.
     */
    private fun retrieveSvgFromUrl(url: String, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val svgData = response.body?.string()
                withContext(Dispatchers.Main) { onComplete(svgData) }
            } else {
                withContext(Dispatchers.Main) { onComplete(null) }
            }

        }
    }
}




