package pl.marta.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_team_details_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_team_details_bottom_sheet.view.*
import pl.marta.R
import pl.marta.model.TeamSimple
import pl.marta.utils.setLayoutFullHeight
import pl.marta.utils.showShortToast

class TeamDetailsBottomSheetFragment(private val mTeam: TeamSimple) : BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return inflater.inflate(R.layout.fragment_team_details_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.teamNameTV.text = mTeam.name
        view.teamCityTV.text = mTeam.city
        view.teamPhoneTV.text = mTeam.phone
        view.teamEmailTV.text = mTeam.email
        view.teamEmailTV.visibility = if (mTeam.email.isEmpty()) View.GONE else View.VISIBLE

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarEmailBtn.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mTeam.email))
            try {
                startActivity(
                    Intent.createChooser(emailIntent, getString(R.string.send_email_chooser))
                )
            } catch (ex: android.content.ActivityNotFoundException) {
                requireContext().showShortToast(R.string.send_email_error)
            }
        }
        view.toolbarSmsBtn.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_SENDTO)
            smsIntent.data = Uri.parse("smsto:${mTeam.phone}")
            startActivity(smsIntent)
        }
    }
}
