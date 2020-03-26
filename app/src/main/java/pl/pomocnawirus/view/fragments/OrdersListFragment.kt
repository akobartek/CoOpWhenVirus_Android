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
import pl.pomocnawirus.view.adapters.OrdersRecyclerAdapter
import pl.pomocnawirus.viewmodel.MainViewModel
import pl.pomocnawirus.viewmodel.OrdersViewModel

class OrdersListFragment : Fragment() {

    private lateinit var mViewModel: OrdersViewModel
    private lateinit var mAdapter: OrdersRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_orders_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu()

        mAdapter = OrdersRecyclerAdapter()
        view.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
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
        mViewModel.orders.observe(viewLifecycleOwner, Observer {orders ->
            mAdapter.setTasksList(orders)
            view.ordersRecyclerView.scheduleLayoutAnimation()
            view.ordersLoadingIndicator.hide()
            if (orders.isEmpty()) {
                view.emptyOrdersView.visibility = View.VISIBLE
            } else {
                view.emptyOrdersView.visibility = View.INVISIBLE
            }
        })

        view.addOrderBtn.setOnClickListener {
            // TODO() -> Navigate to creating new order
        }
    }

    private fun inflateToolbarMenu() {
        view?.ordersListToolbar?.inflateMenu(R.menu.orders_menu)
        view?.ordersListToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_team_details -> {
                    // TODO() -> Navigate to team details
                    true
                }
                R.id.action_my_tasks -> {
                    // TODO() -> Navigate to admin's tasks
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
                else -> true
            }
        }
    }
}
