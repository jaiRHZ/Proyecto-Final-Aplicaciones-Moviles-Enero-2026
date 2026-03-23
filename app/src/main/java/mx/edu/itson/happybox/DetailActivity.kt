package mx.edu.itson.happybox

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import mx.edu.itson.happybox.model.CarritoManager
import mx.edu.itson.happybox.model.Producto

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

    private var cantidad = 1
    private var productoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

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
    }

    private fun configurarListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
            Toast.makeText(this, "Agregado al carrito: $cantidad unidades", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica real de CarritoManager
            finish()
        }

        btnBuyNow.setOnClickListener {
            Toast.makeText(this, "Procediendo a la compra", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarCantidad() {
        tvQuantity.text = cantidad.toString()
    }
}
