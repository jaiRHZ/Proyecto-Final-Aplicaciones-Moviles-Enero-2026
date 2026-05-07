// app/src/main/java/mx/edu/itson/happybox/CrearResenaActivity.kt
package mx.edu.itson.happybox

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.model.Resena

class CrearResenaActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CrearResena"
    }

    private lateinit var btnBack: ImageButton
    private lateinit var etTitulo: TextInputEditText
    private lateinit var etComentario: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var btnEnviar: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var productoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_resena)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // Recuperar el productoId enviado desde DetailActivity
        productoId = intent.getIntExtra("productoId", -1)
        Log.d(TAG, "Abierto con productoId=$productoId | usuario=${auth.currentUser?.uid}")

        btnBack      = findViewById(R.id.btnBackCrearResena)
        etTitulo     = findViewById(R.id.etResenaTitulo)
        etComentario = findViewById(R.id.etResenaComentario)
        ratingBar    = findViewById(R.id.ratingResena)
        btnEnviar    = findViewById(R.id.btnEnviarResena)

        ratingBar.rating = 5f

        btnBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressedDispatcher.onBackPressed()
        }

        btnEnviar.setOnClickListener {
            val titulo     = etTitulo.text?.toString()?.trim().orEmpty()
            val comentario = etComentario.text?.toString()?.trim().orEmpty()
            val estrellas  = ratingBar.rating.toInt()

            Log.d(TAG, "Enviando: titulo='$titulo' rating=$estrellas productoId=$productoId")

            // Validaciones
            if (titulo.isBlank() || comentario.isBlank()) {
                Toast.makeText(this, "Completa título y comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (estrellas !in 1..5) {
                Toast.makeText(this, "Selecciona de 1 a 5 estrellas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (productoId == -1) {
                Log.e(TAG, "productoId es -1, no se recibió correctamente desde DetailActivity")
                Toast.makeText(this, "Error: producto no identificado (id=-1)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val usuario = auth.currentUser
            if (usuario == null) {
                Log.e(TAG, "currentUser es null — sesión no iniciada")
                Toast.makeText(this, "Debes iniciar sesión para reseñar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEnviar.isEnabled = false
            btnEnviar.text = "Enviando..."

            // Obtener nombre del usuario desde Firestore
            db.collection("usuarios").document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val nombreAutor = doc.getString("nombre") ?: usuario.email ?: "Anónimo"
                    Log.d(TAG, "Autor recuperado: '$nombreAutor'")
                    guardarResena(titulo, comentario, estrellas, usuario.uid, nombreAutor)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "No se pudo obtener nombre del autor: ${e.message}. Usando email.")
                    guardarResena(titulo, comentario, estrellas, usuario.uid, usuario.email ?: "Anónimo")
                }
        }
    }

    private fun guardarResena(
        titulo: String,
        comentario: String,
        estrellas: Int,
        uid: String,
        nombreAutor: String
    ) {
        // Ruta: productos/{productoId}/resenas/{autoId}
        val resenaRef = db.collection("productos")
            .document(productoId.toString())
            .collection("resenas")
            .document()   // auto-ID

        Log.d(TAG, "Guardando en: ${resenaRef.path}")

        val resena = Resena(
            id          = resenaRef.id,
            productoId  = productoId,
            titulo      = titulo,
            comentario  = comentario,
            rating      = estrellas,
            autorUid    = uid,
            autorNombre = nombreAutor,
            fecha       = System.currentTimeMillis()
        )

        resenaRef.set(resena)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Reseña guardada en ${resenaRef.path}")
                Toast.makeText(this, "¡Reseña enviada! $estrellas ★", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al guardar reseña: ${e.message}", e)
                btnEnviar.isEnabled = true
                btnEnviar.text = "Enviar"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}