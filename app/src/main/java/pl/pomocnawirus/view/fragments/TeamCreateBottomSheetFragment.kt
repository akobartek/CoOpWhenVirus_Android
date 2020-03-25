package pl.pomocnawirus.view.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_team_create_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_team_create_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.utils.*

class TeamCreateBottomSheetFragment(private val teamJoinFragment: TeamJoinFragment) :
    BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mAuth: FirebaseAuth
    private var isEmailCheckBoxChecked = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        mAuth = FirebaseAuth.getInstance()
        val view = View.inflate(requireContext(), R.layout.fragment_team_create_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        view.teamNameET.markRequiredInRed()
        view.teamCityET.markRequiredInRed()
        view.teamPhoneET.markRequiredInRed()

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarSaveBtn.setOnClickListener {
            view.toolbarSaveBtn.isEnabled = false
            val teamName = view.teamNameET.text.toString().trim()
            val city = view.teamCityET.text.toString().trim()
            val phoneNumber = view.teamPhoneET.text.toString().trim()
            val email =
                if (isEmailCheckBoxChecked) mAuth.currentUser?.email
                else view.teamEmailET.text.toString().trim()

            if (!areValuesValid(teamName, city, phoneNumber, email)) {
                view.toolbarSaveBtn.isEnabled = true
                return@setOnClickListener
            }
            val team = Team(
                "", teamName, city, arrayListOf(mAuth.currentUser!!.uid),
                email ?: "", phoneNumber
            )
            dismiss()
            teamJoinFragment.createNewTeam(team)
        }

        view.teamEmailCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isEmailCheckBoxChecked = isChecked
            if (isEmailCheckBoxChecked) {
                view.teamEmailET.text?.clear()
                view.teamEmailET.disable()
            } else view.teamEmailET.enable()
        }

        return bottomSheetDialog
    }

    private fun areValuesValid(name: String, city: String, phone: String, email: String?): Boolean {
        var isValid = true

        if (name.length < 3) {
            view!!.teamNameET.error = getString(R.string.name_error_too_short)
            isValid = false
        }
        if (city.isEmpty()) {
            view!!.teamCityET.error = getString(R.string.city_error_empty)
            isValid = false
        }
        if (phone.isEmpty()) {
            view!!.teamPhoneET.error = getString(R.string.phone_error_empty)
            isValid = false
        } else if (!phone.isValidPhoneNumber()) {
            view!!.teamPhoneET.error = getString(R.string.phone_error_incorrect)
            isValid = false
        }
        if (!email.isNullOrEmpty() && !email.isValidEmail()) {
            view!!.teamEmailET.error = getString(R.string.email_error)
            isValid = false
        }
        return isValid
    }
}
