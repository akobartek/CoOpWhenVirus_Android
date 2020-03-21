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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.User
import pl.pomocnawirus.utils.PreferencesManager
import pl.pomocnawirus.utils.isChromeCustomTabsSupported
import pl.pomocnawirus.view.fragments.*
import pl.pomocnawirus.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mLoadingDialog: AlertDialog
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
                            R.id.safetyFragment -> findNavController(R.id.navHostFragment).navigate(
                                SafetyFragmentDirections.showMapFragment()
                            )
                            R.id.signInFragment -> findNavController(R.id.navHostFragment).navigate(
                                SignInFragmentDirections.showMapFragment()
                            )
                            R.id.teamJoinFragment -> findNavController(R.id.navHostFragment).navigate(
                                TeamJoinFragmentDirections.showMapFragment()
                            )
                            R.id.ordersListFragment -> findNavController(R.id.navHostFragment).navigate(
                                OrdersListFragmentDirections.showMapFragment()
                            )
                            R.id.taskListFragment -> findNavController(R.id.navHostFragment).navigate(
                                TaskListFragmentDirections.showMapFragment()
                            )
                        }
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_safety -> {
                    when (mCurrentFragmentId) {
                        R.id.mapFragment -> findNavController(R.id.navHostFragment).navigate(
                            WebsiteFragmentDirections.showSafetyFragment()
                        )
                        R.id.signInFragment -> findNavController(R.id.navHostFragment).navigate(
                            SignInFragmentDirections.showSafetyFragment()
                        )
                        R.id.teamJoinFragment -> findNavController(R.id.navHostFragment).navigate(
                            TeamJoinFragmentDirections.showSafetyFragment()
                        )
                        R.id.ordersListFragment -> findNavController(R.id.navHostFragment).navigate(
                            OrdersListFragmentDirections.showSafetyFragment()
                        )
                        R.id.taskListFragment -> findNavController(R.id.navHostFragment).navigate(
                            TaskListFragmentDirections.showSafetyFragment()
                        )
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_service -> {
                    if (mAuth.currentUser != null) navigateToCorrectServiceFragment()
                    else when (mCurrentFragmentId) {
                        R.id.mapFragment -> findNavController(R.id.navHostFragment).navigate(
                            WebsiteFragmentDirections.showSignInFragment()
                        )
                        R.id.safetyFragment -> findNavController(R.id.navHostFragment).navigate(
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
        mMainViewModel = ViewModelProvider(this@MainActivity).get(MainViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(this@MainActivity)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        val navController = (navHostFragment as NavHostFragment? ?: return).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mCurrentFragmentId = destination.id
            if (mCurrentFragmentId == R.id.signUpFragment
                || mCurrentFragmentId == R.id.settingsFragment
            ) bottomNavView.visibility = View.GONE
            else bottomNavView.visibility = View.VISIBLE
        }

        mMainViewModel.currentUser.observe(this@MainActivity, Observer { user ->
            if (mLoadingDialog.isShowing) mLoadingDialog.hide()
            if (user != null && bottomNavView.selectedItemId == R.id.navigation_service)
                navigateToCorrectServiceFragment()
        })

        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    override fun onResume() {
        super.onResume()
        bottomNavView.selectedItemId = R.id.navigation_safety
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    fun getCurrentUser(): User? {
        return if (mAuth.currentUser != null) mMainViewModel.currentUser.value
        else null
    }

    fun navigateToCorrectServiceFragment() {
        if (mMainViewModel.currentUser.value == null) {
            mLoadingDialog.show()
            mMainViewModel.fetchUser()
            return
        }
        val isAdmin = mMainViewModel.currentUser.value?.userType == User.USER_TYPE_ADMIN
        if (mMainViewModel.currentUser.value?.teamId?.isEmpty() == true)
            when (mCurrentFragmentId) {
                R.id.safetyFragment -> findNavController(R.id.navHostFragment).navigate(
                    SafetyFragmentDirections.showTeamJoinFragment()
                )
                R.id.mapFragment -> findNavController(R.id.navHostFragment).navigate(
                    WebsiteFragmentDirections.showTeamJoinFragment()
                )
                R.id.signInFragment -> findNavController(R.id.navHostFragment).navigate(
                    SignInFragmentDirections.showTeamJoinFragment()
                )
            }
        else when (mCurrentFragmentId) {
            R.id.safetyFragment -> findNavController(R.id.navHostFragment).navigate(
                if (isAdmin) SafetyFragmentDirections.showOrdersListFragment()
                else SafetyFragmentDirections.showTaskListFragment()
            )
            R.id.mapFragment -> findNavController(R.id.navHostFragment).navigate(
                if (isAdmin) WebsiteFragmentDirections.showOrdersListFragment()
                else WebsiteFragmentDirections.showTaskListFragment()
            )
            R.id.signInFragment -> findNavController(R.id.navHostFragment).navigate(
                if (isAdmin) SignInFragmentDirections.showOrdersListFragment()
                else SignInFragmentDirections.showTaskListFragment()
            )
        }
    }

    override fun onBackPressed() {
        when (mCurrentFragmentId) {
            R.id.signUpFragment ->
                findNavController(R.id.navHostFragment).navigate(SignUpFragmentDirections.showSignInFragment())
            R.id.settingsFragment, R.id.teamFindFragment ->
                findNavController(R.id.navHostFragment).navigateUp()
            else -> doubleBackPressToExit()
        }
    }

    private fun doubleBackPressToExit() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) finish()
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
