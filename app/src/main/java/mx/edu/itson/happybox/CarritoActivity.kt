package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.adapter.CarritoAdapter
import mx.edu.itson.happybox.model.ItemCarrito
import mx.edu.itson.happybox.model.Producto
import mx.edu.itson.happybox.utils.BadgeUtils

class CarritoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var listViewCarrito: ListView
    private lateinit var tvTotalCarrito: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tvCarritoVacio: TextView
    private lateinit var layoutResumen: View

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var listaItems = mutableListOf<ItemCarrito>()
    private lateinit var adapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        inicializarVistas()
        configurarToolbar()
        configurarBottomNav()
        configurarAdapter()
    }

    override fun onResume() {
        super.onResume()
        cargarCarrito()
        BadgeUtils.actualizarBadgeCarrito(bottomNav)
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarCarrito)
        listViewCarrito = findViewById(R.id.listViewCarrito)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        bottomNav = findViewById(R.id.bottomNavCarrito)
        tvCarritoVacio = findViewById(R.id.tvCarritoVacio)
        layoutResumen = findViewById(R.id.layoutResumen)
    }

    private fun configurarAdapter() {
        adapter = CarritoAdapter(
            this,
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
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("productoId", item.producto.id)
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
                Log.e("CarritoActivity", "Error al cargar el carrito", e)
                Toast.makeText(this, "Error al cargar carrito", Toast.LENGTH_SHORT).show()
                mostrarVacio()
            }
    }

    private fun actualizarCantidad(item: ItemCarrito, nuevaCantidad: Int) {
        val uid = auth.currentUser?.uid ?: return
        val productoId = item.producto.id.toString()

        db.collection("usuarios").document(uid).collection("carrito").document(productoId)
            .update("cantidad", nuevaCantidad)
            .addOnSuccessListener {
                item.cantidad = nuevaCantidad
                adapter.notifyDataSetChanged()
                calcularTotal()
            }
            .addOnFailureListener { e ->
                Log.e("CarritoActivity", "Error al actualizar cantidad", e)
            }
    }

    private fun eliminarDelCarrito(item: ItemCarrito) {
        val uid = auth.currentUser?.uid ?: return
        val productoId = item.producto.id.toString()

        db.collection("usuarios").document(uid).collection("carrito").document(productoId)
            .delete()
            .addOnSuccessListener {
                listaItems.remove(item)
                adapter.notifyDataSetChanged()
                calcularTotal()
                if (listaItems.isEmpty()) {
                    mostrarVacio()
                }
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CarritoActivity", "Error al eliminar producto", e)
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
        tvTotalCarrito.text = "$0.00"
    }

    private fun calcularTotal() {
        val total = listaItems.sumOf { it.subtotal }
        tvTotalCarrito.text = getString(R.string.formatoPrecioCarrito, total)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.titleCarrito)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun navegarA(destino: Class<*>) {
        startActivity(Intent(this, destino).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navCarrito

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio  -> { navegarA(HomeActivity::class.java);    true }
                R.id.navBuscar  -> { navegarA(ProductosActivity::class.java); true }
                R.id.navCarrito -> true
                R.id.navPerfil  -> { navegarA(PerfilActivity::class.java);  true }
                else -> false
            }
        }
    }
}
