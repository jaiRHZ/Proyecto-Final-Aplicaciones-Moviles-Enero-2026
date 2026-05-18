package mx.edu.itson.happybox

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import mx.edu.itson.happybox.model.Producto
import java.util.UUID

class AdminEditProductoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var ivImage: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrecio: TextInputEditText
    private lateinit var etCategoria: TextInputEditText
    private lateinit var etStock: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var switchDisponible: SwitchMaterial
    private lateinit var btnSaveProducto: MaterialButton

    private lateinit var db: FirebaseFirestore
    // private lateinit var storage: FirebaseStorage

    private var productoId: Int = -1
    private var imagenUri: Uri? = null
    private var currentImageUrl: String = ""
    private var currentImageResId: Int = 0

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            ivImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_producto)

        db = FirebaseFirestore.getInstance()
        
        try {
            MediaManager.init(this, mapOf("cloud_name" to "dluszraa2"))
        } catch (e: Exception) {
            // Ya está inicializado
        }

        inicializarVistas()
        
        productoId = intent.getIntExtra("productoId", -1)
        if (productoId != -1) {
            toolbar.title = "Editar Producto"
            cargarDatosProducto(productoId)
        } else {
            toolbar.title = "Nuevo Producto"
        }

        configurarListeners()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarAdminEditProducto)
        toolbar.setNavigationOnClickListener { finish() }

        ivImage = findViewById(R.id.ivAdminEditImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        etNombre = findViewById(R.id.etEditNombre)
        etPrecio = findViewById(R.id.etEditPrecio)
        etCategoria = findViewById(R.id.etEditCategoria)
        etStock = findViewById(R.id.etEditStock)
        etDescripcion = findViewById(R.id.etEditDescripcion)
        switchDisponible = findViewById(R.id.switchDisponible)
        btnSaveProducto = findViewById(R.id.btnSaveProducto)
    }

    private fun cargarDatosProducto(id: Int) {
        db.collection("productos").document(id.toString()).get()
            .addOnSuccessListener { doc ->
                val producto = doc.toObject(Producto::class.java)
                if (producto != null) {
                    etNombre.setText(producto.nombre)
                    etPrecio.setText(producto.precio.toString())
                    etCategoria.setText(producto.categoria)
                    etStock.setText(producto.stock.toString())
                    etDescripcion.setText(producto.descripcion)
                    switchDisponible.isChecked = producto.disponible
                    currentImageUrl = producto.imagenUrl
                    currentImageResId = producto.imagenResId

                    if (producto.imagenUrl.isNotEmpty()) {
                        Glide.with(this).load(producto.imagenUrl).into(ivImage)
                    } else if (producto.imagenResId != 0) {
                        ivImage.setImageResource(producto.imagenResId)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarListeners() {
        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnSaveProducto.setOnClickListener {
            guardarProducto()
        }
    }

    private fun guardarProducto() {
        val nombre = etNombre.text.toString().trim()
        val precioStr = etPrecio.text.toString().trim()
        val categoria = etCategoria.text.toString().trim()
        val stockStr = etStock.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val disponible = switchDisponible.isChecked

        if (nombre.isEmpty() || precioStr.isEmpty() || categoria.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos principales", Toast.LENGTH_SHORT).show()
            return
        }

        val precio = precioStr.toDoubleOrNull() ?: 0.0
        val stock = stockStr.toIntOrNull() ?: 0

        btnSaveProducto.isEnabled = false
        btnSaveProducto.text = "Guardando..."

        if (imagenUri != null) {
            MediaManager.get().upload(imagenUri)
                .unsigned("happybox_preset")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"].toString()
                        guardarEnFirestore(nombre, precio, categoria, descripcion, disponible, stock, url)
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        btnSaveProducto.isEnabled = true
                        btnSaveProducto.text = "Guardar Producto"
                        Toast.makeText(this@AdminEditProductoActivity, "Error al subir imagen: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        } else {
            guardarEnFirestore(nombre, precio, categoria, descripcion, disponible, stock, currentImageUrl)
        }
    }

    private fun guardarEnFirestore(nombre: String, precio: Double, categoria: String, descripcion: String, disponible: Boolean, stock: Int, imagenUrl: String) {
        val newId = if (productoId == -1) System.currentTimeMillis().toInt() else productoId
        
        val producto = Producto(
            id = newId,
            nombre = nombre,
            precio = precio,
            categoria = categoria,
            descripcion = descripcion,
            disponible = disponible,
            stock = stock,
            imagenUrl = imagenUrl,
            imagenResId = currentImageResId 
        )

        db.collection("productos").document(newId.toString())
            .set(producto)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnSaveProducto.isEnabled = true
                btnSaveProducto.text = "Guardar Producto"
                Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}
