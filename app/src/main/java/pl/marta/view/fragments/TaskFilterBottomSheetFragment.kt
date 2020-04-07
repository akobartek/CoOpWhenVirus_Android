package pl.marta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_task_filter_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_task_filter_bottom_sheet.view.*
import pl.marta.R
import pl.marta.model.Task
import pl.marta.utils.Filters
import pl.marta.utils.collapse
import pl.marta.utils.expand
import pl.marta.utils.setLayoutFullHeight
import pl.marta.view.adapters.FilterRecyclerAdapter
import pl.marta.viewmodel.OrdersViewModel
import pl.marta.viewmodel.TasksViewModel

class TaskFilterBottomSheetFragment(
    val mViewModel: AndroidViewModel, private val isLeader: Boolean
) : BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mAdapter: FilterRecyclerAdapter
    private lateinit var mFilters: Filters

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
        return inflater.inflate(R.layout.fragment_task_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFilters =
            if (isLeader) (mViewModel as OrdersViewModel).filters.value ?: Filters()
            else (mViewModel as TasksViewModel).filters.value ?: Filters()
        view.taskStatusActiveTasks.isChecked = mFilters.selectedTaskStatus == Task.TASK_STATUS_ADDED
        view.taskStatusMyTasks.isChecked = mFilters.selectedTaskStatus != Task.TASK_STATUS_ADDED

        mAdapter = FilterRecyclerAdapter(mFilters.selectedTaskTypes)
        view.filterTaskTypeOptions.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }

        view.taskStatusActiveTasks.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mFilters.selectedTaskStatus = Task.TASK_STATUS_ADDED
                view.taskStatusMyTasks.isChecked = false
            } else if (!view.taskStatusMyTasks.isChecked)
                view.taskStatusMyTasks.isChecked = true
        }
        view.taskStatusMyTasks.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mFilters.selectedTaskStatus = Task.TASK_STATUS_ACCEPTED
                view.taskStatusActiveTasks.isChecked = false
            } else if (!view.taskStatusActiveTasks.isChecked)
                view.taskStatusActiveTasks.isChecked = true
        }

        val expandFilterStatus = View.OnClickListener {
            if (view.filterTaskStatusOptions.visibility == View.GONE) {
                view.filterTaskStatusOptions.expand()
                view.filterTaskStatusBtn.animate().rotation(180f).duration = 300
            } else {
                view.filterTaskStatusOptions.collapse()
                view.filterTaskStatusBtn.animate().rotation(0f).duration = 300
            }
        }
        view.filterTaskStatusHeader.setOnClickListener(expandFilterStatus)
        view.filterTaskStatusBtn.setOnClickListener(expandFilterStatus)
        val expandFilterType = View.OnClickListener {
            if (view.filterTaskTypeOptions.visibility == View.GONE) {
                view.filterTaskTypeOptions.expand()
                view.filterTaskTypeBtn.animate().rotation(180f).duration = 300
            } else {
                view.filterTaskTypeOptions.collapse()
                view.filterTaskTypeBtn.animate().rotation(0f).duration = 300
            }
        }
        view.filterTaskTypeHeader.setOnClickListener(expandFilterType)
        view.filterTaskTypeBtn.setOnClickListener(expandFilterType)

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarSaveFilterBtn.setOnClickListener {
            mFilters.selectedTaskTypes = mAdapter.taskTypes
            if (isLeader) (mViewModel as OrdersViewModel).filters.postValue(mFilters)
            else (mViewModel as TasksViewModel).filters.postValue(mFilters)
            dismiss()
        }
    }
}
