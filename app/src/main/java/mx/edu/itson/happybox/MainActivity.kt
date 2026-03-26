package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var btnIniciarSesion: Button
    private lateinit var btnRegistrarse: MaterialButton
    private lateinit var tvOlvidaste: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        btnRegistrarse   = findViewById(R.id.btnRegistrarse)
        tvOlvidaste      = findViewById(R.id.tvOlvidaste)

        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegistrarse.setOnClickListener {
        }

        tvOlvidaste.setOnClickListener {
        }
    }
}