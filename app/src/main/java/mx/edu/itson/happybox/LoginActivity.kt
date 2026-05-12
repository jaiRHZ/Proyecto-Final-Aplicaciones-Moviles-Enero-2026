package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var btnEntrar: Button
    private lateinit var btnRegistrarse: MaterialButton
    private lateinit var tvSinCuenta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etCorreo      = findViewById(R.id.etCorreo)
        etContrasena  = findViewById(R.id.etContrasena)
        btnEntrar      = findViewById(R.id.btnEntrar)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        tvSinCuenta    = findViewById(R.id.tvNoTieneCuenta)

        btnEntrar.setOnClickListener {
            val correo     = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEntrar.isEnabled = false
            btnEntrar.text = "Entrando..."

            // Iniciar sesión con Firebase Auth
            auth.signInWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    btnEntrar.isEnabled = true
                    btnEntrar.text = "Entrar"
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
        }

        btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvSinCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
