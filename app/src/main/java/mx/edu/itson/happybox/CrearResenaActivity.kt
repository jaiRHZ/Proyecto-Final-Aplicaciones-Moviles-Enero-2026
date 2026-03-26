// app/src/main/java/mx/edu/itson/happybox/CrearResenaActivity.kt
package mx.edu.itson.happybox

import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CrearResenaActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etTitulo: TextInputEditText
    private lateinit var etComentario: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var btnEnviar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_resena)

        btnBack = findViewById(R.id.btnBackCrearResena)
        etTitulo = findViewById(R.id.etResenaTitulo)
        etComentario = findViewById(R.id.etResenaComentario)
        ratingBar = findViewById(R.id.ratingResena)
        btnEnviar = findViewById(R.id.btnEnviarResena)

        ratingBar.rating = 5f

        btnBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressedDispatcher.onBackPressed()
        }

        btnEnviar.setOnClickListener {
            val titulo = etTitulo.text?.toString()?.trim().orEmpty()
            val comentario = etComentario.text?.toString()?.trim().orEmpty()
            val estrellas = ratingBar.rating.toInt()

            if (titulo.isBlank() || comentario.isBlank()) {
                Toast.makeText(this, "Completa título y comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (estrellas !in 1..5) {
                Toast.makeText(this, "Selecciona de 1 a 5 estrellas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Enviado: $estrellas ★ (simulación)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}