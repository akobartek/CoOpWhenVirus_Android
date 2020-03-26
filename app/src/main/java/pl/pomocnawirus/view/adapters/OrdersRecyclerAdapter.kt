package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Order

class OrdersRecyclerAdapter : RecyclerView.Adapter<OrdersRecyclerAdapter.OrderViewHolder>() {

    private var mOrders = listOf<Order>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OrderViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
    )

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bindView(mOrders[position])

    override fun getItemCount(): Int = mOrders.size

    fun setTasksList(list: List<Order>) {
        mOrders = list
        notifyDataSetChanged()
    }


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(order: Order) {
            itemView.orderNumberOfTasksTV.text =
                if (order.tasks.size < 100) order.tasks.size.toString() else "99+"
            itemView.orderNeedyNameTV.text = order.needyName
            itemView.orderAddressTV.text = order.getAddressFormatted()

            itemView.setOnClickListener {
                // TODO()
            }
        }
    }
}