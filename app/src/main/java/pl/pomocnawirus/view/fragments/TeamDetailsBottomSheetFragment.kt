package pl.pomocnawirus.view.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_team_info_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_team_details_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.TeamSimple
import pl.pomocnawirus.utils.setLayoutFullHeight

class TeamDetailsBottomSheetFragment(private val mTeam: TeamSimple) : BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val view = View.inflate(requireContext(), R.layout.fragment_team_details_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

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
                    Intent.createChooser(
                        emailIntent,
                        getString(R.string.send_email_chooser)
                    )
                )
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(context, getString(R.string.send_email_error), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        view.toolbarSmsBtn.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_SENDTO)
            smsIntent.data = Uri.parse("smsto:${mTeam.phone}")
            startActivity(smsIntent)
        }

        return bottomSheetDialog
    }
}
