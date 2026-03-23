package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import mx.edu.itson.happybox.adapter.ProductoAdapter
import mx.edu.itson.happybox.model.CarritoManager
import mx.edu.itson.happybox.model.Producto

class ProductosActivity : AppCompatActivity() {

    // ── Vistas ──────────────────────────────────────────────
    private lateinit var toolbar: Toolbar
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipTodos: Chip
    private lateinit var chipPrecioAsc: Chip
    private lateinit var chipPrecioDesc: Chip
    private lateinit var chipNombre: Chip
    private lateinit var listViewProductos: ListView
    private lateinit var bottomNav: BottomNavigationView

    // ── Datos ────────────────────────────────────────────────
    private var listaOriginal: List<Producto> = emptyList()
    private var listaFiltrada: MutableList<Producto> = mutableListOf()
    private lateinit var adapter: ProductoAdapter

    private var categoria: String? = null
    private var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        // Leer datos del Intent
        categoria = intent.getStringExtra("categoria")
        query     = intent.getStringExtra("query")

        inicializarVistas()
        configurarToolbar()
        cargarProductos()
        configurarListView()
        configurarChips()
        configurarBottomNav()
    }

    private fun inicializarVistas() {
        toolbar           = findViewById(R.id.toolbarProductos)
        chipGroup         = findViewById(R.id.chipGroupFiltros)
        chipTodos         = findViewById(R.id.chipTodos)
        chipPrecioAsc     = findViewById(R.id.chipPrecioAsc)
        chipPrecioDesc    = findViewById(R.id.chipPrecioDesc)
        chipNombre        = findViewById(R.id.chipNombre)
        listViewProductos = findViewById(R.id.listViewProductos)
        bottomNav         = findViewById(R.id.bottomNavProductos)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            // Si hay búsqueda, mostrar "Resultados", si no, la categoría
            title = when {
                query != null -> "Resultados: $query"
                categoria != null -> categoria
                else -> "Productos"
            }
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarProductos() {
        // 1. Obtener la base de datos (por ahora simulada con todas las categorías)
        val todosLosProductos = obtenerTodosLosProductos()

        // 2. Aplicar filtros iniciales según lo que se recibió
        listaOriginal = when {
            query != null -> {
                // Filtro por búsqueda de texto
                todosLosProductos.filter { 
                    it.nombre.contains(query!!, ignoreCase = true) || 
                    it.descripcion.contains(query!!, ignoreCase = true)
                }
            }
            categoria != null -> {
                // Filtro por categoría
                todosLosProductos.filter { it.categoria == categoria }
            }
            else -> todosLosProductos
        }

        if (listaOriginal.isEmpty()) {
            Toast.makeText(this, getString(R.string.toastListaVacia), Toast.LENGTH_SHORT).show()
        }

        listaFiltrada = listaOriginal.toMutableList()
    }

    private fun configurarListView() {
        adapter = ProductoAdapter(this, listaFiltrada)
        listViewProductos.adapter = adapter

        listViewProductos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val productoSeleccionado = listaFiltrada[position]
                irADetalle(productoSeleccionado)
            }
    }

    private fun configurarChips() {
        chipTodos.setOnClickListener {
            desmarcarTodosLosChips()
            chipTodos.isChecked = true
            listaFiltrada.clear()
            listaFiltrada.addAll(listaOriginal)
            adapter.notifyDataSetChanged()
        }

        chipPrecioAsc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioAsc.isChecked = true
            listaFiltrada.sortBy { it.precio }
            adapter.notifyDataSetChanged()
        }

        chipPrecioDesc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioDesc.isChecked = true
            listaFiltrada.sortByDescending { it.precio }
            adapter.notifyDataSetChanged()
        }

        chipNombre.setOnClickListener {
            desmarcarTodosLosChips()
            chipNombre.isChecked = true
            listaFiltrada.sortBy { it.nombre }
            adapter.notifyDataSetChanged()
        }

        chipTodos.isChecked = true
    }

    private fun desmarcarTodosLosChips() {
        chipTodos.isChecked      = false
        chipPrecioAsc.isChecked  = false
        chipPrecioDesc.isChecked = false
        chipNombre.isChecked     = false
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navBuscar

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    finish()
                    true
                }
                R.id.navBuscar -> true
                R.id.navCarrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    true
                }
                R.id.navPerfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
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
        }
        startActivity(intent)
    }

    private fun obtenerTodosLosProductos(): List<Producto> {
        val categorias = listOf("Peluches", "Globos", "Tazas", "Detalles", "Regalos")
        val listaCompleta = mutableListOf<Producto>()
        
        categorias.forEach { cat ->
            listaCompleta.addAll(obtenerProductosPorCategoria(cat))
        }
        return listaCompleta
    }

    private fun obtenerProductosPorCategoria(cat: String): List<Producto> {
        return when (cat) {
            "Peluches" -> listOf(
                Producto(1, "Osito de peluche mediano",  249.00, cat, "Peluche suave de 30 cm ideal para regalar."),
                Producto(2, "Conejo suave grande",        320.00, cat, "Conejo esponjoso de 45 cm con lazo."),
                Producto(3, "Perrito de peluche",         199.00, cat, "Perrito tierno de 25 cm, muy suave al tacto."),
                Producto(4, "Oso panda gigante",          580.00, cat, "Panda de 60 cm, perfecto para sorprender.")
            )
            "Globos" -> listOf(
                Producto(5, "Globo personalizado",         85.00, cat, "Globo metálico con tu mensaje impreso."),
                Producto(6, "Set de globos cumpleaños",   150.00, cat, "12 globos de colores con listón."),
                Producto(7, "Globo figura corazón",       120.00, cat, "Globo en forma de corazón, color rosa.")
            )
            "Tazas" -> listOf(
                Producto(8,  "Taza personalizada 11oz",   180.00, cat, "Taza blanca con foto o mensaje."),
                Producto(9,  "Taza mágica",               220.00, cat, "Cambia de color al servir líquido caliente."),
                Producto(10, "Taza tipo termo",           350.00, cat, "Mantiene la temperatura por 6 horas.")
            )
            "Detalles" -> listOf(
                Producto(11, "Caja de chocolates",        260.00, cat, "Surtido de 12 chocolates artesanales."),
                Producto(12, "Vela aromática",            140.00, cat, "Vela de soja con aroma a vainilla.")
            )
            "Regalos" -> listOf(
                Producto(13, "Set de spa relax",          480.00, cat, "Incluye sales de baño, vela y mascarilla."),
                Producto(14, "Canasta gourmet",           650.00, cat, "Canasta con productos gourmet selectos.")
            )
            else -> emptyList()
        }
    }
}
