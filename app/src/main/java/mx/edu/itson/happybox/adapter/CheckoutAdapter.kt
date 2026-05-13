package mx.edu.itson.happybox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.ItemCarrito

class CheckoutAdapter(private var items: List<ItemCarrito>) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    fun updateItems(newItems: List<ItemCarrito>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_checkout, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProduct: ImageView = itemView.findViewById(R.id.ivCheckItem)
        private val tvName: TextView = itemView.findViewById(R.id.tvCheckItemName)
        private val tvQty: TextView = itemView.findViewById(R.id.tvCheckItemQty)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvCheckItemSubtotal)

        fun bind(item: ItemCarrito) {
            tvName.text = item.producto.nombre
            tvQty.text = "Cantidad: ${item.cantidad}"
            tvSubtotal.text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(item.subtotal)
            ivProduct.setImageResource(if (item.producto.imagenResId != 0) item.producto.imagenResId else R.drawable.ic_placeholder_producto)
        }
    }
}
