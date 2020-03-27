package pl.pomocnawirus.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.User
import pl.pomocnawirus.utils.FirestoreUtils.firestoreCollectionUsers
import pl.pomocnawirus.utils.isValidEmail
import pl.pomocnawirus.utils.isValidPassword
import pl.pomocnawirus.utils.isValidPhoneNumber
import pl.pomocnawirus.utils.tryToRunFunctionOnInternet

class SignUpFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sign_up, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.signUpToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.signUpToolbar.setNavigationOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.showSignInFragment())
        }
        view.emailET.markRequiredInRed()
        view.passwordET.markRequiredInRed()
        view.nameET.markRequiredInRed()

        mAuth = FirebaseAuth.getInstance()

        view.signUpBtn.setOnClickListener {
            view.signUpBtn.isEnabled = false
            val email = view.emailET.text.toString().trim()
            val password = view.passwordET.text.toString().trim()
            val name = view.nameET.text.toString().trim()
            val phoneNumber = view.phoneET.text.toString().trim()

            if (!areValuesValid(email, password, name, phoneNumber)) {
                view.signUpBtn.isEnabled = true
                return@setOnClickListener
            }

            requireActivity().tryToRunFunctionOnInternet({
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity!!) { task ->
                        if (task.isSuccessful) {
                            mAuth.useAppLanguage()
                            mAuth.currentUser?.sendEmailVerification()
                            val user = User("", email, name, User.USER_TYPE_USER, phoneNumber, "")

                            FirebaseFirestore.getInstance().collection(firestoreCollectionUsers)
                                .document(mAuth.currentUser!!.uid)
                                .set(user.createUserHashMap())

                            mAuth.signOut()
                            showSignUpSuccessfulDialog()
                        } else {
                            Log.d("SignUpFailed", task.exception!!.toString())
                            if (task.exception!! is FirebaseAuthUserCollisionException) {
                                view.signUpBtn.isEnabled = true
                                view.emailET.error = getString(R.string.sign_up_existing_user_error)
                                view.emailET.requestFocus()
                            } else {
                                view.signUpBtn.isEnabled = true
                                Snackbar.make(
                                    view.signUpLayout,
                                    R.string.sign_up_error,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }, { view.signUpBtn.isEnabled = true })
        }
        view.signUpLayout.setOnClickListener {
            (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }
    }

    private fun showSignUpSuccessfulDialog() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.sign_up_successful_dialog_title)
            .setMessage(R.string.sign_up_successful_dialog_message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog?.dismiss()
                findNavController().navigate(SignUpFragmentDirections.showSignInFragment())
            }
            .create()
            .show()
    }

    private fun areValuesValid(email: String, pass: String, name: String, phone: String): Boolean {
        var isValid = true

        if (!email.isValidEmail()) {
            view!!.emailET.error = getString(R.string.email_error)
            isValid = false
        }
        if (pass.length < 6) {
            view!!.passwordET.error = getString(R.string.password_error_too_short)
            isValid = false
        } else if (!pass.isValidPassword()) {
            view!!.passwordET.error = getString(R.string.password_error_wrong)
            isValid = false
        }
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
}
