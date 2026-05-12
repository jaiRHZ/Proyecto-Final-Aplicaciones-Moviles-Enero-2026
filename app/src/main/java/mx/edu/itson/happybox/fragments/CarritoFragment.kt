package mx.edu.itson.happybox.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.DetailActivity
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.adapter.CarritoAdapter
import mx.edu.itson.happybox.model.ItemCarrito
import mx.edu.itson.happybox.model.Producto

class CarritoFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var listViewCarrito: ListView
    private lateinit var tvTotalCarrito: TextView
    private lateinit var tvSubtotalCarrito: TextView
    private lateinit var tvIvaCarrito: TextView
    private lateinit var tvEnvioCarrito: TextView
    private lateinit var tvPromoEnvio: TextView
    private lateinit var tvCarritoVacio: TextView
    private lateinit var layoutResumen: View

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var listaItems = mutableListOf<ItemCarrito>()
    private lateinit var adapter: CarritoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carrito, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        inicializarVistas(view)
        configurarToolbar()
        configurarAdapter()
    }

    override fun onResume() {
        super.onResume()
        cargarCarrito()
    }

    private fun inicializarVistas(view: View) {
        toolbar = view.findViewById(R.id.toolbarCarrito)
        listViewCarrito = view.findViewById(R.id.listViewCarrito)
        tvTotalCarrito = view.findViewById(R.id.tvTotalCarrito)
        tvSubtotalCarrito = view.findViewById(R.id.tvSubtotalCarrito)
        tvIvaCarrito = view.findViewById(R.id.tvIvaCarrito)
        tvEnvioCarrito = view.findViewById(R.id.tvEnvioCarrito)
        tvPromoEnvio = view.findViewById(R.id.tvPromoEnvio)
        tvCarritoVacio = view.findViewById(R.id.tvCarritoVacio)
        layoutResumen = view.findViewById(R.id.layoutResumen)
    }

    private fun configurarAdapter() {
        adapter = CarritoAdapter(
            requireContext(),
            listaItems,
            onIncrementar = { item -> actualizarCantidad(item, item.cantidad + 1) },
            onDecrementar = { item ->
                if (item.cantidad > 1) {
                    actualizarCantidad(item, item.cantidad - 1)
                } else {
                    eliminarDelCarrito(item)
                }
            },
            onEliminar = { item -> eliminarDelCarrito(item) }
        )
        listViewCarrito.adapter = adapter

        listViewCarrito.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position)
            if (item != null) {
                val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("productoId",          item.producto.id)
                    putExtra("productoNombre",      item.producto.nombre)
                    putExtra("productoPrecio",      item.producto.precio)
                    putExtra("productoDescription", item.producto.descripcion)
                    putExtra("productoImagenRes",   item.producto.imagenResId)
                }
                startActivity(intent)
            }
        }
    }

    private fun cargarCarrito() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            mostrarVacio()
            return
        }

        db.collection("usuarios").document(uid).collection("carrito")
            .get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                
                if (result.isEmpty) {
                    mostrarVacio()
                    return@addOnSuccessListener
                }

                val itemsTemp = mutableListOf<ItemCarrito>()
                var procesados = 0
                val totalDocs = result.size()

                for (doc in result) {
                    val productoId = doc.get("productoId")?.toString()?.toIntOrNull()
                    val cantidad = doc.get("cantidad")?.toString()?.toIntOrNull() ?: 1

                    if (productoId != null) {
                        db.collection("productos").document(productoId.toString())
                            .get()
                            .addOnSuccessListener { prodDoc ->
                                if (!isAdded) return@addOnSuccessListener
                                val producto = prodDoc.toObject(Producto::class.java)
                                if (producto != null) {
                                    itemsTemp.add(ItemCarrito(producto, cantidad))
                                }
                                procesados++
                                if (procesados == totalDocs) {
                                    actualizarListaUI(itemsTemp)
                                }
                            }
                            .addOnFailureListener {
                                if (!isAdded) return@addOnFailureListener
                                procesados++
                                if (procesados == totalDocs) {
                                    actualizarListaUI(itemsTemp)
                                }
                            }
                    } else {
                        procesados++
                        if (procesados == totalDocs) {
                            actualizarListaUI(itemsTemp)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e("CarritoFragment", "Error al cargar el carrito", e)
                Toast.makeText(requireContext(), "Error al cargar carrito", Toast.LENGTH_SHORT).show()
                mostrarVacio()
            }
    }

    private fun actualizarCantidad(item: ItemCarrito, nuevaCantidad: Int) {
        val uid = auth.currentUser?.uid ?: return
        val productoId = item.producto.id.toString()

        db.collection("usuarios").document(uid).collection("carrito").document(productoId)
            .update("cantidad", nuevaCantidad)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                item.cantidad = nuevaCantidad
                adapter.notifyDataSetChanged()
                calcularTotal()
                
                // Disparar actualización del badge en la activity principal
                val activity = requireActivity()
                if (activity is mx.edu.itson.happybox.MainHostActivity) {
                    mx.edu.itson.happybox.utils.BadgeUtils.actualizarBadgeCarrito(
                        activity.findViewById(R.id.bottomNavHost)
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("CarritoFragment", "Error al actualizar cantidad", e)
            }
    }

    private fun eliminarDelCarrito(item: ItemCarrito) {
        val uid = auth.currentUser?.uid ?: return
        val productoId = item.producto.id.toString()

        db.collection("usuarios").document(uid).collection("carrito").document(productoId)
            .delete()
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                listaItems.remove(item)
                adapter.notifyDataSetChanged()
                calcularTotal()
                if (listaItems.isEmpty()) {
                    mostrarVacio()
                }
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                
                // Disparar actualización del badge en la activity principal
                val activity = requireActivity()
                if (activity is mx.edu.itson.happybox.MainHostActivity) {
                    mx.edu.itson.happybox.utils.BadgeUtils.actualizarBadgeCarrito(
                        activity.findViewById(R.id.bottomNavHost)
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("CarritoFragment", "Error al eliminar producto", e)
            }
    }

    private fun actualizarListaUI(items: List<ItemCarrito>) {
        listaItems.clear()
        listaItems.addAll(items)
        if (listaItems.isEmpty()) {
            mostrarVacio()
        } else {
            listViewCarrito.visibility = View.VISIBLE
            layoutResumen.visibility = View.VISIBLE
            tvCarritoVacio.visibility = View.GONE
            adapter.notifyDataSetChanged()
            calcularTotal()
        }
    }

    private fun mostrarVacio() {
        listViewCarrito.visibility = View.GONE
        layoutResumen.visibility = View.GONE
        tvCarritoVacio.visibility = View.VISIBLE
        listaItems.clear()
        adapter.notifyDataSetChanged()
        
        val cero = "$0.00"
        tvSubtotalCarrito.text = cero
        tvIvaCarrito.text = cero
        tvEnvioCarrito.text = cero
        tvTotalCarrito.text = cero
        tvPromoEnvio.visibility = View.GONE
    }

    private fun calcularTotal() {
        var total = listaItems.sumOf { it.subtotal }
        val iva = total * 0.16
        
        val envio: Double
        if (total >= 200.0) {
            envio = 0.0
            tvPromoEnvio.visibility = View.VISIBLE
        } else {
            envio = 50.0
            tvPromoEnvio.visibility = View.GONE
        }
        
        val subtotal = total - iva

        total += envio

        tvSubtotalCarrito.text = getString(R.string.formatoPrecioCarrito, subtotal)
        tvIvaCarrito.text = getString(R.string.formatoPrecioCarrito, iva)
        tvEnvioCarrito.text = getString(R.string.formatoPrecioCarrito, envio)
        tvTotalCarrito.text = getString(R.string.formatoPrecioCarrito, total)
    }

    private fun configurarToolbar() {
        toolbar.title = getString(R.string.titleCarrito)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            val activity = requireActivity()
            if (activity is mx.edu.itson.happybox.MainHostActivity) {
                activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavHost).selectedItemId = R.id.navInicio
            }
        }
    }
}
