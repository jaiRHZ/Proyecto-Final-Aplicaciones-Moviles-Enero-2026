package mx.edu.itson.happybox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Domicilio

class DomicilioAdapter(
    private var domicilios: List<Domicilio>,
    private val onEditClick: (Domicilio) -> Unit,
    private val onDeleteClick: (Domicilio) -> Unit
) : RecyclerView.Adapter<DomicilioAdapter.DomicilioViewHolder>() {

    fun actualizarLista(nuevosDomicilios: List<Domicilio>) {
        domicilios = nuevosDomicilios
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DomicilioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_domicilio, parent, false)
        return DomicilioViewHolder(view)
    }

    override fun onBindViewHolder(holder: DomicilioViewHolder, position: Int) {
        holder.bind(domicilios[position])
    }

    override fun getItemCount(): Int = domicilios.size

    inner class DomicilioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCalleNumero: TextView = itemView.findViewById(R.id.tvCalleNumero)
        private val tvColoniaCiudad: TextView = itemView.findViewById(R.id.tvColoniaCiudad)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditDomicilio)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteDomicilio)

        fun bind(domicilio: Domicilio) {
            tvCalleNumero.text = "${domicilio.calle} ${domicilio.numero}"
            tvColoniaCiudad.text = "Col. ${domicilio.colonia}, C.P. ${domicilio.codigoPostal}, ${domicilio.ciudad}"

            btnEdit.setOnClickListener { onEditClick(domicilio) }
            btnDelete.setOnClickListener { onDeleteClick(domicilio) }
        }
    }
}
