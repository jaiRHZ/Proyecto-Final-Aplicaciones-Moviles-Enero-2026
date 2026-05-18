package mx.edu.itson.happybox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Producto

class AdminProductosAdapter(
    private var productos: List<Producto>,
    private val onEditClick: (Producto) -> Unit,
    private val onDeleteClick: (Producto) -> Unit
) : RecyclerView.Adapter<AdminProductosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivAdminProdImage)
        val tvName: TextView = view.findViewById(R.id.tvAdminProdName)
        val tvPrice: TextView = view.findViewById(R.id.tvAdminProdPrice)
        val tvStock: TextView = view.findViewById(R.id.tvAdminProdStock)
        val btnEdit: ImageButton = view.findViewById(R.id.btnAdminEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnAdminDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvName.text = producto.nombre
        holder.tvPrice.text = String.format("$%.2f", producto.precio)
        holder.tvStock.text = "Stock: ${producto.stock}"

        if (producto.imagenUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_placeholder_producto)
                .into(holder.ivImage)
        } else if (producto.imagenResId != 0) {
            holder.ivImage.setImageResource(producto.imagenResId)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_placeholder_producto)
        }

        holder.btnEdit.setOnClickListener { onEditClick(producto) }
        holder.btnDelete.setOnClickListener { onDeleteClick(producto) }
    }

    override fun getItemCount() = productos.size

    fun actualizarLista(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }
}
