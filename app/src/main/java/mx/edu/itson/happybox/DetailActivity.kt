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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.RatingBar
import android.graphics.Paint
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

    private lateinit var rvResenas: RecyclerView

    private lateinit var resenaAdapter: ResenaAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var cantidad = 1
    private var productoId: Int = -1
    private var currentImagenUrl: String = ""



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

        rvResenas          = findViewById(R.id.rvResenas)

        db = FirebaseFirestore.getInstance()
        resenaAdapter = ResenaAdapter(emptyList())
        rvResenas.layoutManager = LinearLayoutManager(this)
        rvResenas.adapter = resenaAdapter
    }

    private var maxStock = 0

    private fun recuperarDatos() {
        productoId = intent.getIntExtra("productoId", -1)
        val nombre = intent.getStringExtra("productoNombre") ?: "Producto"
        val precio = intent.getDoubleExtra("productoPrecio", 0.0)
        val descripcion = intent.getStringExtra("productoDescription") ?: ""
        val imagenRes = intent.getIntExtra("productoImagenRes", R.drawable.ic_placeholder_producto)
        val imagenUrl = intent.getStringExtra("productoImagenUrl") ?: ""
        currentImagenUrl = imagenUrl

        tvTitle.text = nombre
        tvPrice.text = String.format("$%.2f", precio)
        tvDescription.text = descripcion
        
        if (currentImagenUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(currentImagenUrl)
                .placeholder(R.drawable.ic_placeholder_producto)
                .into(ivProduct)
        } else {
            ivProduct.setImageResource(if (imagenRes != 0) imagenRes else R.drawable.ic_placeholder_producto)
        }
        
        tvResenas.paintFlags = tvResenas.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        if (productoId != -1) {
            db.collection("productos").document(productoId.toString()).get()
                .addOnSuccessListener { doc ->
                    val producto = doc.toObject(mx.edu.itson.happybox.model.Producto::class.java)
                    if (producto != null) {
                        maxStock = producto.stock
                        
                        // Cargar imagen de URL si existe
                        if (producto.imagenUrl.isNotEmpty() && producto.imagenUrl != currentImagenUrl) {
                            currentImagenUrl = producto.imagenUrl
                            com.bumptech.glide.Glide.with(this@DetailActivity)
                                .load(producto.imagenUrl)
                                .placeholder(R.drawable.ic_placeholder_producto)
                                .into(ivProduct)
                        }

                        // Validar disponibilidad
                        if (!producto.disponible || producto.stock <= 0) {
                            tvPrice.text = "No Disponible"
                            tvPrice.setTextColor(getColor(R.color.danger))
                            btnAddToCart.isEnabled = false
                            btnBuyNow.isEnabled = false
                            btnPlus.isEnabled = false
                            btnMinus.isEnabled = false
                        }
                    }
                }
        }

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
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Inicia sesión para dejar una reseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mostrarModalResena(uid)
        }

        btnPlus.setOnClickListener {
            if (maxStock == 0 || cantidad < maxStock) {
                cantidad++
                actualizarCantidad()
            } else {
                Toast.makeText(this, "Stock máximo alcanzado", Toast.LENGTH_SHORT).show()
            }
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
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Inicia sesión para comprar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("isDirectBuy", true)
                putExtra("productoId", productoId)
                putExtra("productoNombre", tvTitle.text.toString())
                putExtra("productoPrecio", tvPrice.text.toString().replace("$", "").toDoubleOrNull() ?: 0.0)
                putExtra("productoImagenRes", this@DetailActivity.intent.getIntExtra("productoImagenRes", R.drawable.ic_placeholder_producto))
                putExtra("productoImagenUrl", currentImagenUrl)
                putExtra("cantidad", cantidad)
            }
            startActivity(intent)
        }
    }

    private fun mostrarModalResena(uid: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_resena, null)
        dialog.setContentView(view)

        val tvTitleModal = view.findViewById<TextView>(R.id.tvDialogResenaTitle)
        tvTitleModal.text = "Escribir Reseña"

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingEditResena)
        val etTitulo = view.findViewById<TextInputEditText>(R.id.etEditTitulo)
        val etComentario = view.findViewById<TextInputEditText>(R.id.etEditComentario)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveEdit)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val titulo = etTitulo.text?.toString()?.trim() ?: ""
            val comentario = etComentario.text?.toString()?.trim() ?: ""
            val rating = ratingBar.rating.toInt()

            if (titulo.isEmpty() || comentario.isEmpty() || rating == 0) {
                Toast.makeText(this, "Completa todos los campos y la calificación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("usuarios").document(uid).get().addOnSuccessListener { userDoc ->
                val autorNombre = userDoc.getString("nombre") ?: "Usuario Anónimo"
                val autorAvatar = "" 

                val resenaRef = db.collection("productos").document(productoId.toString()).collection("resenas").document()

                val nuevaResena = mapOf(
                    "id" to resenaRef.id,
                    "productoId" to productoId,
                    "autorUid" to uid,
                    "autorNombre" to autorNombre,
                    "autorAvatar" to autorAvatar,
                    "rating" to rating,
                    "titulo" to titulo,
                    "comentario" to comentario,
                    "fecha" to System.currentTimeMillis()
                )

                btnSave.isEnabled = false
                btnSave.text = "Guardando..."

                resenaRef.set(nuevaResena)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Reseña enviada!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarResenas()
                    }
                    .addOnFailureListener { e ->
                        Log.e("DetailActivity", "Error al crear reseña", e)
                        Toast.makeText(this, "Error al guardar reseña", Toast.LENGTH_SHORT).show()
                        btnSave.isEnabled = true
                        btnSave.text = "Guardar"
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener perfil", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun actualizarCantidad() {
        tvQuantity.text = cantidad.toString()
    }
}
