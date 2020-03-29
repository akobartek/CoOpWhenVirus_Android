package pl.pomocnawirus.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.content_team_join.view.*
import kotlinx.android.synthetic.main.fragment_team_join.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.utils.showBasicAlertDialog
import pl.pomocnawirus.utils.showShortToast
import pl.pomocnawirus.utils.tryToRunFunctionOnInternet
import pl.pomocnawirus.view.activities.MainActivity
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
            requireActivity().tryToRunFunctionOnInternet({
                mViewModel.addUserToTeam(teamCode)
                    .observe(viewLifecycleOwner, Observer { addedToTeam ->
                        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                        if (addedToTeam) {
                            requireContext().showShortToast(R.string.added_to_team_successful)
                            findNavController().navigate(
                                TeamJoinFragmentDirections.showTaskListFragment()
                            )
                        } else requireContext().showBasicAlertDialog(
                            R.string.team_not_found,
                            R.string.team_code_invalid_error
                        )
                    })
            }, {
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
            })
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    fun createNewTeam(team: Team) {
        mLoadingDialog.show()
        requireActivity().tryToRunFunctionOnInternet({
            mViewModel.createNewTeam(team).observe(viewLifecycleOwner, Observer { teamId ->
                if (teamId.isNotEmpty()) {
                    if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                    requireContext().showShortToast(R.string.team_created_successful)
                    findNavController().navigate(
                        TeamJoinFragmentDirections.showOrdersListFragment()
                    )
                } else requireContext().showBasicAlertDialog(
                    R.string.update_error_title,
                    R.string.update_error_message
                )
            })
        }, {
            if (mLoadingDialog.isShowing) mLoadingDialog.hide()
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
                    (requireActivity() as MainActivity).signOut()
                    true
                }
                else -> true
            }
        }
    }
}
