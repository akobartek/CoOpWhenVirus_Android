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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_orders_list.view.*
import kotlinx.android.synthetic.main.fragment_orders_list.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.view.adapters.OrdersRecyclerAdapter
import pl.pomocnawirus.view.adapters.TasksRecyclerAdapter
import pl.pomocnawirus.viewmodel.MainViewModel
import pl.pomocnawirus.viewmodel.OrdersViewModel

class OrdersListFragment : Fragment() {

    private lateinit var mViewModel: OrdersViewModel
    private lateinit var mOrdersAdapter: OrdersRecyclerAdapter
    private lateinit var mTasksAdapter: TasksRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_orders_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

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

        mViewModel = ViewModelProvider(requireActivity()).get(OrdersViewModel::class.java)
        val teamId = arguments?.let { OrdersListFragmentArgs.fromBundle(it).teamId }
        if (teamId == null)
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java).currentUser.value?.teamId
        mViewModel.fetchOrders(teamId!!)
        mViewModel.orders.observe(viewLifecycleOwner, Observer {
            if (mViewModel.areOrdersSelectedToShow) showOrders()
            else showTasks()
        })
        mViewModel.filters.observe(viewLifecycleOwner, Observer {
            if (!mViewModel.areOrdersSelectedToShow) showTasks()
        })

        view.addOrderBtn.setOnClickListener {
            findNavController().navigate(OrdersListFragmentDirections.showOrderEditorFragment(null))
        }
    }

    private fun inflateToolbarMenu() {
        view?.ordersListToolbar?.inflateMenu(
            if (mViewModel.areOrdersSelectedToShow) R.menu.orders_menu
            else R.menu.tasks_menu_leader
        )
        view?.ordersListToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_team_details -> {
                    findNavController().navigate(OrdersListFragmentDirections.showTeamEditorFragment())
                    true
                }
                R.id.action_show_tasks -> {
                    mViewModel.areOrdersSelectedToShow = true
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
        val filters = mViewModel.filters.value
        mViewModel.orders.value?.forEach { order ->
            tasksToShow.addAll(order.tasks.filter { task ->
                filters?.selectedTaskTypes!!.contains(task.type) &&
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
