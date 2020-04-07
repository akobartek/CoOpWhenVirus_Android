package pl.marta.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_members_list.view.*
import kotlinx.android.synthetic.main.dialog_contact_details.view.*
import kotlinx.android.synthetic.main.fragment_team_members_bottom_sheet.view.*
import pl.marta.R
import pl.marta.model.User
import pl.marta.utils.copyToClipboard
import pl.marta.utils.setLayoutFullHeight
import pl.marta.utils.showShortToast
import pl.marta.utils.tryToRunFunctionOnInternet
import pl.marta.view.adapters.MembersRecyclerAdapter
import pl.marta.viewmodel.TeamEditorViewModel

class TeamMembersBottomSheetFragment(val showInviteDialog: (String) -> Unit) :
    BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mViewModel: TeamEditorViewModel
    private lateinit var mAdapter: MembersRecyclerAdapter
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return inflater.inflate(R.layout.fragment_team_members_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(TeamEditorViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        mAdapter = MembersRecyclerAdapter(this::showPopup)
        view.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        mViewModel.fetchTeamMembers()
        mViewModel.teamMembers.observe(viewLifecycleOwner, Observer { members ->
            val list = members.filter { it.id != FirebaseAuth.getInstance().currentUser!!.uid }
            mAdapter.setMembersList(list)
            view.membersRecyclerView.scheduleLayoutAnimation()
            view.membersLoadingIndicator.hide()
            if (list.isEmpty()) {
                view.emptyMembersView.visibility = View.VISIBLE
            } else {
                view.emptyMembersView.visibility = View.INVISIBLE
            }
        })

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarInviteBtn.setOnClickListener { showInviteDialog(mViewModel.team.value!!.id) }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    private fun showPopup(view: View, member: User) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.member_popup_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.action_make_leader).isVisible =
            member.userType == User.USER_TYPE_USER
        popupMenu.menu.findItem(R.id.action_remove_leader).isVisible =
            member.userType == User.USER_TYPE_LEADER
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_contact_details -> {
                    showContactDetailsDialog(member)
                    true
                }
                R.id.action_make_leader -> {
                    showChangeUserTypeDialog(member, true)
                    true
                }
                R.id.action_remove_leader -> {
                    showChangeUserTypeDialog(member, false)
                    true
                }
                R.id.action_remove_member -> {
                    showRemoveMemberDialog(member)
                    true
                }
                else -> true
            }
        }
        popupMenu.show()
    }

    @SuppressLint("InflateParams")
    private fun showContactDetailsDialog(member: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_contact_details, null)
        dialogView.memberEmailTV.text = member.email
        dialogView.memberPhoneTV.text = member.phone
        if (member.phone.isEmpty()) dialogView.memberPhoneTV.visibility = View.GONE
        dialogView.memberEmailTV.setOnClickListener {
            requireActivity().copyToClipboard("member_email", member.email)
            requireContext().showShortToast(R.string.email_copied)
        }
        dialogView.memberPhoneTV.setOnClickListener {
            requireActivity().copyToClipboard("member_phone", member.phone)
            requireContext().showShortToast(R.string.phone_copied)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(member.name)
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()
    }

    private fun showChangeUserTypeDialog(member: User, willBeLeader: Boolean) =
        AlertDialog.Builder(requireContext())
            .setMessage(if (willBeLeader) R.string.make_leader_message else R.string.remove_leader_message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog?.dismiss()
                changeUserType(member, willBeLeader)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()

    private fun changeUserType(member: User, willBeLeader: Boolean) {
        mLoadingDialog.show()
        requireActivity().tryToRunFunctionOnInternet({
            if (willBeLeader)
                mViewModel.makeNewLeader(member)
                    .observe(viewLifecycleOwner, Observer { isChangeSuccessful ->
                        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                        if (isChangeSuccessful)
                            updateAdapterListAfterUserTypeChange(member, willBeLeader)
                        else requireContext().showShortToast(R.string.operation_failed_message)
                    })
            else
                mViewModel.removeLeaderRole(member)
                    .observe(viewLifecycleOwner, Observer { isChangeSuccessful ->
                        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                        if (isChangeSuccessful)
                            updateAdapterListAfterUserTypeChange(member, willBeLeader)
                        else requireContext().showShortToast(R.string.operation_failed_message)
                    })
        }, {
            mLoadingDialog.hide()
            requireContext().showShortToast(R.string.operation_failed_message)
        })
    }

    private fun updateAdapterListAfterUserTypeChange(member: User, willBeLeader: Boolean) {
        val list = mAdapter.getMembersList()
        list[list.indexOfFirst { it.id == member.id }].userType =
            if (willBeLeader) User.USER_TYPE_LEADER else User.USER_TYPE_USER
        mAdapter.setMembersList(list)
    }

    private fun showRemoveMemberDialog(member: User) =
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.remove_team_member_message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog?.dismiss()
                removeMember(member)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()

    private fun removeMember(member: User) {
        mLoadingDialog.show()
        requireActivity().tryToRunFunctionOnInternet({
            mViewModel.removeUserFromTeam(member)
                .observe(viewLifecycleOwner, Observer { isRemovingSuccessful ->
                    if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                    if (isRemovingSuccessful) {
                        val list = arrayListOf<User>()
                        list.addAll(mAdapter.getMembersList())
                        list.remove(member)
                        mAdapter.setMembersList(list)
                    } else requireContext().showShortToast(R.string.operation_failed_message)
                })
        }, {
            mLoadingDialog.hide()
            requireContext().showShortToast(R.string.operation_failed_message)
        })
    }
}
