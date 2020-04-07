package pl.marta.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.content_team_editor.view.*
import kotlinx.android.synthetic.main.fragment_team_editor.view.*
import kotlinx.coroutines.*
import pl.marta.R
import pl.marta.model.Team
import pl.marta.utils.*
import pl.marta.view.activities.MainActivity
import pl.marta.viewmodel.TeamEditorViewModel

class TeamEditorFragment : Fragment() {

    companion object {
        var teamChanged = false
    }

    private lateinit var mViewModel: TeamEditorViewModel
    private lateinit var mLoadingDialog: AlertDialog
    private lateinit var mJob: Job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        teamChanged = false
        return inflater.inflate(R.layout.fragment_team_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

        mViewModel = ViewModelProvider(requireActivity()).get(TeamEditorViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        mLoadingDialog.show()
        mViewModel.fetchTeam((requireActivity() as MainActivity).getCurrentUser()!!.teamId)

        mJob = GlobalScope.async(Dispatchers.IO) {
            delay(15000)
            if (mViewModel.team.value == null) {
                requireActivity().tryToRunFunctionOnInternet({}, {
                    mLoadingDialog.hide()
                    findNavController().navigateUp()
                })
            }
        }

        mViewModel.team.observe(viewLifecycleOwner, Observer { team ->
            if (mJob.isActive) mJob.cancel()
            if (mLoadingDialog.isShowing) mLoadingDialog.hide()

            if (team != null) {
                view.teamNameET.setText(team.name)
                view.teamCityET.setText(team.city)
                view.teamPhoneET.setText(team.phone)
                view.teamEmailET.setText(team.email)
            }
        })

        view.showTeamMembersBtn.setOnClickListener {
            val teamMembersBottomSheet = TeamMembersBottomSheetFragment(this::showInviteDialog)
            teamMembersBottomSheet.show(childFragmentManager, teamMembersBottomSheet.tag)
        }

        view.teamEditorLayout.setOnClickListener {
            (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }
        view.teamNameInputLayout.markRequiredInRed()
        view.teamCityInputLayout.markRequiredInRed()
        view.teamPhoneInputLayout.markRequiredInRed()
        view.teamNameET.setOnTouchListener(mTouchListener)
        view.teamCityET.setOnTouchListener(mTouchListener)
        view.teamPhoneET.setOnTouchListener(mTouchListener)
        view.teamEmailET.setOnTouchListener(mTouchListener)
    }

    override fun onStop() {
        super.onStop()
        if (mJob.isActive) mJob.cancel()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    fun onBackPressed() {
        if (teamChanged) requireContext().showUnsavedChangesDialog { findNavController().navigateUp() }
        else findNavController().navigateUp()
    }

    private fun inflateToolbarMenu() {
        view?.teamEditorToolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        view?.teamEditorToolbar?.setNavigationOnClickListener { onBackPressed() }
        view?.teamEditorToolbar?.inflateMenu(R.menu.team_editor_menu)
        view?.teamEditorToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save_team -> {
                    saveTeam()
                    true
                }
                R.id.action_invite_to_team -> {
                    showInviteDialog(mViewModel.team.value!!.id)
                    true
                }
                else -> true
            }
        }
    }

    private fun showInviteDialog(teamId: String) =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.invite_to_team_title)
            .setMessage(getString(R.string.invite_to_team_message, teamId))
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setNeutralButton(R.string.copy_to_clipboard) { dialog, _ ->
                requireActivity().copyToClipboard("Invite_code", teamId)
                requireContext().showShortToast(R.string.copied_to_clipboard)
                dialog.dismiss()
            }
            .create()
            .show()

    private fun saveTeam() {
        val teamName = view?.teamNameET?.text.toString().trim()
        val city = view?.teamCityET?.text.toString().trim()
        val phoneNumber = view?.teamPhoneET?.text.toString().trim()
        val email = view?.teamEmailET?.text.toString().trim()

        if (!areValuesValid(teamName, city, phoneNumber, email)) return

        val team = Team(
            mViewModel.team.value!!.id, teamName, city,
            mViewModel.team.value!!.leaders, email, phoneNumber
        )
        mViewModel.updateTeam(team)
    }

    private fun areValuesValid(name: String, city: String, phone: String, email: String?): Boolean {
        var isValid = true

        if (name.length < 3) {
            view!!.teamNameET.error = getString(R.string.name_error_too_short)
            isValid = false
        }
        if (city.isEmpty()) {
            view!!.teamCityET.error = getString(R.string.city_error_empty)
            isValid = false
        }
        if (phone.isEmpty()) {
            view!!.teamPhoneET.error = getString(R.string.phone_error_empty)
            isValid = false
        } else if (!phone.isValidPhoneNumber()) {
            view!!.teamPhoneET.error = getString(R.string.phone_error_incorrect)
            isValid = false
        }
        if (!email.isNullOrEmpty() && !email.isValidEmail()) {
            view!!.teamEmailET.error = getString(R.string.email_error)
            isValid = false
        }
        return isValid
    }

    private val mTouchListener = View.OnTouchListener { _, _ ->
        teamChanged = true
        false
    }
}
