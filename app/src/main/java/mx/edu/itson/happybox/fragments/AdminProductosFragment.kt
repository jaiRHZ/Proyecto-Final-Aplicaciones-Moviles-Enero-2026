package mx.edu.itson.happybox.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.AdminEditProductoActivity
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.adapter.AdminProductosAdapter
import mx.edu.itson.happybox.model.Producto

class AdminProductosFragment : Fragment() {

    private lateinit var rvProductos: RecyclerView
    private lateinit var fabAddProducto: FloatingActionButton
    private lateinit var adapter: AdminProductosAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_productos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rvProductos = view.findViewById(R.id.rvAdminProductos)
        fabAddProducto = view.findViewById(R.id.fabAddProducto)

        adapter = AdminProductosAdapter(
            emptyList(),
            onEditClick = { producto -> 
                val intent = Intent(requireContext(), AdminEditProductoActivity::class.java)
                intent.putExtra("productoId", producto.id)
                startActivity(intent)
            },
            onDeleteClick = { producto -> confirmarEliminacion(producto) }
        )
        
        rvProductos.layoutManager = LinearLayoutManager(requireContext())
        rvProductos.adapter = adapter

        fabAddProducto.setOnClickListener {
            startActivity(Intent(requireContext(), AdminEditProductoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    private fun cargarProductos() {
        if (!isAdded) return
        db.collection("productos").get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                val lista = result.mapNotNull { it.toObject(Producto::class.java) }
                adapter.actualizarLista(lista)
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun confirmarEliminacion(producto: Producto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar '${producto.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("productos").document(producto.id.toString())
                    .delete()
                    .addOnSuccessListener {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                            cargarProductos()
                        }
                    }
                    .addOnFailureListener {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Error al eliminar producto", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
