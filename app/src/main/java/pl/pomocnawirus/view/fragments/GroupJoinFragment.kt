package pl.pomocnawirus.view.fragments

import android.os.Bundle
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
import kotlinx.android.synthetic.main.content_group_join.view.*
import kotlinx.android.synthetic.main.fragment_group_join.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Group
import pl.pomocnawirus.utils.showBasicAlertDialog
import pl.pomocnawirus.view.activities.MainActivity
import pl.pomocnawirus.viewmodel.GroupJoinViewModel

class GroupJoinFragment : Fragment() {

    private lateinit var mViewModel: GroupJoinViewModel
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_group_join, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

        mViewModel = ViewModelProvider(requireActivity()).get(GroupJoinViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        view.browseGroupsBtn.setOnClickListener {
            findNavController().navigate(GroupJoinFragmentDirections.showGroupFindFragment())
        }

        view.createGroupBtn.setOnClickListener {
            val createGroupBottomSheet = GroupCreateBottomSheetFragment(this@GroupJoinFragment)
            createGroupBottomSheet.show(childFragmentManager, createGroupBottomSheet.tag)
        }

        view.joinGroupBtn.setOnClickListener {
            val groupCode = view.groupCodeET.text.toString().trim()
            if (groupCode.isEmpty()) {
                view.groupCodeET.error = getString(R.string.group_code_empty_error)
                view.groupCodeET.requestFocus()
                return@setOnClickListener
            }
            mLoadingDialog.show()
            mViewModel.checkIfGroupExists(groupCode)
                .observe(viewLifecycleOwner, Observer { groupExists ->
                    if (groupExists) {
                        val currentUser = (requireActivity() as MainActivity).getCurrentUser()
                        currentUser?.let { user ->
                            user.groupId = groupCode
                            mViewModel.updateUser(currentUser)
                                .observe(viewLifecycleOwner, Observer { userUpdated ->
                                    if (userUpdated) {
                                        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.added_to_group_successful,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().navigate(GroupJoinFragmentDirections.showTaskListFragment())
                                    } else requireContext().showBasicAlertDialog(
                                        R.string.update_error_title,
                                        R.string.update_error_message
                                    )
                                })
                        }
                    } else requireContext().showBasicAlertDialog(
                        R.string.group_not_found,
                        R.string.group_code_invalid_error
                    )
                })
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    fun createNewGroup(group: Group) {
        mLoadingDialog.show()
        mViewModel.createNewGroup(group).observe(viewLifecycleOwner, Observer { groupCreated ->
            if (groupCreated) {
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
                Toast.makeText(
                    requireContext(),
                    R.string.group_created_successful,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(GroupJoinFragmentDirections.showOrdersListFragment())
            } else requireContext().showBasicAlertDialog(
                R.string.update_error_title,
                R.string.update_error_message
            )
        })
    }

    private fun inflateToolbarMenu() {
        view?.groupJoinToolbar?.inflateMenu(R.menu.basic_menu)
        view?.groupJoinToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(GroupJoinFragmentDirections.showSettingsFragment())
                    true
                }
                R.id.action_sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(GroupJoinFragmentDirections.showSafetyFragment())
                    true
                }
                else -> true
            }
        }
    }
}
