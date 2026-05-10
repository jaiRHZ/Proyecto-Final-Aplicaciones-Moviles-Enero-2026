package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.adapter.ProductoListaAdapter
import mx.edu.itson.happybox.adapter.ProductoSugeridoAdapter
import mx.edu.itson.happybox.model.Producto
import mx.edu.itson.happybox.model.ProductoSeeder

class HomeActivity : AppCompatActivity() {

    private lateinit var etBuscar: EditText
    private lateinit var cardPeluches: Chip
    private lateinit var cardGlobos: Chip
    private lateinit var cardTazas: Chip
    private lateinit var cardDetalles: Chip
    private lateinit var cardRegalos: Chip
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var ivCart: ImageView
    private lateinit var ivProfile: ImageView
    private lateinit var rvSugeridos: RecyclerView
    private lateinit var rvTodosProductos: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        inicializarVistas()
        configurarClickListeners()
        configurarBuscador()
        configurarBottomNav()
        cargarProductos()
    }

    private fun inicializarVistas() {
        etBuscar      = findViewById(R.id.etBuscar)
        cardPeluches  = findViewById<Chip>(R.id.cardPeluches)
        cardGlobos    = findViewById<Chip>(R.id.cardGlobos)
        cardTazas     = findViewById<Chip>(R.id.cardTazas)
        cardDetalles  = findViewById<Chip>(R.id.cardDetalles)
        cardRegalos   = findViewById<Chip>(R.id.cardRegalos)
        bottomNav     = findViewById(R.id.bottomNavHome)
        ivCart        = findViewById(R.id.ivCart)
        ivProfile     = findViewById(R.id.ivProfile)
        rvSugeridos       = findViewById(R.id.rvSugeridos)
        rvTodosProductos  = findViewById(R.id.rvTodosProductos)
        db                = FirebaseFirestore.getInstance()
    }

    private fun configurarClickListeners() {
        cardPeluches.setOnClickListener { irAProductos(categoria = "Peluches") }
        cardGlobos.setOnClickListener   { irAProductos(categoria = "Globos") }
        cardTazas.setOnClickListener    { irAProductos(categoria = "Tazas") }
        cardDetalles.setOnClickListener { irAProductos(categoria = "Detalles") }
        cardRegalos.setOnClickListener  { irAProductos(categoria = "Regalos") }

        ivCart.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        ivProfile.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }
    }

    private fun configurarBuscador() {
        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val consulta = etBuscar.text.toString().trim()
                if (consulta.isNotEmpty()) {
                    irAProductos(query = consulta)
                }
                true
            } else {
                false
            }
        }
    }

    private fun irAProductos(categoria: String? = null, query: String? = null) {
        val intent = Intent(this, ProductosActivity::class.java)
        categoria?.let { intent.putExtra("categoria", it) }
        query?.let { intent.putExtra("query", it) }
        startActivity(intent)
    }

    /** Una sola consulta a Firestore alimenta tanto el carrusel como la lista completa */
    private fun cargarProductos() {
        rvSugeridos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTodosProductos.layoutManager = GridLayoutManager(this, 2)

        ProductoSeeder.sembrar(db) {
            db.collection("productos")
                .get()
                .addOnSuccessListener { snapshot ->
                    val disponibles = snapshot.documents
                        .mapNotNull { it.toObject(Producto::class.java) }
                        .filter { it.disponible }

                    // Carrusel: 6 aleatorios
                    rvSugeridos.adapter = ProductoSugeridoAdapter(
                        context  = this,
                        productos = disponibles.shuffled().take(6),
                        onClick  = { irADetalle(it) }
                    )

                    // Lista completa: todos ordenados por nombre
                    rvTodosProductos.adapter = ProductoListaAdapter(
                        context  = this,
                        productos = disponibles.sortedBy { it.nombre },
                        onClick  = { irADetalle(it) }
                    )
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

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navInicio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> true
                R.id.navBuscar -> {
                    etBuscar.requestFocus()
                    true
                }
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
}
