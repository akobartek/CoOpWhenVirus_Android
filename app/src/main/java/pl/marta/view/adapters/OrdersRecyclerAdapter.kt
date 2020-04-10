package pl.marta.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order.view.*
import pl.marta.R
import pl.marta.model.Order
import pl.marta.model.Task
import pl.marta.view.fragments.OrdersListFragmentDirections

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersRecyclerAdapter.OrderViewHolder>() {

    private var mOrders = listOf<Order>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OrderViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
    )

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bindView(mOrders[position])

    override fun getItemCount(): Int = mOrders.size

    fun setOrdersList(list: List<Order>) {
        mOrders = list
        notifyDataSetChanged()
    }


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(order: Order) {
            val activeTasks = order.tasks.filter { it.status != Task.TASK_STATUS_COMPLETE }
            itemView.orderNumberOfTasksTV.text =
                if (activeTasks.size < 100) activeTasks.size.toString() else "99+"
            itemView.orderNeedyNameTV.text = order.martaName
            itemView.orderAddressTV.text = order.getAddressFormatted()

            itemView.setOnClickListener {
                itemView.findNavController()
                    .navigate(OrdersListFragmentDirections.showOrderEditorFragment(order))
            }
        }
    }
}