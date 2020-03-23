package pl.pomocnawirus.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_task_list.view.*
import kotlinx.android.synthetic.main.fragment_task_list.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.view.activities.MainActivity
import pl.pomocnawirus.view.adapters.TasksRecyclerAdapter
import pl.pomocnawirus.viewmodel.MainViewModel
import pl.pomocnawirus.viewmodel.TasksViewModel

class TaskListFragment : Fragment() {

    private lateinit var mViewModel: TasksViewModel
    private lateinit var mAdapter: TasksRecyclerAdapter
    private var mShowMyTasks = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_task_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

        mAdapter = TasksRecyclerAdapter() {
            // TODO() -> Open bottom sheet with task information
//            val createTeamBottomSheet = TeamCreateBottomSheetFragment(this@TaskListFragment)
//            createTeamBottomSheet.show(childFragmentManager, createTeamBottomSheet.tag)
        }
        view.tasksRecyclerView.layoutManager = LinearLayoutManager(view.context)
        view.tasksRecyclerView.itemAnimator = DefaultItemAnimator()
        view.tasksRecyclerView.adapter = mAdapter

        mViewModel = ViewModelProvider(requireActivity()).get(TasksViewModel::class.java)
        val teamId = arguments?.let { TaskListFragmentArgs.fromBundle(it).teamId }
        if (teamId == null)
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java).currentUser.value?.teamId
        mViewModel.fetchTeam(teamId!!)
        mViewModel.team.observe(viewLifecycleOwner, Observer { showTasks() })
    }

    private fun showTasks() {
        val tasksToShow = arrayListOf<Task>()
        mViewModel.team.value?.orders?.forEach { order ->
            if (!mShowMyTasks)
                tasksToShow.addAll(order.tasks.filter { it.status == Task.TASK_STATUS_ADDED })
        }
        mAdapter.setTasksList(tasksToShow)
        view?.tasksRecyclerView?.scheduleLayoutAnimation()
        view?.tasksLoadingIndicator?.hide()
        if (tasksToShow.isEmpty()) {
            view?.emptyActiveTasksView?.visibility = View.VISIBLE
        } else {
            view?.emptyActiveTasksView?.visibility = View.INVISIBLE
        }
    }

    private fun inflateToolbarMenu() {
        view?.taskListToolbar?.inflateMenu(R.menu.tasks_menu)
        view?.taskListToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_filter -> {
                    // TODO() -> Filter bottom sheet
                    true
                }
                R.id.action_account -> {
                    // TODO() -> Account details fragment
                    true
                }
                R.id.action_settings -> {
                    findNavController().navigate(TaskListFragmentDirections.showSettingsFragment())
                    true
                }
                R.id.action_sign_out -> {
                    mViewModel.unregisterTeamListener()
                    (requireActivity() as MainActivity).signOut()
                    true
                }
                else -> true
            }
        }
    }
}
