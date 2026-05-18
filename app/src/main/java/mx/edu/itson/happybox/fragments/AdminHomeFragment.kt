package mx.edu.itson.happybox.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHomeFragment : Fragment() {

    private lateinit var ventasChart: BarChart
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        ventasChart = view.findViewById(R.id.ventasChart)

        setupChart()
        loadVentasData()
    }

    private fun setupChart() {
        ventasChart.description.isEnabled = false
        ventasChart.setDrawGridBackground(false)
        ventasChart.setDrawBarShadow(false)
        ventasChart.setPinchZoom(false)
        
        val xAxis = ventasChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        
        ventasChart.axisRight.isEnabled = false
    }

    private fun loadVentasData() {
        if (!isAdded) return
        db.collection("usuarios").get()
            .addOnSuccessListener { users ->
                if (!isAdded) return@addOnSuccessListener
                val salesByDate = mutableMapOf<String, Float>()
                val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                var procesados = 0
                val totalUsuarios = users.size()

                if (totalUsuarios == 0) {
                    return@addOnSuccessListener
                }

                for (userDoc in users) {
                    db.collection("usuarios").document(userDoc.id).collection("pedidos").get()
                        .addOnSuccessListener { pedidosResult ->
                            for (doc in pedidosResult) {
                                val total = doc.getDouble("total")?.toFloat() ?: 0f
                                val fechaLong = doc.getLong("fecha") ?: 0L
                                if (fechaLong > 0) {
                                    val dateStr = sdf.format(Date(fechaLong))
                                    salesByDate[dateStr] = (salesByDate[dateStr] ?: 0f) + total
                                }
                            }
                            procesados++

                            if (procesados == totalUsuarios) {
                                if (!isAdded) return@addOnSuccessListener
                                buildChart(salesByDate)
                            }
                        }
                        .addOnFailureListener {
                            procesados++
                            if (procesados == totalUsuarios && isAdded) {
                                buildChart(salesByDate)
                            }
                        }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error al cargar gráfica", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun buildChart(salesByDate: Map<String, Float>) {
        val sortedKeys = salesByDate.keys.toList().sorted()
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        for ((index, key) in sortedKeys.withIndex()) {
            entries.add(BarEntry(index.toFloat(), salesByDate[key] ?: 0f))
            labels.add(key)
        }

        if (entries.isNotEmpty()) {
            val dataSet = BarDataSet(entries, "Ventas Diarias ($)")
            dataSet.color = requireContext().getColor(R.color.happybox_primary)
            dataSet.valueTextSize = 10f

            val data = BarData(dataSet)
            data.barWidth = 0.5f

            ventasChart.data = data
            ventasChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            ventasChart.invalidate() 
        }
    }
}
