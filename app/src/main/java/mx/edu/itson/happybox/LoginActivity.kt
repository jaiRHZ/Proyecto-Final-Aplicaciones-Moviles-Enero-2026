package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var btnEntrar: Button
    private lateinit var btnRegistrarse: com.google.android.material.button.MaterialButton
    private lateinit var tvSinCuenta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etCorreo      = findViewById(R.id.etCorreo)
        etContrasena  = findViewById(R.id.etContrasena)
        btnEntrar      = findViewById(R.id.btnEntrar)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        // El layout activity_login tiene tvSinCuenta según los strings, reviso el layout
        tvSinCuenta = findViewById(R.id.tvSinCuenta)

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

            // Simulación de login exitoso
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Cerramos login para que no regrese al presionar atrás
        }

        btnRegistrarse.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvSinCuenta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
