package mx.edu.itson.happybox.model

/**
 * Representa un pedido generado por un cliente en HappyBox.
 *
 * Se persiste como documento en la sub-colección `usuarios/{uid}/pedidos` de Firestore.
 * El campo [status] es leído por [BadgeUtils] para actualizar el badge de pedidos
 * pendientes en el panel de administración.
 *
 * @property id Identificador único UUID generado en [CheckoutActivity] al momento
 *              de confirmar el pedido.
 * @property fecha Marca de tiempo en milisegundos ([System.currentTimeMillis]) que
 *                 indica cuándo fue creado el pedido.
 * @property domicilio Dirección de entrega seleccionada por el cliente. Puede ser
 *                     `null` si el documento en Firestore está incompleto.
 * @property metodoPago Método de pago elegido por el cliente (ej. "Efectivo",
 *                      "Tarjeta de Crédito").
 * @property total Monto total cobrado al cliente, calculado por [PrecioUtils].
 * @property items Lista de mapas con los datos de cada artículo comprado.
 *                 Cada mapa contiene las claves: `productoId`, `nombre`, `cantidad`, `subtotal`.
 * @property status Estado actual del pedido. Los valores esperados son:
 *                  `"Procesando"` (valor por defecto al crear), `"En camino"`,
 *                  `"Entregado"` y `"Cancelado"`.
 */
data class Pedido(
    val id: String = "",
    val fecha: Long = 0L,
    val domicilio: Domicilio? = null,
    val metodoPago: String = "",
    val total: Double = 0.0,
    val items: List<Map<String, Any>> = emptyList(),
    val status: String = "Procesando"
) {
    /**
     * Controla si la tarjeta del pedido en [PedidoAdapter] muestra u oculta
     * el panel de detalles (artículos y método de pago).
     *
     * Este campo es ignorado por Firestore gracias a la anotación [@Exclude],
     * ya que solo sirve para manejar el estado de la UI en memoria.
     */
    @get:com.google.firebase.firestore.Exclude
    var isExpanded: Boolean = false
}
