package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.adapter.ProductoAdapter
import mx.edu.itson.happybox.model.CarritoManager
import mx.edu.itson.happybox.model.Producto
import mx.edu.itson.happybox.model.ProductoSeeder

class ProductosActivity : AppCompatActivity() {

    // ── Vistas ──────────────────────────────────────────────
    private lateinit var toolbar: Toolbar
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipTodos: Chip
    private lateinit var chipPrecioAsc: Chip
    private lateinit var chipPrecioDesc: Chip
    private lateinit var chipNombre: Chip
    private lateinit var etBuscar: EditText
    private lateinit var listViewProductos: ListView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var progressBar: ProgressBar

    // ── Datos ────────────────────────────────────────────────
    private var listaOriginal: List<Producto> = emptyList()
    private var listaFiltrada: MutableList<Producto> = mutableListOf()
    private lateinit var adapter: ProductoAdapter
    private lateinit var db: FirebaseFirestore

    private var categoria: String? = null
    private var query: String? = null
    private var ordenSeleccionado: Orden = Orden.NINGUNO

    private enum class Orden {
        NINGUNO,
        PRECIO_ASC,
        PRECIO_DESC,
        NOMBRE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        categoria = intent.getStringExtra("categoria")
        query     = intent.getStringExtra("query")

        db = FirebaseFirestore.getInstance()

        inicializarVistas()
        configurarToolbar()
        configurarListView()
        configurarBuscador()
        configurarChips()
        configurarBottomNav()

        // Sembrar productos si es la primera vez, luego cargar desde Firestore
        ProductoSeeder.sembrar(db) {
            cargarProductosDesdeFirestore()
        }
    }

    private fun inicializarVistas() {
        toolbar           = findViewById(R.id.toolbarProductos)
        chipGroup         = findViewById(R.id.chipGroupFiltros)
        chipTodos         = findViewById(R.id.chipTodos)
        chipPrecioAsc     = findViewById(R.id.chipPrecioAsc)
        chipPrecioDesc    = findViewById(R.id.chipPrecioDesc)
        chipNombre        = findViewById(R.id.chipNombre)
        etBuscar          = findViewById(R.id.etBuscar)
        listViewProductos = findViewById(R.id.listViewProductos)
        bottomNav         = findViewById(R.id.bottomNavProductos)
        progressBar       = findViewById(R.id.progressBarProductos)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = when {
                query != null     -> "Resultados: $query"
                categoria != null -> categoria
                else              -> "Productos"
            }
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarProductosDesdeFirestore() {
        progressBar.visibility = View.VISIBLE

        db.collection("productos")
            .get()
            .addOnSuccessListener { snapshot ->
                val todos = snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }

                listaOriginal = when {
                    categoria != null -> todos.filter { it.categoria == categoria }
                    else              -> todos
                }

                if (!query.isNullOrBlank() && etBuscar.text.isNullOrBlank()) {
                    etBuscar.setText(query)
                    etBuscar.setSelection(etBuscar.text.length)
                } else {
                    aplicarBusquedaYOrden()
                }

                if (listaFiltrada.isEmpty()) {
                    Toast.makeText(this, getString(R.string.toastListaVacia), Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al cargar productos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun configurarBuscador() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarBusquedaYOrden()
                supportActionBar?.title = if (s.isNullOrBlank()) {
                    categoria ?: "Productos"
                } else {
                    "Resultados: ${s.toString().trim()}"
                }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun configurarListView() {
        adapter = ProductoAdapter(this, listaFiltrada)
        listViewProductos.adapter = adapter

        listViewProductos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                irADetalle(listaFiltrada[position])
            }
    }

    private fun configurarChips() {
        chipTodos.setOnClickListener {
            desmarcarTodosLosChips()
            chipTodos.isChecked = true
            ordenSeleccionado = Orden.NINGUNO
            aplicarBusquedaYOrden()
        }

        chipPrecioAsc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioAsc.isChecked = true
            ordenSeleccionado = Orden.PRECIO_ASC
            aplicarBusquedaYOrden()
        }

        chipPrecioDesc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioDesc.isChecked = true
            ordenSeleccionado = Orden.PRECIO_DESC
            aplicarBusquedaYOrden()
        }

        chipNombre.setOnClickListener {
            desmarcarTodosLosChips()
            chipNombre.isChecked = true
            ordenSeleccionado = Orden.NOMBRE
            aplicarBusquedaYOrden()
        }

        chipTodos.isChecked = true
    }

    private fun aplicarBusquedaYOrden() {
        val consulta = etBuscar.text.toString().trim()
        val filtrada = listaOriginal.filter { producto ->
            consulta.isEmpty() ||
                producto.nombre.contains(consulta, ignoreCase = true) ||
                producto.descripcion.contains(consulta, ignoreCase = true) ||
                producto.categoria.contains(consulta, ignoreCase = true)
        }

        listaFiltrada.clear()
        listaFiltrada.addAll(
            when (ordenSeleccionado) {
                Orden.PRECIO_ASC -> filtrada.sortedBy { it.precio }
                Orden.PRECIO_DESC -> filtrada.sortedByDescending { it.precio }
                Orden.NOMBRE -> filtrada.sortedBy { it.nombre }
                Orden.NINGUNO -> filtrada
            }
        )
        adapter.notifyDataSetChanged()
    }

    private fun desmarcarTodosLosChips() {
        chipTodos.isChecked      = false
        chipPrecioAsc.isChecked  = false
        chipPrecioDesc.isChecked = false
        chipNombre.isChecked     = false
    }

    private fun navegarA(destino: Class<*>) {
        startActivity(Intent(this, destino).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navBuscar

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio  -> { navegarA(HomeActivity::class.java);    true }
                R.id.navBuscar  -> true
                R.id.navCarrito -> { navegarA(CarritoActivity::class.java); true }
                R.id.navPerfil  -> { navegarA(PerfilActivity::class.java);  true }
                else -> false
            }
        }
    }

    private fun irADetalle(producto: Producto) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("productoId",          producto.id)
            putExtra("productoNombre",      producto.nombre)
            putExtra("productoPrecio",      producto.precio)
            putExtra("productoDescription", producto.descripcion)
            putExtra("productoImagenRes",   producto.imagenResId)
            putExtra("productoImagenUrl",   producto.imagenUrl)
        }
        startActivity(intent)
    }
}
