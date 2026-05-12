package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val tvVolverLogin  = findViewById<TextView>(R.id.tvVolverLogin)
        val etNombre       = findViewById<TextInputEditText>(R.id.etNombre)
        val etCorreo       = findViewById<TextInputEditText>(R.id.etCorreo)
        val etTelefono     = findViewById<TextInputEditText>(R.id.etTelefono)
        val etContrasena   = findViewById<TextInputEditText>(R.id.etContrasena)

        btnCrearCuenta.setOnClickListener {
            val nombre    = etNombre.text.toString().trim()
            val correo    = etCorreo.text.toString().trim()
            val telefono  = etTelefono.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Validar campos vacíos
            if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar longitud mínima de contraseña (Firebase requiere mínimo 6)
            if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCrearCuenta.isEnabled = false
            btnCrearCuenta.text = "Creando cuenta..."

            // 1. Crear usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid

                    // 2. Guardar datos extra en Firestore
                    val usuario = User(
                        id       = uid,
                        nombre   = nombre,
                        correo   = correo,
                        telefono = telefono
                    )

                    db.collection("usuarios").document(uid)
                        .set(usuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainHostActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            btnCrearCuenta.isEnabled = true
                            btnCrearCuenta.text = "Crear cuenta"
                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnCrearCuenta.isEnabled = true
                    btnCrearCuenta.text = "Crear cuenta"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        tvVolverLogin.setOnClickListener {
            finish()
        }
    }
}