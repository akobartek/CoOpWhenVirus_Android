package pl.marta.view.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.content_about_us.view.*
import kotlinx.android.synthetic.main.fragment_about_us.view.*
import pl.marta.R
import pl.marta.utils.PreferencesManager
import pl.marta.utils.isChromeCustomTabsSupported
import pl.marta.utils.showShortToast

class AboutUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_about_us, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.aboutUsToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.aboutUsToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val text = getString(R.string.about_us_text_part1) + "\n" + getString(R.string.about_us_text_part2)
        view.aboutUsTV.text = text

        view.contactGithubBtn.setOnClickListener { openUrl("https://github.com/akobartek/Marta_Android") }
        view.contactEmailBtn.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, "sokolowskijbartek@gmail.com")
            try {
                startActivity(
                    Intent.createChooser(emailIntent, getString(R.string.send_email_chooser))
                )
            } catch (ex: android.content.ActivityNotFoundException) {
                requireContext().showShortToast(R.string.send_email_error)
            }
        }
    }

    private fun openUrl(url: String) {
        if (requireContext().isChromeCustomTabsSupported()) {
            CustomTabsIntent.Builder().apply {
                val color =
                    if (PreferencesManager.getNightMode()) Color.parseColor("#28292e")
                    else Color.WHITE
                setToolbarColor(color)
                setSecondaryToolbarColor(color)
            }.build().launchUrl(requireContext(), Uri.parse(url))
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}
