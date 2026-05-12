package mx.edu.itson.happybox.model

data class Pedido(
    val id: String = "",
    val fecha: Long = 0L,
    val domicilio: Domicilio? = null,
    val metodoPago: String = "",
    val total: Double = 0.0,
    val items: List<Map<String, Any>> = emptyList(),
    val status: String = "Procesando"
) {
    @get:com.google.firebase.firestore.Exclude
    var isExpanded: Boolean = false
}
