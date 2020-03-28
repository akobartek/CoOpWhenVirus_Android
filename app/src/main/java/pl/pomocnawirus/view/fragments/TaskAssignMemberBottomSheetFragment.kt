package pl.pomocnawirus.view.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_members_list.view.*
import kotlinx.android.synthetic.main.fragment_task_assign_member_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.User
import pl.pomocnawirus.utils.setLayoutFullHeight
import pl.pomocnawirus.view.activities.MainActivity
import pl.pomocnawirus.view.adapters.TaskAssignMemberRecyclerAdapter
import pl.pomocnawirus.viewmodel.OrderEditorViewModel

class TaskAssignMemberBottomSheetFragment(private val selectUser: (User?) -> Unit) :
    BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mViewModel: OrderEditorViewModel
    private lateinit var mAdapter: TaskAssignMemberRecyclerAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val view = View.inflate(requireContext(), R.layout.fragment_team_members_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        mViewModel = ViewModelProvider(requireActivity()).get(OrderEditorViewModel::class.java)
        mAdapter = TaskAssignMemberRecyclerAdapter() { user ->
            dismiss()
            selectUser(user)
        }
        view.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        mViewModel.fetchTeamMembers((requireActivity() as MainActivity).getCurrentUser()!!.teamId)
        mViewModel.teamMembers.observe(viewLifecycleOwner, Observer { members ->
            mAdapter.setMembersList(members)
            view.membersRecyclerView.scheduleLayoutAnimation()
            view.membersLoadingIndicator.hide()
        })

        view.toolbarCancelBtn.setOnClickListener {
            dismiss()
            selectUser(null)
        }

        return bottomSheetDialog
    }
}
