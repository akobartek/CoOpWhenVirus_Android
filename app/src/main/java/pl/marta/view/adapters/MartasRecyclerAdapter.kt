package pl.marta.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_marta.view.*
import pl.marta.R
import pl.marta.model.Marta

class MartasRecyclerAdapter(val onClickAction: (Marta) -> Unit) :
    RecyclerView.Adapter<MartasRecyclerAdapter.MartaViewHolder>() {

    private var mMartas = listOf<Marta>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MartaViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_marta, parent, false)
    )

    override fun onBindViewHolder(holder: MartaViewHolder, position: Int) =
        holder.bindView(mMartas[position])

    override fun getItemCount(): Int = mMartas.size

    fun setMartasList(list: List<Marta>) {
        mMartas = list
        notifyDataSetChanged()
    }


    inner class MartaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(marta: Marta) {
            itemView.martaNameTV.text = marta.name
            itemView.martaAddressTV.text = marta.getAddressFormatted()

            itemView.setOnClickListener {
                onClickAction(marta)
            }
        }
    }
}