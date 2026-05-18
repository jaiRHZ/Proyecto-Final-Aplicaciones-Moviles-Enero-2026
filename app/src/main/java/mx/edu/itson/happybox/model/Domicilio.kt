package mx.edu.itson.happybox.model

import java.io.Serializable

/**
 * Representa una dirección de entrega registrada por un usuario.
 *
 * Cada instancia se persiste como un documento en la sub-colección
 * `usuarios/{uid}/domicilios` de Firestore. Implementa [Serializable]
 * para poder ser enviada entre actividades mediante [Intent.putExtra].
 *
 * @property id Identificador del documento en Firestore (asignado por la base de datos).
 * @property userId UID del usuario propietario de este domicilio.
 * @property calle Nombre de la calle o avenida.
 * @property numero Número exterior (y opcionalmente interior) del inmueble.
 * @property colonia Nombre de la colonia o fraccionamiento.
 * @property codigoPostal Código postal de cinco dígitos.
 * @property ciudad Nombre de la ciudad o municipio.
 */
data class Domicilio(
    val id: String = "",
    val userId: String = "",
    val calle: String = "",
    val numero: String = "",
    val colonia: String = "",
    val codigoPostal: String = "",
    val ciudad: String = ""
) : Serializable {

    /**
     * Genera una cadena de texto legible con la dirección completa.
     *
     * Se utiliza en [CheckoutActivity] para mostrar la dirección seleccionada
     * y en [PedidoAdapter] para presentar el destino de cada pedido.
     *
     * @return Cadena con el formato: `Calle Num, Colonia, C.P. CP, Ciudad`.
     */
    fun getDireccionCompleta(): String {
        return "$calle $numero, $colonia, C.P. $codigoPostal, $ciudad"
    }
}
