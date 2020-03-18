package pl.pomocnawirus.view.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.dialog_reset_password.view.*
import kotlinx.android.synthetic.main.fragment_sign_in.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.utils.createUnderlinedString

class SignInFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sign_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        view.forgotPasswordTV.text = view.forgotPasswordTV.text.toString().createUnderlinedString()
        view.createAccountTV.text = view.createAccountTV.text.toString().createUnderlinedString()

        view.forgotPasswordTV?.setOnClickListener {
            showResetPasswordDialog()
        }
        view.createAccountTV.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.showSignUpFragment())
        }
        view.signInBtn.setOnClickListener {
            view.signInBtn.isEnabled = false
            val email = view.emailET.text.toString().trim()
            val password = view.passwordET.text.toString().trim()

            if (isEmailOrPasswordNull(email, password)) {
                view.signInBtn.isEnabled = true
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        if (!mAuth.currentUser!!.isEmailVerified) {
                            showVerifyEmailDialog()
                            mAuth.signOut()
                            view.signInBtn.isEnabled = true
                        } else {
                            Toast.makeText(context, R.string.signed_in, Toast.LENGTH_SHORT).show()
                            // TODO() -> Navigate to account fragment
//                            findNavController().navigateUp()
                        }
                    } else {
                        Log.d("SignInFailed", task.exception.toString())
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                view.emailET.error = getString(R.string.sign_in_no_user_error)
                                view.emailET.requestFocus()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                view.passwordET.error =
                                    getString(R.string.sign_in_wrong_password_error)
                                view.passwordET.requestFocus()
                            }
                            else -> Snackbar.make(
                                view.signInLayout,
                                R.string.sign_in_error,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        view.signInBtn.isEnabled = true
                    }
                }
        }
        view.signInLayout.setOnClickListener {
            (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }
    }

    private fun isEmailOrPasswordNull(email: String, password: String): Boolean {
        var isNull = false

        if (!isEmailValid(email)) {
            view!!.emailET.error = getString(R.string.email_error)
            isNull = true
        }
        if (password.isEmpty()) {
            view!!.passwordET.error = getString(R.string.password_error_empty)
            isNull = true
        }
        return isNull
    }

    private fun isEmailValid(email: CharSequence): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun showVerifyEmailDialog() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.verify_email_dialog_title)
            .setMessage(R.string.verify_email_dialog_message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog?.dismiss()
            }
            .setNeutralButton(getString(R.string.verify_email_send_again)) { dialog, _ ->
                dialog.dismiss()
                mAuth.currentUser?.sendEmailVerification()
                Toast.makeText(context!!, getString(R.string.message_sent), Toast.LENGTH_SHORT)
                    .show()
            }
            .create()
            .show()
    }

    @SuppressLint("InflateParams")
    private fun showResetPasswordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reset_password, null)

        val dialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.reset_password_dialog_title)
            .setMessage(R.string.reset_password_dialog_message)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.send), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val email = dialogView.resetPasswordET.text.toString().trim()
                if (!isEmailValid(email)) {
                    dialogView.resetPasswordET.error = getString(R.string.email_error)
                    return@setOnClickListener
                } else {
                    mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                                Snackbar.make(
                                    view!!.signInLayout,
                                    R.string.message_sent,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            } else {
                                dialogView.resetPasswordET.error =
                                    getString(R.string.reset_password_error)
                            }
                        }
                }
            }
        }
        dialog.show()
    }
}
