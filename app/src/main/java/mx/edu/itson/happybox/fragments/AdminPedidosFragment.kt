package mx.edu.itson.happybox.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.adapter.PedidoAdapter
import mx.edu.itson.happybox.model.Pedido

class AdminPedidosFragment : Fragment() {

    private lateinit var tvResumen: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: PedidoAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_pedidos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        tvResumen = view.findViewById(R.id.tvResumenAdminPedidos)
        listView = view.findViewById(R.id.listViewAdminPedidos)

        adapter = PedidoAdapter(requireContext(), emptyList())
        listView.adapter = adapter

        cargarTodosLosPedidos()
    }

    private fun cargarTodosLosPedidos() {
        if (!isAdded) return
        db.collection("usuarios").get()
            .addOnSuccessListener { users ->
                if (!isAdded) return@addOnSuccessListener
                val todosLosPedidos = mutableListOf<Pedido>()
                var procesados = 0
                val totalUsuarios = users.size()

                if (totalUsuarios == 0) {
                    adapter.updateData(emptyList())
                    tvResumen.text = "0 pedidos en total"
                    return@addOnSuccessListener
                }

                for (userDoc in users) {
                    db.collection("usuarios").document(userDoc.id).collection("pedidos").get()
                        .addOnSuccessListener { pedidosResult ->
                            val pedidosUsuario = pedidosResult.mapNotNull { it.toObject(Pedido::class.java) }
                            todosLosPedidos.addAll(pedidosUsuario)
                            procesados++

                            if (procesados == totalUsuarios) {
                                if (!isAdded) return@addOnSuccessListener
                                todosLosPedidos.sortByDescending { it.fecha }
                                adapter.updateData(todosLosPedidos)
                                tvResumen.text = "${todosLosPedidos.size} pedidos en total"
                            }
                        }
                        .addOnFailureListener {
                            procesados++
                            if (procesados == totalUsuarios && isAdded) {
                                todosLosPedidos.sortByDescending { it.fecha }
                                adapter.updateData(todosLosPedidos)
                                tvResumen.text = "${todosLosPedidos.size} pedidos en total"
                            }
                        }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
