package pl.pomocnawirus.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_team_join.view.*
import kotlinx.android.synthetic.main.fragment_team_join.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.utils.showBasicAlertDialog
import pl.pomocnawirus.viewmodel.TeamsViewModel

class TeamJoinFragment : Fragment() {

    private lateinit var mViewModel: TeamsViewModel
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team_join, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

        mViewModel = ViewModelProvider(requireActivity()).get(TeamsViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        view.browseTeamsBtn.setOnClickListener {
            findNavController().navigate(TeamJoinFragmentDirections.showTeamFindFragment())
        }

        view.createTeamBtn.setOnClickListener {
            val createTeamBottomSheet = TeamCreateBottomSheetFragment(this@TeamJoinFragment)
            createTeamBottomSheet.show(childFragmentManager, createTeamBottomSheet.tag)
        }

        view.joinTeamBtn.setOnClickListener {
            val teamCode = view.teamCodeET.text.toString().trim()
            if (teamCode.isEmpty()) {
                view.teamCodeET.error = getString(R.string.team_code_empty_error)
                view.teamCodeET.requestFocus()
                return@setOnClickListener
            }
            mLoadingDialog.show()
            mViewModel.addUserToTeam(teamCode)
                .observe(viewLifecycleOwner, Observer { addedToTeam ->
                    if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                    Log.d("WTF", addedToTeam.toString())
                    if (addedToTeam) {
                        Toast.makeText(
                            requireContext(),
                            R.string.added_to_team_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(TeamJoinFragmentDirections.showTaskListFragment())
                    } else requireContext().showBasicAlertDialog(
                        R.string.team_not_found,
                        R.string.team_code_invalid_error
                    )
                })
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    fun createNewTeam(team: Team) {
        mLoadingDialog.show()
        mViewModel.createNewTeam(team).observe(viewLifecycleOwner, Observer { teamCreated ->
            if (teamCreated) {
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                Toast.makeText(
                    requireContext(),
                    R.string.team_created_successful,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(TeamJoinFragmentDirections.showOrdersListFragment())
            } else requireContext().showBasicAlertDialog(
                R.string.update_error_title,
                R.string.update_error_message
            )
        })
    }

    private fun inflateToolbarMenu() {
        view?.teamJoinToolbar?.inflateMenu(R.menu.basic_menu)
        view?.teamJoinToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(TeamJoinFragmentDirections.showSettingsFragment())
                    true
                }
                R.id.action_sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(TeamJoinFragmentDirections.showSafetyFragment())
                    true
                }
                else -> true
            }
        }
    }
}
