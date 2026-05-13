package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var btnIniciarSesion: Button
    private lateinit var btnRegistrarse: MaterialButton
    private lateinit var tvOlvidaste: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainHostActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        btnRegistrarse   = findViewById(R.id.btnRegistrarse)
        tvOlvidaste      = findViewById(R.id.tvOlvidaste)

        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegistrarse.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvOlvidaste.setOnClickListener {
            // Implementar recuperación de contraseña si es necesario
        }
    }
}