package mx.edu.itson.happybox.model

import java.io.Serializable

data class Domicilio(
    val id: String = "",
    val userId: String = "",
    val calle: String = "",
    val numero: String = "",
    val colonia: String = "",
    val codigoPostal: String = "",
    val ciudad: String = ""
) : Serializable {
    fun getDireccionCompleta(): String {
        return "$calle $numero, $colonia, C.P. $codigoPostal, $ciudad"
    }
}
