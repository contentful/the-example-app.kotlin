package com.contentful.tea.kotlin.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.extensions.showError
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.android.synthetic.main.fragment_qrcode_scanner.*
import kotlinx.android.synthetic.main.fragment_qrcode_scanner.view.*

private const val PERMISSION_CAMERA_REQUEST_ID: Int = 1

class QRCodeScannerFragment : Fragment() {
    private val decodeCallback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            if (result != null &&
                settings_qr_barcode != null
            ) {
                if (result.text.startsWith("the-example-app-mobile://")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.text)))
                } else {
                    if (activity != null) {
                        activity?.showError(
                            message = getString(R.string.settings_qr_non_tea_url, result.text),
                            okHandler = { settings_qr_barcode.decodeSingle(this) }
                        )
                    }
                }
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_qrcode_scanner, container, false)
        root.settings_qr_barcode.apply {
            barcodeView.decoderFactory = DefaultDecoderFactory(BarcodeFormat.values().asList())
            decodeSingle(decodeCallback)
            setStatusText(getString(R.string.settings_qr_finder_status_text))
        }

        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST_ID
            )
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        settings_qr_barcode.resume()
    }

    override fun onPause() {
        settings_qr_barcode.pauseAndWait()
        super.onPause()
    }
}
