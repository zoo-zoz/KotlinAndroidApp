package com.example.testapp

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate

class ChartTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_test)

        // 启用返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupLineChart()
        setupBarChart()
        setupPieChart()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupLineChart() {
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        
        val entries = ArrayList<Entry>()
        for (i in 0..10) {
            entries.add(Entry(i.toFloat(), (Math.random() * 100).toFloat()))
        }

        val dataSet = LineDataSet(entries, "温度数据").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
        }

        lineChart.data = LineData(dataSet)
        lineChart.description.text = "折线图示例"
        lineChart.invalidate()
    }

    private fun setupBarChart() {
        val barChart = findViewById<BarChart>(R.id.barChart)
        
        val entries = ArrayList<BarEntry>()
        for (i in 0..5) {
            entries.add(BarEntry(i.toFloat(), (Math.random() * 100).toFloat()))
        }

        val dataSet = BarDataSet(entries, "销售数据").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        barChart.data = BarData(dataSet)
        barChart.description.text = "柱状图示例"
        barChart.invalidate()
    }

    private fun setupPieChart() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(30f, "Android"))
        entries.add(PieEntry(25f, "iOS"))
        entries.add(PieEntry(20f, "Web"))
        entries.add(PieEntry(15f, "Desktop"))
        entries.add(PieEntry(10f, "其他"))

        val dataSet = PieDataSet(entries, "平台分布").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        pieChart.data = PieData(dataSet)
        pieChart.description.text = "饼图示例"
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.invalidate()
    }
}
