package mx.edu.itson.happybox

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)

        btnCrearCuenta.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Validar que ningún campo esté vacío
            if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Todos los campos están llenos, proceder con el registro
            Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }

        tvVolverLogin.setOnClickListener {
            finish() // Regresa al Login/Main
        }
    }
}