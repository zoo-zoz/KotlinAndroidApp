package com.example.testapp

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class IoTDashboardActivity : AppCompatActivity() {
    
    // 图表组件
    private lateinit var temperatureChart: LineChart
    private lateinit var humidityChart: LineChart
    private lateinit var powerChart: BarChart
    private lateinit var performanceChart: RadarChart
    
    // 仪表盘组件
    private lateinit var gaugeTemperature: GaugeView
    private lateinit var gaugeHumidity: GaugeView
    private lateinit var gaugeCpu: GaugeView
    private lateinit var gaugeMemory: GaugeView
    
    // 数值显示
    private lateinit var tvTemperature: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvPressure: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvCurrent: TextView
    private lateinit var tvPower: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvUpdateTime: TextView
    
    // 数据更新
    private val handler = Handler(Looper.getMainLooper())
    private var isUpdating = false
    private val updateInterval = 1000L // 1秒更新一次
    private val maxDataPoints = 50 // 最多显示50个数据点
    
    // 时间格式化
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iot_dashboard)
        
        // 启用返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "IoT 数据监控仪表盘"
        
        initViews()
        setupCharts()
        startDataUpdate()
    }
    
    private fun initViews() {
        // 图表
        temperatureChart = findViewById(R.id.temperatureChart)
        humidityChart = findViewById(R.id.humidityChart)
        powerChart = findViewById(R.id.powerChart)
        performanceChart = findViewById(R.id.performanceChart)
        
        // 仪表盘
        gaugeTemperature = findViewById(R.id.gaugeTemperature)
        gaugeHumidity = findViewById(R.id.gaugeHumidity)
        gaugeCpu = findViewById(R.id.gaugeCpu)
        gaugeMemory = findViewById(R.id.gaugeMemory)
        
        // 数值显示
        tvTemperature = findViewById(R.id.tvTemperature)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvPressure = findViewById(R.id.tvPressure)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvCurrent = findViewById(R.id.tvCurrent)
        tvPower = findViewById(R.id.tvPower)
        tvStatus = findViewById(R.id.tvStatus)
        tvUpdateTime = findViewById(R.id.tvUpdateTime)
    }
    
    private fun setupCharts() {
        setupTemperatureChart()
        setupHumidityChart()
        setupPowerChart()
        setupPerformanceChart()
        setupGauges()
    }
    
    private fun setupGauges() {
        // 温度仪表 (0-50°C)
        gaugeTemperature.apply {
            setRange(0f, 50f)
            setLabel("温度")
            setUnit("°C")
        }
        
        // 湿度仪表 (0-100%)
        gaugeHumidity.apply {
            setRange(0f, 100f)
            setLabel("湿度")
            setUnit("%")
        }
        
        // CPU仪表 (0-100%)
        gaugeCpu.apply {
            setRange(0f, 100f)
            setLabel("CPU")
            setUnit("%")
        }
        
        // 内存仪表 (0-100%)
        gaugeMemory.apply {
            setRange(0f, 100f)
            setLabel("内存")
            setUnit("%")
        }
    }
    
    private fun setupTemperatureChart() {
        temperatureChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            
            // X轴配置
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
                valueFormatter = TimeAxisValueFormatter()
            }
            
            // 左Y轴配置
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            // 右Y轴禁用
            axisRight.isEnabled = false
            
            // 图例配置
            legend.apply {
                form = Legend.LegendForm.LINE
                textColor = Color.DKGRAY
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            }
            
            // 初始化数据
            data = LineData()
            invalidate()
        }
    }
    
    private fun setupHumidityChart() {
        humidityChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
                valueFormatter = TimeAxisValueFormatter()
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                form = Legend.LegendForm.LINE
                textColor = Color.DKGRAY
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            }
            
            data = LineData()
            invalidate()
        }
    }
    
    private fun setupPowerChart() {
        powerChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.DKGRAY
                valueFormatter = object : ValueFormatter() {
                    private val labels = arrayOf("电压", "电流", "功率", "频率")
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textColor = Color.DKGRAY
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
            
            invalidate()
        }
    }
    
    private fun setupPerformanceChart() {
        performanceChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            setBackgroundColor(Color.WHITE)
            webLineWidth = 1.5f
            webColor = Color.LTGRAY
            webLineWidthInner = 1f
            webColorInner = Color.LTGRAY
            webAlpha = 150
            
            // 设置雷达图的层数
            setSkipWebLineCount(0)
            
            xAxis.apply {
                textColor = Color.DKGRAY
                textSize = 11f
                valueFormatter = object : ValueFormatter() {
                    private val labels = arrayOf("CPU", "内存", "网络", "磁盘", "温度")
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
            }
            
            yAxis.apply {
                textColor = Color.DKGRAY
                textSize = 10f
                setLabelCount(5, true)
                axisMinimum = 0f
                axisMaximum = 100f
                // 格式化 Y 轴标签为百分比
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}%"
                    }
                }
            }
            
            legend.isEnabled = false
            
            // 设置动画
            animateXY(400, 400)
            
            invalidate()
        }
    }
    
    private fun startDataUpdate() {
        isUpdating = true
        updateData()
    }
    
    private fun updateData() {
        if (!isUpdating) return
        
        // 生成模拟数据
        val temperature = 20f + Random.nextFloat() * 15f // 20-35°C
        val humidity = 40f + Random.nextFloat() * 30f // 40-70%
        val pressure = 1000f + Random.nextFloat() * 50f // 1000-1050 hPa
        val voltage = 220f + Random.nextFloat() * 10f // 220-230V
        val current = 5f + Random.nextFloat() * 5f // 5-10A
        val power = voltage * current / 1000f // kW
        
        // 更新数值显示
        tvTemperature.text = String.format("%.1f°C", temperature)
        tvHumidity.text = String.format("%.1f%%", humidity)
        tvPressure.text = String.format("%.1f hPa", pressure)
        tvVoltage.text = String.format("%.1fV", voltage)
        tvCurrent.text = String.format("%.2fA", current)
        tvPower.text = String.format("%.2f kW", power)
        tvUpdateTime.text = "更新时间: ${timeFormat.format(Date())}"
        
        // 更新状态
        tvStatus.text = if (temperature < 30 && humidity < 65) "正常" else "警告"
        tvStatus.setTextColor(
            if (temperature < 30 && humidity < 65) 
                Color.parseColor("#4CAF50") 
            else 
                Color.parseColor("#FF9800")
        )
        
        // 更新图表
        updateTemperatureChart(temperature)
        updateHumidityChart(humidity)
        updatePowerChart(voltage, current, power)
        updatePerformanceChart()
        
        // 更新仪表盘
        updateGauges(temperature, humidity)
        
        // 继续更新
        handler.postDelayed({ updateData() }, updateInterval)
    }
    
    private fun updateTemperatureChart(value: Float) {
        val data = temperatureChart.data
        
        if (data != null) {
            var set = data.getDataSetByIndex(0) as? LineDataSet
            
            if (set == null) {
                set = createTemperatureDataSet()
                data.addDataSet(set)
            }
            
            // 添加新数据点
            data.addEntry(Entry(set.entryCount.toFloat(), value), 0)
            
            // 限制数据点数量
            if (set.entryCount > maxDataPoints) {
                set.removeFirst()
                // 重新索引
                for (i in 0 until set.entryCount) {
                    set.getEntryForIndex(i).x = i.toFloat()
                }
            }
            
            data.notifyDataChanged()
            temperatureChart.notifyDataSetChanged()
            temperatureChart.setVisibleXRangeMaximum(maxDataPoints.toFloat())
            temperatureChart.moveViewToX(data.entryCount.toFloat())
        }
    }
    
    private fun updateHumidityChart(value: Float) {
        val data = humidityChart.data
        
        if (data != null) {
            var set = data.getDataSetByIndex(0) as? LineDataSet
            
            if (set == null) {
                set = createHumidityDataSet()
                data.addDataSet(set)
            }
            
            data.addEntry(Entry(set.entryCount.toFloat(), value), 0)
            
            if (set.entryCount > maxDataPoints) {
                set.removeFirst()
                for (i in 0 until set.entryCount) {
                    set.getEntryForIndex(i).x = i.toFloat()
                }
            }
            
            data.notifyDataChanged()
            humidityChart.notifyDataSetChanged()
            humidityChart.setVisibleXRangeMaximum(maxDataPoints.toFloat())
            humidityChart.moveViewToX(data.entryCount.toFloat())
        }
    }
    
    private fun updatePowerChart(voltage: Float, current: Float, power: Float) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, voltage / 10f)) // 缩放到合适范围
        entries.add(BarEntry(1f, current))
        entries.add(BarEntry(2f, power * 10f))
        entries.add(BarEntry(3f, 50f + Random.nextFloat() * 10f)) // 频率
        
        val dataSet = BarDataSet(entries, "电力参数").apply {
            colors = listOf(
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0")
            )
            valueTextColor = Color.DKGRAY
            valueTextSize = 10f
        }
        
        powerChart.data = BarData(dataSet)
        powerChart.invalidate()
    }
    
    private fun updatePerformanceChart() {
        val entries = ArrayList<RadarEntry>()
        // 生成 0-100 之间的随机值
        entries.add(RadarEntry(30f + Random.nextFloat() * 50f)) // CPU: 30-80%
        entries.add(RadarEntry(40f + Random.nextFloat() * 40f)) // 内存: 40-80%
        entries.add(RadarEntry(20f + Random.nextFloat() * 60f)) // 网络: 20-80%
        entries.add(RadarEntry(50f + Random.nextFloat() * 30f)) // 磁盘: 50-80%
        entries.add(RadarEntry(25f + Random.nextFloat() * 45f)) // 温度: 25-70%
        
        val dataSet = RadarDataSet(entries, "系统性能").apply {
            color = Color.parseColor("#2196F3")
            fillColor = Color.parseColor("#2196F3")
            setDrawFilled(true)
            fillAlpha = 120
            lineWidth = 2.5f
            setDrawHighlightCircleEnabled(true)
            setDrawHighlightIndicators(false)
            valueTextColor = Color.DKGRAY
            valueTextSize = 10f
            // 格式化数值显示为百分比
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%"
                }
            }
        }
        
        performanceChart.data = RadarData(dataSet)
        performanceChart.invalidate()
    }
    
    private fun createTemperatureDataSet(): LineDataSet {
        return LineDataSet(ArrayList(), "温度 (°C)").apply {
            color = Color.parseColor("#F44336")
            setCircleColor(Color.parseColor("#F44336"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 9f
            setDrawFilled(true)
            fillColor = Color.parseColor("#F44336")
            fillAlpha = 50
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER // 平滑曲线
        }
    }
    
    private fun createHumidityDataSet(): LineDataSet {
        return LineDataSet(ArrayList(), "湿度 (%)").apply {
            color = Color.parseColor("#2196F3")
            setCircleColor(Color.parseColor("#2196F3"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 9f
            setDrawFilled(true)
            fillColor = Color.parseColor("#2196F3")
            fillAlpha = 50
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
    }
    
    private fun updateGauges(temperature: Float, humidity: Float) {
        // 更新温度仪表
        gaugeTemperature.setValue(temperature, true)
        
        // 更新湿度仪表
        gaugeHumidity.setValue(humidity, true)
        
        // 更新CPU仪表 (30-80%)
        val cpuUsage = 30f + Random.nextFloat() * 50f
        gaugeCpu.setValue(cpuUsage, true)
        
        // 更新内存仪表 (40-80%)
        val memoryUsage = 40f + Random.nextFloat() * 40f
        gaugeMemory.setValue(memoryUsage, true)
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
    
    override fun onDestroy() {
        super.onDestroy()
        isUpdating = false
        handler.removeCallbacksAndMessages(null)
    }
    
    // 时间轴格式化器
    private class TimeAxisValueFormatter : ValueFormatter() {
        private val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        private val startTime = System.currentTimeMillis()
        
        override fun getFormattedValue(value: Float): String {
            val time = startTime + (value * 1000).toLong()
            return format.format(Date(time))
        }
    }
}
