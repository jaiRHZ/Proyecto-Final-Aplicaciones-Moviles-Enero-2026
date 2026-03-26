// app/src/main/java/mx/edu/itson/happybox/EditarNombreActivity.kt
package mx.edu.itson.happybox

import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import mx.edu.itson.happybox.prefs.UserPrefs

class EditarNombreActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_nombre)

        btnBack = findViewById(R.id.btnBackEditarNombre)
        etNombre = findViewById(R.id.etEditarNombre)
        btnGuardar = findViewById(R.id.btnGuardarNombre)

        etNombre.setText(UserPrefs.getNombre(this))

        btnBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressedDispatcher.onBackPressed()
        }

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text?.toString()?.trim().orEmpty()

            if (nuevoNombre.length < 2) {
                Toast.makeText(this, "Ingresa un nombre válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserPrefs.setNombre(this, nuevoNombre)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}