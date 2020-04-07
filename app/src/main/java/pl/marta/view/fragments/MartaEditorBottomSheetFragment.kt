package pl.marta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_marta_editor_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_marta_editor_bottom_sheet.view.*
import pl.marta.R
import pl.marta.model.Marta
import pl.marta.utils.*
import pl.marta.viewmodel.MartasViewModel

class MartaEditorBottomSheetFragment(private val mMarta: Marta?) : BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mViewModel: MartasViewModel

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
        return inflater.inflate(R.layout.fragment_marta_editor_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(MartasViewModel::class.java)
        view.martaNameInputLayout.markRequiredInRed()
        view.addressInputLayout.markRequiredInRed()
        view.cityInputLayout.markRequiredInRed()
        view.phoneInputLayout.markRequiredInRed()

        if (mMarta != null) {
            view.toolbarTitle.text = getString(R.string.edit_template)
            view.martaNameET.setText(mMarta.name)
            view.martaNameET.disable()
            view.addressET.setText(mMarta.address)
            view.addressET.disable()
            view.cityET.setText(mMarta.city)
            view.cityET.disable()
            view.phoneET.setText(mMarta.phone)
            view.phoneET.disable()
            view.emailET.setText(mMarta.email)
            view.emailET.disable()
        } else {
            view.toolbarTitle.text = getString(R.string.add_template)
            view.martaNameET.enable()
            view.addressET.enable()
            view.cityET.enable()
            view.phoneET.enable()
            view.emailET.enable()
        }
        view.toolbarEditMartaBtn.visibility = if (mMarta != null) View.VISIBLE else View.GONE
        view.toolbarSaveMartaBtn.visibility = if (mMarta != null) View.GONE else View.VISIBLE

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarEditMartaBtn.setOnClickListener {
            view.toolbarEditMartaBtn.visibility = View.GONE
            view.toolbarSaveMartaBtn.visibility = View.VISIBLE
            view.martaNameET.enable()
            view.addressET.enable()
            view.cityET.enable()
            view.phoneET.enable()
            view.emailET.enable()
        }
        view.toolbarSaveMartaBtn.setOnClickListener {
            val martaName = view.martaNameET?.text.toString().trim()
            val address = view.addressET?.text.toString().trim()
            val city = view.cityET?.text.toString().trim()
            val phoneNumber = view.phoneET?.text.toString().trim()
            val email = view.emailET?.text.toString().trim()

            if (!areValuesValid(martaName, address, city, phoneNumber, email))
                return@setOnClickListener

            val marta = mMarta ?: Marta()
            marta.apply {
                this.name = martaName
                this.address = address
                this.city = city
                this.phone = phoneNumber
                this.email = email
            }

            if (mMarta == null) mViewModel.addNewMarta(marta)
            else mViewModel.updateMarta(marta)
            dismiss()
        }
    }

    private fun areValuesValid(
        name: String, address: String, city: String, phone: String, email: String?
    ): Boolean {
        var isValid = true

        if (name.length < 3) {
            view?.martaNameET?.error = getString(R.string.name_error_too_short)
            isValid = false
        }
        if (address.isEmpty()) {
            view?.addressET?.error = getString(R.string.address_empty_error)
            isValid = false
        }
        if (city.isEmpty()) {
            view!!.cityET.error = getString(R.string.city_error_empty)
            isValid = false
        }
        if (phone.isEmpty()) {
            view!!.phoneET.error = getString(R.string.phone_error_empty)
            isValid = false
        } else if (!phone.isValidPhoneNumber()) {
            view!!.phoneET.error = getString(R.string.phone_error_incorrect)
            isValid = false
        }
        if (!email.isNullOrEmpty() && !email.isValidEmail()) {
            view!!.emailET.error = getString(R.string.email_error)
            isValid = false
        }
        return isValid
    }
}
