package pl.pomocnawirus.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import pl.pomocnawirus.R
import java.text.SimpleDateFormat
import java.util.*

fun Context.isChromeCustomTabsSupported(): Boolean {
    val serviceIntent = Intent("android.support.customtabs.action.CustomTabsService")
    serviceIntent.setPackage("com.android.chrome")
    val resolveInfos = packageManager.queryIntentServices(serviceIntent, 0)
    return resolveInfos.isNotEmpty()
}

fun String.createUnderlinedString(): SpannableString {
    val spannable = SpannableString(this)
    spannable.setSpan(UnderlineSpan(), 0, this.length, 0)
    return spannable
}

fun Activity.showNoInternetDialogWithTryAgain(
    function: () -> Unit,
    functionCancel: () -> Unit
): Unit =
    AlertDialog.Builder(this)
        .setTitle(R.string.no_internet_title)
        .setMessage(R.string.no_internet_reconnect_message)
        .setCancelable(false)
        .setPositiveButton(R.string.try_again) { dialog, _ ->
            dialog.dismiss()
            if (checkNetworkConnection()) function()
            else if (!isFinishing && !isDestroyed)
                showNoInternetDialogWithTryAgain(function, functionCancel)
        }
        .setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
            functionCancel()
        }
        .create()
        .show()

@Suppress("DEPRECATION")
fun Activity.checkNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities != null
    } else {
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun Activity.tryToRunFunctionOnInternet(function: () -> Unit, functionCancel: () -> Unit) {
    if (checkNetworkConnection())
        try {
            function()
        } catch (exc: Throwable) {
            showNoInternetDialogWithTryAgain(function, functionCancel)
        }
    else showNoInternetDialogWithTryAgain(function, functionCancel)
}

fun EditText.enable() {
    isFocusable = true
    isEnabled = true
    isFocusableInTouchMode = true
}

fun EditText.disable() {
    isFocusable = false
    isEnabled = false
    isFocusableInTouchMode = false
}

fun Date.format(): String {
    val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return simpleDateFormat.format(this)
}

fun Context.showBasicAlertDialog(titleId: Int, messageId: Int) {
    AlertDialog.Builder(this)
        .setTitle(titleId)
        .setMessage(messageId)
        .setCancelable(false)
        .setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPhoneNumber(): Boolean =
    android.util.Patterns.PHONE.matcher(this).matches()