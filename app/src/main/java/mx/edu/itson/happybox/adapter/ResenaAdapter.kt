package mx.edu.itson.happybox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Resena
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResenaAdapter(
    private var resenas: List<Resena>,
    private val onDeleteClick: ((Resena) -> Unit)? = null,
    private val onEditClick: ((Resena) -> Unit)? = null
) : RecyclerView.Adapter<ResenaAdapter.ResenaViewHolder>() {

    fun actualizarLista(nuevasResenas: List<Resena>) {
        resenas = nuevasResenas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResenaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resena, parent, false)
        return ResenaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResenaViewHolder, position: Int) {
        val resena = resenas[position]
        holder.bind(resena)
    }

    override fun getItemCount(): Int = resenas.size

    inner class ResenaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAutor: TextView = itemView.findViewById(R.id.tvResenaAutor)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvResenaFecha)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarResena)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvResenaTitulo)
        private val tvComentario: TextView = itemView.findViewById(R.id.tvResenaComentario)
        private val btnDelete: android.widget.ImageButton = itemView.findViewById(R.id.btnDeleteResena)
        private val btnEdit: android.widget.ImageButton = itemView.findViewById(R.id.btnEditResena)

        fun bind(resena: Resena) {
            if (resena.productoNombre.isNotEmpty()) {
                tvAutor.text = "Producto: ${resena.productoNombre}"
            } else {
                tvAutor.text = resena.autorNombre
            }
            tvTitulo.text = resena.titulo
            tvComentario.text = resena.comentario
            ratingBar.rating = resena.rating.toFloat()

            // Formatear la fecha (ej: 12 may. 2026)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val fechaFormateada = sdf.format(Date(resena.fecha))
            tvFecha.text = fechaFormateada

            if (onDeleteClick != null) {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener { onDeleteClick.invoke(resena) }
            } else {
                btnDelete.visibility = View.GONE
            }

            if (onEditClick != null) {
                btnEdit.visibility = View.VISIBLE
                btnEdit.setOnClickListener { onEditClick.invoke(resena) }
            } else {
                btnEdit.visibility = View.GONE
            }
        }
    }
}
