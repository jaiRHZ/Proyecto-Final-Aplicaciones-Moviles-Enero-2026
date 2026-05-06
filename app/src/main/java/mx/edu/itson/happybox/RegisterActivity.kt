package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

        btnCrearCuenta.setOnClickListener {
            // Lógica de registro aquí
            Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
            finish() // Regresa a la pantalla anterior
        }

        tvVolverLogin.setOnClickListener {
            finish() // Regresa al Login/Main
        }
    }
}