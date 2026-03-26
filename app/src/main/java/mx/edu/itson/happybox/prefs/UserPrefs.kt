// app/src/main/java/.../prefs/UserPrefs.kt
package mx.edu.itson.happybox.prefs

import android.content.Context

object UserPrefs {
    private const val PREFS_NAME = "happybox_prefs"
    private const val KEY_NOMBRE = "user_nombre"
    private const val DEFAULT_NOMBRE = "Usuario"

    fun getNombre(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NOMBRE, DEFAULT_NOMBRE) ?: DEFAULT_NOMBRE
    }

    fun setNombre(context: Context, nombre: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NOMBRE, nombre).apply()
    }
}