package pl.marta.view.fragments

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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_orders_list.view.*
import kotlinx.android.synthetic.main.fragment_orders_list.view.*
import pl.marta.R
import pl.marta.model.Task
import pl.marta.utils.Filters
import pl.marta.view.adapters.OrdersRecyclerAdapter
import pl.marta.view.adapters.TasksRecyclerAdapter
import pl.marta.viewmodel.MainViewModel
import pl.marta.viewmodel.OrdersViewModel

class OrdersListFragment : Fragment() {

    private lateinit var mViewModel: OrdersViewModel
    private lateinit var mOrdersAdapter: OrdersRecyclerAdapter
    private lateinit var mTasksAdapter: TasksRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_orders_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(OrdersViewModel::class.java)
        inflateToolbarMenu()
        view.emptyOrdersView.text = getString(
            if (mViewModel.areOrdersSelectedToShow) R.string.empty_orders_list else R.string.empty_tasks_list
        )
        if (!mViewModel.areOrdersSelectedToShow) view.addOrderBtn.hide()
        else view.addOrderBtn.visibility = View.VISIBLE

        mOrdersAdapter = OrdersRecyclerAdapter()
        mTasksAdapter = TasksRecyclerAdapter() { task ->
            val taskDetailsBottomSheet = TaskDetailsBottomSheetFragment(
                mViewModel.orders.value!!.first { it.tasks.contains(task) }, task
            )
            taskDetailsBottomSheet.show(childFragmentManager, taskDetailsBottomSheet.tag)
        }
        view.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0 && !view.addOrderBtn.isShown)
                        view.addOrderBtn.show()
                    else if (dy > 0 && view.addOrderBtn.isShown)
                        view.addOrderBtn.hide()
                }
            })
        }

        var teamId = arguments?.let { TaskListFragmentArgs.fromBundle(it).teamId }
        if (teamId == null)
            teamId = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
                .currentUser.value?.teamId
        if (!teamId.isNullOrEmpty()) mViewModel.fetchOrders(teamId)
        else requireActivity().recreate()
        mViewModel.orders.observe(viewLifecycleOwner, Observer {
            if (mViewModel.areOrdersSelectedToShow) showOrders()
            else showTasks()
        })
        mViewModel.filters.observe(viewLifecycleOwner, Observer {
            if (!mViewModel.areOrdersSelectedToShow && it != null) showTasks()
        })

        view.addOrderBtn.setOnClickListener {
            findNavController().navigate(OrdersListFragmentDirections.showOrderEditorFragment(null))
        }
    }

    private fun inflateToolbarMenu() {
        view?.ordersListToolbar?.menu?.clear()
        view?.ordersListToolbar?.inflateMenu(
            if (mViewModel.areOrdersSelectedToShow) R.menu.orders_menu
            else R.menu.tasks_menu_leader
        )
        view?.ordersListToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_martas_templates -> {
                    // TODO() -> XDDD
                    true
                }
                R.id.action_team_details -> {
                    findNavController().navigate(OrdersListFragmentDirections.showTeamEditorFragment())
                    true
                }
                R.id.action_show_tasks -> {
                    mViewModel.areOrdersSelectedToShow = false
                    view?.ordersListToolbar?.title = getString(R.string.tasks)
                    view?.emptyOrdersView?.text = getString(R.string.empty_tasks_list)
                    view?.addOrderBtn?.hide()
                    inflateToolbarMenu()
                    showTasks()
                    true
                }
                R.id.action_account -> {
                    findNavController().navigate(OrdersListFragmentDirections.showAccountFragment())
                    true
                }
                R.id.action_settings -> {
                    findNavController().navigate(OrdersListFragmentDirections.showSettingsFragment())
                    true
                }
                R.id.action_filter -> {
                    val taskFilterBottomSheet = TaskFilterBottomSheetFragment(mViewModel, true)
                    taskFilterBottomSheet.show(childFragmentManager, taskFilterBottomSheet.tag)
                    true
                }
                R.id.action_show_orders -> {
                    mViewModel.areOrdersSelectedToShow = true
                    view?.ordersListToolbar?.title = getString(R.string.orders)
                    view?.emptyOrdersView?.text = getString(R.string.empty_orders_list)
                    view?.addOrderBtn?.show()
                    inflateToolbarMenu()
                    showOrders()
                    true
                }
                else -> true
            }
        }
    }

    private fun showOrders() {
        view?.ordersRecyclerView?.adapter = mOrdersAdapter
        mOrdersAdapter.setOrdersList(mViewModel.orders.value!!)
        view?.ordersRecyclerView?.scheduleLayoutAnimation()
        view?.ordersLoadingIndicator?.hide()
        if (mViewModel.orders.value!!.isEmpty()) {
            view?.emptyOrdersView?.visibility = View.VISIBLE
        } else {
            view?.emptyOrdersView?.visibility = View.INVISIBLE
        }
    }

    private fun showTasks() {
        val tasksToShow = arrayListOf<Task>()
        val filters = mViewModel.filters.value ?: Filters()
        mViewModel.orders.value?.forEach { order ->
            tasksToShow.addAll(order.tasks.filter { task ->
                filters.selectedTaskTypes.contains(task.type) &&
                        if (filters.selectedTaskStatus == Task.TASK_STATUS_ADDED) task.status == Task.TASK_STATUS_ADDED
                        else task.status != Task.TASK_STATUS_ADDED
            })
        }

        view?.ordersRecyclerView?.adapter = mTasksAdapter
        mTasksAdapter.setTasksList(tasksToShow)
        view?.ordersRecyclerView?.scheduleLayoutAnimation()
        view?.ordersLoadingIndicator?.hide()
        if (tasksToShow.isEmpty()) {
            view?.emptyOrdersView?.visibility = View.VISIBLE
        } else {
            view?.emptyOrdersView?.visibility = View.INVISIBLE
        }
    }
}
