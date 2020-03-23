package pl.pomocnawirus.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.content_account.view.*
import kotlinx.android.synthetic.main.dialog_change_email.view.*
import kotlinx.android.synthetic.main.dialog_change_password.view.*
import kotlinx.android.synthetic.main.dialog_delete_account.view.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.User
import pl.pomocnawirus.utils.*
import pl.pomocnawirus.view.activities.MainActivity
import pl.pomocnawirus.viewmodel.AccountViewModel

class AccountFragment : Fragment() {

    private lateinit var mViewModel: AccountViewModel
    private lateinit var mLoadingDialog: AlertDialog
    private lateinit var mCurrentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

        mViewModel = ViewModelProvider(requireActivity()).get(AccountViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        mCurrentUser = (requireActivity() as MainActivity).getCurrenUser()!!

        view.nameET.setText(mCurrentUser.name)
        view.phoneET.setText(mCurrentUser.phone)

        view.saveUserBtn.setOnClickListener {
            view.saveUserBtn.isEnabled = false
            val name = view.nameET.text.toString().trim()
            val phoneNumber = view.phoneET.text.toString().trim()

            if (!areValuesValid(name, phoneNumber)) {
                view.saveUserBtn.isEnabled = true
                return@setOnClickListener
            }

            if (mCurrentUser.name == name && mCurrentUser.phone == phoneNumber) {
                view.saveUserBtn.isEnabled = true
                requireContext().showShortToast(R.string.data_saved)
            } else {
                mCurrentUser.name = name
                mCurrentUser.phone = phoneNumber
                mViewModel.updateUserData(mCurrentUser)
                    .observe(viewLifecycleOwner, Observer { updateSuccessful ->
                        view.saveUserBtn.isEnabled = true
                        if (updateSuccessful) requireContext().showShortToast(R.string.data_saved)
                        else requireContext().showBasicAlertDialog(
                            R.string.update_error_title,
                            R.string.update_error_message
                        )
                    })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    private fun areValuesValid(name: String, phone: String): Boolean {
        var isValid = true

        if (name.length < 3) {
            view!!.nameET.error = getString(R.string.name_error_too_short)
            isValid = false
        }
        if (phone.isNotEmpty() && !phone.isValidPhoneNumber()) {
            view!!.phoneET.error = getString(R.string.phone_error_incorrect)
            isValid = false
        }
        return isValid
    }

    private fun inflateToolbarMenu() {
        view?.accountToolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        view?.accountToolbar?.setNavigationOnClickListener { findNavController().navigateUp() }
        view?.accountToolbar?.inflateMenu(R.menu.tasks_menu)
        view?.accountToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_change_password -> {
                    showChangePasswordDialog()
                    true
                }
                R.id.action_change_email -> {
                    showChangeEmailDialog()
                    true
                }
                R.id.action_leave_team -> {
                    showLeaveTeamDialog()
                    true
                }
                R.id.action_delete_account -> {
                    showDeleteAccountDialog()
                    true
                }
                else -> true
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.change_password)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val currentPassword = dialogView.currentPasswordET.text.toString()
                val newPassword = dialogView.newPasswordET.text.toString()
                if (newPassword != dialogView.repeatPasswordET.text.toString().trim()) {
                    dialogView.repeatPasswordET.error = getString(R.string.repeated_password_error)
                    return@setOnClickListener
                }
                if (newPassword.length < 6) {
                    dialogView.newPasswordET.error = getString(R.string.password_error_too_short)
                    return@setOnClickListener
                } else if (!newPassword.isValidPassword()) {
                    dialogView.newPasswordET.error = getString(R.string.password_error_wrong)
                    return@setOnClickListener
                }

                mViewModel.reAuthenticateUser(currentPassword)
                    .observe(viewLifecycleOwner, Observer { authenticateSuccessful ->
                        if (authenticateSuccessful) {
                            mViewModel.updateUserPassword(newPassword)
                                .observe(viewLifecycleOwner, Observer { passwordChanged ->
                                    if (passwordChanged) {
                                        requireContext().showShortToast(R.string.change_password_successful)
                                        dialog.dismiss()
                                    } else
                                        requireContext().showShortToast(R.string.operation_failed_error)
                                })
                        } else {
                            dialogView.currentPasswordET.error =
                                getString(R.string.authenticate_failed)
                        }
                    })
            }
        }
        dialog.show()
    }

    @SuppressLint("InflateParams")
    private fun showChangeEmailDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_email, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.change_email_address)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val password = dialogView.passwordET.text.toString()
                val newEmail = dialogView.newEmailET.text.toString()
                if (!newEmail.isValidEmail()) {
                    view!!.newEmailET.error = getString(R.string.email_error)
                    return@setOnClickListener
                }

                mViewModel.reAuthenticateUser(password)
                    .observe(viewLifecycleOwner, Observer { authenticateSuccessful ->
                        if (authenticateSuccessful) {
                            mViewModel.updateUserEmail(newEmail)
                                .observe(viewLifecycleOwner, Observer { emailChanged ->
                                    if (emailChanged) {
                                        requireContext().showShortToast(R.string.change_email_successful)
                                        dialog.dismiss()
                                    } else
                                        requireContext().showShortToast(R.string.operation_failed_error)
                                })
                        } else {
                            dialogView.currentPasswordET.error =
                                getString(R.string.authenticate_failed)
                        }
                    })
            }
        }
        dialog.show()
    }

    private fun showLeaveTeamDialog() =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.leave_team_dialog_title)
            .setMessage(R.string.leave_team_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.leave) { dialog, _ ->
                dialog.dismiss()
                leaveTeam()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    private fun leaveTeam() {
        mLoadingDialog.show()
        val isLeader = mCurrentUser.userType == User.USER_TYPE_LEADER
        mViewModel.leaveTeam(isLeader, mCurrentUser.teamId)
            .observe(viewLifecycleOwner, Observer { isLeavingSuccessful ->
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                if (isLeavingSuccessful) {
                    requireContext().showShortToast(R.string.leave_successful)
                    findNavController().navigate(AccountFragmentDirections.showTeamJoinFragment())
                } else {
                    requireContext().showBasicAlertDialog(
                        R.string.leave_failed_title,
                        if (!isLeader) R.string.operation_failed_message else R.string.leave_failed_message_leader
                    )
                }
            })
    }

    @SuppressLint("InflateParams")
    private fun showDeleteAccountDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_account, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.change_email_address)
            .setTitle(R.string.delete_account_dialog_title)
            .setMessage(R.string.delete_account_dialog_message)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.delete), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val password = dialogView.deleteAccountPasswordET.text.toString()

                mViewModel.reAuthenticateUser(password)
                    .observe(viewLifecycleOwner, Observer { authenticateSuccessful ->
                        if (authenticateSuccessful) {
                            dialog.dismiss()
                            deleteAccount()
                        } else {
                            dialogView.currentPasswordET.error =
                                getString(R.string.authenticate_failed)
                        }
                    })
            }
        }
        dialog.show()
    }

    private fun deleteAccount() {
        mLoadingDialog.show()
        val isLeader = mCurrentUser.userType == User.USER_TYPE_LEADER
        mViewModel.leaveTeam(isLeader, mCurrentUser.teamId)
            .observe(viewLifecycleOwner, Observer { isLeavingSuccessful ->
                if (isLeavingSuccessful) {
                    mViewModel.deleteUser().observe(viewLifecycleOwner, Observer { isDeleted ->
                        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                        if (isDeleted) {
                            requireContext().showShortToast(R.string.delete_successful)
                            findNavController().navigate(AccountFragmentDirections.showSignInFragment())
                        } else {
                            requireContext().showBasicAlertDialog(
                                R.string.delete_failed_title,
                                if (!isLeader) R.string.operation_failed_message else R.string.delete_failed_message_leader
                            )
                        }
                    })
                } else {
                    if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                    requireContext().showBasicAlertDialog(
                        R.string.delete_failed_title,
                        if (!isLeader) R.string.operation_failed_message else R.string.delete_failed_message_leader
                    )
                }
            })
    }
}
