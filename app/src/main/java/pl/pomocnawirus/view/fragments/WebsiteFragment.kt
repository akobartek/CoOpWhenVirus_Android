package pl.pomocnawirus.view.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_website.view.*
import pl.pomocnawirus.R

class WebsiteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_website, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val urlAddress = "https://korona.ws/"
        view.webView.settings.javaScriptEnabled = true
        view.webView.settings.domStorageEnabled = true
        view.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(webView: WebView, url: String, favicon: Bitmap?) {
                if (url == urlAddress)
                    view.loadingIndicator.show()
                super.onPageStarted(webView, url, favicon)
            }

            override fun onPageCommitVisible(webView: WebView?, url: String?) {
                view.loadingIndicator.hide()
                super.onPageCommitVisible(webView, url)
            }
        }

        if (savedInstanceState != null) view.webView.restoreState(savedInstanceState)
        else view.webView.loadUrl(urlAddress)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view?.webView?.saveState(outState)
    }

    fun onBackPressed() {
        if (view?.webView?.canGoBack() == true) view?.webView?.goBack()
        else findNavController().navigateUp()
    }
}
