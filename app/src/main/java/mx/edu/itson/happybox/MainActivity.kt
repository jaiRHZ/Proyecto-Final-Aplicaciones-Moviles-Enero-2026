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

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    val rol = doc.getString("rol") ?: "cliente"
                    if (rol == "admin") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainHostActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this, MainHostActivity::class.java))
                    finish()
                }
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