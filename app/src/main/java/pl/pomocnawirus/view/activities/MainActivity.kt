package pl.pomocnawirus.view.activities

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import pl.pomocnawirus.R
import pl.pomocnawirus.utils.PreferencesManager
import pl.pomocnawirus.utils.isChromeCustomTabsSupported
import pl.pomocnawirus.view.fragments.SafetyFragmentDirections
import pl.pomocnawirus.view.fragments.SignInFragmentDirections
import pl.pomocnawirus.view.fragments.SignUpFragmentDirections
import pl.pomocnawirus.view.fragments.WebsiteFragmentDirections

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var mCurrentFragmentId: Int? = null
    private var mBackPressed = 0L

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_map -> {
                    if (isChromeCustomTabsSupported()) {
                        CustomTabsIntent.Builder().apply {
                            val color =
                                if (PreferencesManager.getNightMode()) Color.parseColor("#28292e")
                                else Color.WHITE
                            setToolbarColor(color)
                            setSecondaryToolbarColor(color)
                        }.build().launchUrl(this@MainActivity, Uri.parse("https://korona.ws/"))
                    } else {
                        when (mCurrentFragmentId) {
                            R.id.navigation_safety ->
                                findNavController(R.id.navHostFragment).navigate(
                                    SafetyFragmentDirections.showMapFragment()
                                )
                            R.id.navigation_service ->
                                findNavController(R.id.navHostFragment).navigate(
                                    SignInFragmentDirections.showMapFragment()
                                )
                        }
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_safety -> {
                    when (mCurrentFragmentId) {
                        R.id.mapFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                WebsiteFragmentDirections.showSafetyFragment()
                            )
                        R.id.signInFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                SignInFragmentDirections.showSafetyFragment()
                            )
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_service -> {
                    // TODO() -> If signed in show different fragment
                    when (mCurrentFragmentId) {
                        R.id.mapFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                WebsiteFragmentDirections.showSignInFragment()
                            )
                        R.id.safetyFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                SafetyFragmentDirections.showSignInFragment()
                            )
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferencesManager.getNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            window.decorView.systemUiVisibility = 0
        } else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PreferencesManager.getNightMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }

        mAuth = FirebaseAuth.getInstance()
        val navController = (navHostFragment as NavHostFragment? ?: return).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mCurrentFragmentId = destination.id
            if (mCurrentFragmentId == R.id.signUpFragment
            ) bottomNavView.visibility = View.GONE
            else bottomNavView.visibility = View.VISIBLE
        }

        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    override fun onResume() {
        super.onResume()
        bottomNavView.selectedItemId = R.id.navigation_safety
    }

    override fun onBackPressed() {
        when (mCurrentFragmentId) {
            R.id.signUpFragment ->
                findNavController(R.id.navHostFragment).navigate(SignUpFragmentDirections.showSignInFragment())
            else -> doubleBackPressToExit()
        }
    }

    private fun doubleBackPressToExit() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) super.onBackPressed()
        else Toast.makeText(
            baseContext,
            getString(R.string.press_to_exit),
            Toast.LENGTH_SHORT
        ).show()
        mBackPressed = System.currentTimeMillis()
    }

    private fun showUnsavedChangesDialog(discardAction: () -> Unit) =
        AlertDialog.Builder(this@MainActivity)
            .setMessage(R.string.unsaved_changes_dialog_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.discard) { dialog, _ ->
                dialog.dismiss()
                discardAction()
            }
            .setNegativeButton(R.string.keep_editing) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
}
