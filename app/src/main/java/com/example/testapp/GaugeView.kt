package com.example.testapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 配置参数
    private var minValue = 0f
    private var maxValue = 100f
    private var currentValue = 0f
    private var unit = ""
    private var label = "SCORE"
    
    // 颜色配置
    private var arcColor = Color.parseColor("#4CAF50")
    private var arcBackgroundColor = Color.parseColor("#E0E0E0")
    private var needleColor = Color.parseColor("#2196F3")
    private var textColor = Color.parseColor("#333333")
    
    // 画笔
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val arcBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        strokeCap = Paint.Cap.ROUND
        color = arcBackgroundColor
    }
    
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = needleColor
        strokeWidth = 8f
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
    }
    
    private val scalePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        strokeWidth = 2f
    }
    
    private val scaleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }
    
    // 动画
    private var animatedValue = 0f
    private var targetValue = 0f
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 2f - 60f
        
        // 绘制背景圆弧
        drawBackgroundArc(canvas, centerX, centerY, radius)
        
        // 绘制进度圆弧
        drawProgressArc(canvas, centerX, centerY, radius)
        
        // 绘制刻度
        drawScale(canvas, centerX, centerY, radius)
        
        // 绘制指针
        drawNeedle(canvas, centerX, centerY, radius)
        
        // 绘制中心圆
        drawCenterCircle(canvas, centerX, centerY)
        
        // 绘制文本
        drawText(canvas, centerX, centerY)
    }
    
    private fun drawBackgroundArc(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        // 绘制背景圆弧（从 150° 到 390°，共 240°）
        canvas.drawArc(rect, 150f, 240f, false, arcBackgroundPaint)
    }
    
    private fun drawProgressArc(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        // 计算进度角度
        val progress = (animatedValue - minValue) / (maxValue - minValue)
        val sweepAngle = progress * 240f
        
        // 根据进度改变颜色
        arcPaint.color = when {
            progress < 0.3f -> Color.parseColor("#4CAF50") // 绿色
            progress < 0.7f -> Color.parseColor("#FF9800") // 橙色
            else -> Color.parseColor("#F44336") // 红色
        }
        
        // 绘制进度圆弧
        canvas.drawArc(rect, 150f, sweepAngle, false, arcPaint)
    }
    
    private fun drawScale(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val scaleCount = 11 // 0, 10, 20, ..., 100
        val angleStep = 240f / (scaleCount - 1)
        
        for (i in 0 until scaleCount) {
            val angle = 150f + i * angleStep
            val radian = Math.toRadians(angle.toDouble())
            
            // 刻度线
            val startX = centerX + (radius - 40f) * cos(radian).toFloat()
            val startY = centerY + (radius - 40f) * sin(radian).toFloat()
            val endX = centerX + (radius - 20f) * cos(radian).toFloat()
            val endY = centerY + (radius - 20f) * sin(radian).toFloat()
            
            scalePaint.strokeWidth = if (i % 2 == 0) 4f else 2f
            canvas.drawLine(startX, startY, endX, endY, scalePaint)
            
            // 刻度数字（每隔一个显示）
            if (i % 2 == 0) {
                val value = minValue + (maxValue - minValue) * i / (scaleCount - 1)
                val textX = centerX + (radius - 60f) * cos(radian).toFloat()
                val textY = centerY + (radius - 60f) * sin(radian).toFloat() + 10f
                canvas.drawText(value.toInt().toString(), textX, textY, scaleTextPaint)
            }
        }
    }
    
    private fun drawNeedle(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val progress = (animatedValue - minValue) / (maxValue - minValue)
        val angle = 150f + progress * 240f
        val radian = Math.toRadians(angle.toDouble())
        
        // 指针终点
        val needleLength = radius - 30f
        val endX = centerX + needleLength * cos(radian).toFloat()
        val endY = centerY + needleLength * sin(radian).toFloat()
        
        // 绘制指针（三角形）
        val path = Path()
        val needleWidth = 12f
        
        // 指针基座的两个点
        val baseAngle1 = angle - 90
        val baseAngle2 = angle + 90
        val baseRadius = needleWidth
        
        val base1X = centerX + baseRadius * cos(Math.toRadians(baseAngle1.toDouble())).toFloat()
        val base1Y = centerY + baseRadius * sin(Math.toRadians(baseAngle1.toDouble())).toFloat()
        val base2X = centerX + baseRadius * cos(Math.toRadians(baseAngle2.toDouble())).toFloat()
        val base2Y = centerY + baseRadius * sin(Math.toRadians(baseAngle2.toDouble())).toFloat()
        
        path.moveTo(base1X, base1Y)
        path.lineTo(endX, endY)
        path.lineTo(base2X, base2Y)
        path.close()
        
        canvas.drawPath(path, needlePaint)
    }
    
    private fun drawCenterCircle(canvas: Canvas, centerX: Float, centerY: Float) {
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        canvas.drawCircle(centerX, centerY, 20f, circlePaint)
        
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = needleColor
            strokeWidth = 4f
        }
        canvas.drawCircle(centerX, centerY, 20f, borderPaint)
    }
    
    private fun drawText(canvas: Canvas, centerX: Float, centerY: Float) {
        // 标签
        textPaint.textSize = 32f
        textPaint.color = Color.parseColor("#999999")
        canvas.drawText(label, centerX, centerY + 60f, textPaint)
        
        // 数值
        textPaint.textSize = 72f
        textPaint.color = textColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val valueText = if (unit.isEmpty()) {
            animatedValue.toInt().toString()
        } else {
            "${animatedValue.toInt()}$unit"
        }
        canvas.drawText(valueText, centerX, centerY + 120f, textPaint)
        textPaint.typeface = Typeface.DEFAULT
    }
    
    // 公共方法
    fun setValue(value: Float, animate: Boolean = true) {
        targetValue = value.coerceIn(minValue, maxValue)
        
        if (animate) {
            animateToValue(targetValue)
        } else {
            animatedValue = targetValue
            currentValue = targetValue
            invalidate()
        }
    }
    
    fun setRange(min: Float, max: Float) {
        minValue = min
        maxValue = max
        invalidate()
    }
    
    fun setUnit(unit: String) {
        this.unit = unit
        invalidate()
    }
    
    fun setLabel(label: String) {
        this.label = label
        invalidate()
    }
    
    fun setGaugeColor(color: Int) {
        arcColor = color
        invalidate()
    }
    
    private fun animateToValue(target: Float) {
        val start = animatedValue
        val diff = target - start
        val duration = 1000L
        val startTime = System.currentTimeMillis()
        
        val runnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                
                // 使用缓动函数
                val easeProgress = easeOutCubic(progress)
                animatedValue = start + diff * easeProgress
                
                invalidate()
                
                if (progress < 1f) {
                    postDelayed(this, 16) // ~60 FPS
                } else {
                    animatedValue = target
                    currentValue = target
                }
            }
        }
        
        post(runnable)
    }
    
    private fun easeOutCubic(t: Float): Float {
        val t1 = t - 1f
        return t1 * t1 * t1 + 1f
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 400
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredSize, widthSize)
            else -> desiredSize
        }
        
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredSize, heightSize)
            else -> desiredSize
        }
        
        setMeasuredDimension(width, height)
    }
}
