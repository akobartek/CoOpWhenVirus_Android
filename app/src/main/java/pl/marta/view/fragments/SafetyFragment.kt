package pl.marta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_safety.view.*
import pl.marta.R

class SafetyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_safety, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.safetyToolbar.inflateMenu(R.menu.safety_menu)
        view.safetyToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(SafetyFragmentDirections.showSettingsFragment())
                    true
                }
                R.id.action_about_us -> {
                    findNavController().navigate(SafetyFragmentDirections.showAboutUsFragment())
                    true
                }
                else -> true
            }
        }
    }
}
