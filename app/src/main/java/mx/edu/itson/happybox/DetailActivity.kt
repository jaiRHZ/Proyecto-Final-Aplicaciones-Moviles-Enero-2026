// app/src/main/java/mx/edu/itson/happybox/DetailActivity.kt
package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import mx.edu.itson.happybox.adapter.ResenaAdapter
import mx.edu.itson.happybox.model.Resena
import android.util.Log

class DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var ivProduct: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var btnMinus: MaterialButton
    private lateinit var btnPlus: MaterialButton
    private lateinit var btnAddToCart: MaterialButton
    private lateinit var btnBuyNow: MaterialButton

    private lateinit var tvResenas: TextView
    private lateinit var chipEscribirResena: Chip
    private lateinit var rvResenas: RecyclerView

    private lateinit var resenaAdapter: ResenaAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var cantidad = 1
    private var productoId: Int = -1

    private val crearResenaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "¡Reseña enviada!", Toast.LENGTH_SHORT).show()
                cargarResenas()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        auth = FirebaseAuth.getInstance()

        inicializarVistas()
        recuperarDatos()
        configurarListeners()
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBackDetail)
        ivProduct = findViewById(R.id.ivProductDetail)
        tvTitle = findViewById(R.id.tvDetailTitle)
        tvPrice = findViewById(R.id.tvDetailPrice)
        tvDescription = findViewById(R.id.tvDetailDescription)
        tvQuantity = findViewById(R.id.tvQuantity)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBuyNow = findViewById(R.id.btnBuyNow)

        tvResenas          = findViewById(R.id.tvDetailReviews)
        chipEscribirResena = findViewById(R.id.chipEscribirResena)
        rvResenas          = findViewById(R.id.rvResenas)

        db = FirebaseFirestore.getInstance()
        resenaAdapter = ResenaAdapter(emptyList())
        rvResenas.layoutManager = LinearLayoutManager(this)
        rvResenas.adapter = resenaAdapter
    }

    private fun recuperarDatos() {
        productoId = intent.getIntExtra("productoId", -1)
        val nombre = intent.getStringExtra("productoNombre") ?: "Producto"
        val precio = intent.getDoubleExtra("productoPrecio", 0.0)
        val descripcion = intent.getStringExtra("productoDescription") ?: ""
        val imagenRes = intent.getIntExtra("productoImagenRes", R.drawable.ic_placeholder_producto)

        tvTitle.text = nombre
        tvPrice.text = String.format("$%.2f", precio)
        tvDescription.text = descripcion
        ivProduct.setImageResource(if (imagenRes != 0) imagenRes else R.drawable.ic_placeholder_producto)

        cargarResenas()
    }

    private fun cargarResenas() {
        if (productoId == -1) return

        db.collection("productos").document(productoId.toString()).collection("resenas")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Resena>()
                for (doc in result) {
                    val resena = doc.toObject(Resena::class.java)
                    lista.add(resena)
                }
                resenaAdapter.actualizarLista(lista)

                if (lista.isNotEmpty()) {
                    val promedio = lista.map { it.rating }.average()
                    tvResenas.text = String.format("%.1f (%d reseñas)", promedio, lista.size)
                } else {
                    tvResenas.text = "0 (0 reseñas)"
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailActivity", "Error al cargar reseñas", e)
            }
    }

    private fun configurarListeners() {
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvResenas.setOnClickListener {
            val intent = Intent(this, CrearResenaActivity::class.java).apply {
                putExtra("productoId", productoId)
            }
            crearResenaLauncher.launch(intent)
        }

        chipEscribirResena.setOnClickListener {
            val intent = Intent(this, CrearResenaActivity::class.java).apply {
                putExtra("productoId", productoId)
            }
            crearResenaLauncher.launch(intent)
        }

        btnPlus.setOnClickListener {
            cantidad++
            actualizarCantidad()
        }

        btnMinus.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                actualizarCantidad()
            }
        }

        btnAddToCart.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAddToCart.isEnabled = false

            val cartData = mapOf(
                "productoId" to productoId,
                "cantidad" to cantidad,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("usuarios").document(uid).collection("carrito").document(productoId.toString())
                .set(cartData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Se agregó exitosamente al carrito", Toast.LENGTH_SHORT).show()
                    btnAddToCart.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Log.e("DetailActivity", "Error al agregar al carrito", e)
                    Toast.makeText(this, "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
                    btnAddToCart.isEnabled = true
                }
        }

        btnBuyNow.setOnClickListener {
            Toast.makeText(this, "Procediendo a la compra", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarCantidad() {
        tvQuantity.text = cantidad.toString()
    }
}