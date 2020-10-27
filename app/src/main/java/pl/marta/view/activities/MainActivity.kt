package pl.marta.view.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import pl.marta.R
import pl.marta.model.User
import pl.marta.services.FCMService.Companion.newOrderNotificationAction
import pl.marta.utils.PreferencesManager
import pl.marta.utils.showShortToast
import pl.marta.view.fragments.*
import pl.marta.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        fun subscribeToNewOrdersNotifications(teamId: String) {
            FirebaseMessaging.getInstance().subscribeToTopic("newOrder-$teamId")
        }

        fun unsubscribeFromNewOrdersNotifications(teamId: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("newOrder-$teamId")
        }
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mLoadingDialog: AlertDialog
    private var mCurrentFragmentId: Int? = null
    private var mBackPressed = 0L

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_safety -> {
                    when (mCurrentFragmentId) {
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
                        R.id.accountFragment -> findNavController(R.id.navHostFragment).navigate(
                            AccountFragmentDirections.showSafetyFragment()
                        )
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_service -> {
                    if (mAuth.currentUser != null) navigateToCorrectServiceFragment()
                    else when (mCurrentFragmentId) {
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
                || mCurrentFragmentId == R.id.accountFragment
                || mCurrentFragmentId == R.id.orderEditorFragment
                || mCurrentFragmentId == R.id.teamEditorFragment
                || mCurrentFragmentId == R.id.martasListFragment
                || mCurrentFragmentId == R.id.aboutUsFragment
            ) bottomNavView.visibility = View.GONE
            else bottomNavView.visibility = View.VISIBLE
        }

        mMainViewModel.currentUser.observe(this@MainActivity, { user ->
            if (mLoadingDialog.isShowing) mLoadingDialog.hide()
            if (user != null && bottomNavView.selectedItemId == R.id.navigation_service)
                navigateToCorrectServiceFragment()
        })

        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    override fun onResume() {
        try {
            super.onResume()
            if (intent.extras != null && intent.getBooleanExtra(newOrderNotificationAction, false))
                bottomNavView.selectedItemId = R.id.navigation_service
        } catch (t: Throwable) {
            bottomNavView.selectedItemId = R.id.navigation_safety
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    fun getCurrentUser(): User? = mMainViewModel.currentUser.value

    fun signOut() {
        unsubscribeFromNewOrdersNotifications(mMainViewModel.currentUser.value!!.teamId)
        mMainViewModel.unregisterUserListener()
        mMainViewModel.currentUser.postValue(null)
        mAuth.signOut()
        bottomNavView.visibility = View.VISIBLE
        bottomNavView.selectedItemId = R.id.navigation_safety
    }

    fun navigateToCorrectServiceFragment() {
        if (mAuth.currentUser == null) return
        else if (mMainViewModel.currentUser.value == null) {
            mLoadingDialog.show()
            mMainViewModel.fetchUser()
            return
        }
        if (mMainViewModel.currentUser.value?.teamId?.isEmpty() == true)
            when (mCurrentFragmentId) {
                R.id.safetyFragment -> findNavController(R.id.navHostFragment).navigate(
                    SafetyFragmentDirections.showTeamJoinFragment()
                )
                R.id.signInFragment -> findNavController(R.id.navHostFragment).navigate(
                    SignInFragmentDirections.showTeamJoinFragment()
                )
            }
        else {
            val teamId = mMainViewModel.currentUser.value!!.teamId
            val isLeader = mMainViewModel.currentUser.value?.userType == User.USER_TYPE_LEADER
            subscribeToNewOrdersNotifications(teamId)
            when (mCurrentFragmentId) {
                R.id.safetyFragment -> findNavController(R.id.navHostFragment).navigate(
                    if (isLeader) SafetyFragmentDirections.showOrdersListFragment(teamId)
                    else SafetyFragmentDirections.showTaskListFragment(teamId)
                )
                R.id.signInFragment -> findNavController(R.id.navHostFragment).navigate(
                    if (isLeader) SignInFragmentDirections.showOrdersListFragment(teamId)
                    else SignInFragmentDirections.showTaskListFragment(teamId)
                )
            }
        }
    }

    override fun onBackPressed() {
        when (mCurrentFragmentId) {
            R.id.signUpFragment ->
                findNavController(R.id.navHostFragment).navigate(SignUpFragmentDirections.showSignInFragment())
            R.id.settingsFragment, R.id.accountFragment, R.id.aboutUsFragment, R.id.martasListFragment ->
                findNavController(R.id.navHostFragment).navigateUp()
            R.id.teamFindFragment ->
                if ((supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                        .childFragmentManager.fragments[0] as TeamFindFragment).onBackPressed()
                ) findNavController(R.id.navHostFragment).navigateUp()
            R.id.orderEditorFragment ->
                (supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                    .childFragmentManager.fragments[0] as OrderEditorFragment).onBackPressed()
            R.id.teamEditorFragment ->
                (supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                    .childFragmentManager.fragments[0] as TeamEditorFragment).onBackPressed()
            else -> doubleBackPressToExit()
        }
    }

    private fun doubleBackPressToExit() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) finish()
        baseContext.showShortToast(R.string.press_to_exit)
        mBackPressed = System.currentTimeMillis()
    }
}
