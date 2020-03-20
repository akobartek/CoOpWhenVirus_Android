package pl.pomocnawirus.view.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_group_create_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_group_create_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Group
import pl.pomocnawirus.utils.disable
import pl.pomocnawirus.utils.enable
import pl.pomocnawirus.utils.isValidEmail
import pl.pomocnawirus.utils.isValidPhoneNumber

class GroupCreateBottomSheetFragment(val groupJoinFragment: GroupJoinFragment) :
    BottomSheetDialogFragment() {

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mAuth: FirebaseAuth
    private var isEmailCheckBoxChecked = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        mAuth = FirebaseAuth.getInstance()
        val view = View.inflate(requireContext(), R.layout.fragment_group_create_bottom_sheet, null)
        bottomSheet.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarSaveBtn.setOnClickListener {
            view.toolbarSaveBtn.isEnabled = false
            val groupName = view.groupNameET.text.toString().trim()
            val city = view.groupCityET.text.toString().trim()
            val phoneNumber = view.groupPhoneET.text.toString().trim()
            val email =
                if (isEmailCheckBoxChecked) mAuth.currentUser?.email
                else view.groupEmailET.text.toString().trim()

            if (!areValuesValid(groupName, city, phoneNumber, email)) {
                view.toolbarSaveBtn.isEnabled = true
                return@setOnClickListener
            }
            val group = Group(
                "", groupName, city, arrayListOf(mAuth.currentUser!!.uid),
                email ?: "", phoneNumber
            )
            dismiss()
            groupJoinFragment.createNewGroup(group)
        }

        view.groupEmailCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isEmailCheckBoxChecked = isChecked
            if (isEmailCheckBoxChecked) {
                view.groupEmailET.text?.clear()
                view.groupEmailET.disable()
            } else view.groupEmailET.enable()
        }

        return bottomSheet
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun areValuesValid(name: String, city: String, phone: String, email: String?): Boolean {
        var isValid = true

        if (name.length < 3) {
            view!!.groupNameET.error = getString(R.string.name_error_too_short)
            isValid = false
        }
        if (city.isEmpty()) {
            view!!.groupCityET.error = getString(R.string.city_error_empty)
            isValid = false
        }
        if (phone.isEmpty()) {
            view!!.groupPhoneET.error = getString(R.string.phone_error_empty)
            isValid = false
        } else if (!phone.isValidPhoneNumber()) {
            view!!.groupPhoneET.error = getString(R.string.phone_error_incorrect)
            isValid = false
        }
        if (!email.isNullOrEmpty() && !email.isValidEmail()) {
            view!!.groupEmailET.error = getString(R.string.email_error)
            isValid = false
        }
        return isValid
    }
}
