package com.example.testapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sqrt

class PathAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 路径点列表
    private val pathPoints = mutableListOf<PointF>()
    
    // 移动的对象列表
    private val movingObjects = mutableListOf<MovingObject>()
    
    // 地图元素
    private val mapBuildings = mutableListOf<MapBuilding>()
    private val mapRoads = mutableListOf<MapRoad>()
    private val mapAreas = mutableListOf<MapArea>()
    private var showMap = true
    
    // 画笔
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }
    
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }
    
    private val pointBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    private val movingObjectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722")
        style = Paint.Style.FILL
    }
    
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722")
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    
    // 地图画笔
    private val buildingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val buildingBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCCCCC")
        style = Paint.Style.FILL
    }
    
    private val roadLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
    }
    
    private val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val areaTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    // 路径
    private val path = Path()
    
    // 动画
    private var currentAnimator: ValueAnimator? = null
    
    init {
        setBackgroundColor(Color.parseColor("#E8F5E9"))
        initializeMap()
    }
    
    private fun initializeMap() {
        // 创建示例地图（可以根据实际需求调整）
        // 这里创建一个简单的城市地图布局
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制地图
        if (showMap) {
            drawMap(canvas)
        }
        
        // 绘制路径
        drawPath(canvas)
        
        // 绘制路径点
        drawPathPoints(canvas)
        
        // 绘制移动对象
        drawMovingObjects(canvas)
    }
    
    private fun drawMap(canvas: Canvas) {
        // 绘制区域
        mapAreas.forEach { area ->
            areaPaint.color = area.color
            canvas.drawRect(area.rect, areaPaint)
            
            // 绘制区域名称
            if (area.name.isNotEmpty()) {
                canvas.drawText(
                    area.name,
                    area.rect.centerX(),
                    area.rect.centerY() + 10f,
                    areaTextPaint
                )
            }
        }
        
        // 绘制道路
        mapRoads.forEach { road ->
            roadPaint.color = road.color
            canvas.drawRect(road.rect, roadPaint)
            
            // 绘制道路中线
            if (road.isHorizontal) {
                val centerY = road.rect.centerY()
                canvas.drawLine(road.rect.left, centerY, road.rect.right, centerY, roadLinePaint)
            } else {
                val centerX = road.rect.centerX()
                canvas.drawLine(centerX, road.rect.top, centerX, road.rect.bottom, roadLinePaint)
            }
        }
        
        // 绘制建筑物
        mapBuildings.forEach { building ->
            buildingPaint.color = building.color
            canvas.drawRect(building.rect, buildingPaint)
            canvas.drawRect(building.rect, buildingBorderPaint)
            
            // 绘制建筑物名称
            if (building.name.isNotEmpty()) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FFFFFF")
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    building.name,
                    building.rect.centerX(),
                    building.rect.centerY() + 8f,
                    textPaint
                )
            }
        }
    }
    
    private fun drawPath(canvas: Canvas) {
        if (pathPoints.size < 2) return
        
        path.reset()
        path.moveTo(pathPoints[0].x, pathPoints[0].y)
        
        for (i in 1 until pathPoints.size) {
            path.lineTo(pathPoints[i].x, pathPoints[i].y)
        }
        
        canvas.drawPath(path, pathPaint)
    }
    
    private fun drawPathPoints(canvas: Canvas) {
        pathPoints.forEachIndexed { index, point ->
            // 绘制点
            canvas.drawCircle(point.x, point.y, 12f, pointPaint)
            canvas.drawCircle(point.x, point.y, 12f, pointBorderPaint)
            
            // 绘制标签
            canvas.drawText("P$index", point.x, point.y - 25f, labelPaint)
        }
    }
    
    private fun drawMovingObjects(canvas: Canvas) {
        movingObjects.forEach { obj ->
            // 绘制光晕效果
            val glowRadius = 25f + (System.currentTimeMillis() % 1000) / 1000f * 10f
            glowPaint.alpha = (100 - (System.currentTimeMillis() % 1000) / 10).toInt()
            canvas.drawCircle(obj.currentX, obj.currentY, glowRadius, glowPaint)
            
            // 绘制移动对象
            canvas.drawCircle(obj.currentX, obj.currentY, 15f, movingObjectPaint)
            
            // 绘制对象ID
            textPaint.color = Color.WHITE
            canvas.drawText(obj.id.toString(), obj.currentX, obj.currentY + 10f, textPaint)
            
            // 绘制轨迹
            if (obj.trail.size > 1) {
                val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FF5722")
                    strokeWidth = 2f
                    style = Paint.Style.STROKE
                    alpha = 100
                }
                
                val trailPath = Path()
                trailPath.moveTo(obj.trail[0].x, obj.trail[0].y)
                for (i in 1 until obj.trail.size) {
                    trailPath.lineTo(obj.trail[i].x, obj.trail[i].y)
                }
                canvas.drawPath(trailPath, trailPaint)
            }
        }
        
        // 持续刷新以显示光晕动画
        if (movingObjects.isNotEmpty()) {
            postInvalidateDelayed(16)
        }
    }
    
    // 添加路径点
    fun addPathPoint(x: Float, y: Float) {
        pathPoints.add(PointF(x, y))
        invalidate()
    }
    
    // 清除所有路径点
    fun clearPathPoints() {
        pathPoints.clear()
        invalidate()
    }
    
    // 获取路径点数量
    fun getPathPointCount(): Int = pathPoints.size
    
    // 添加移动对象
    fun addMovingObject(id: Int, startX: Float, startY: Float) {
        movingObjects.add(MovingObject(id, startX, startY))
        invalidate()
    }
    
    // 移动对象到指定路径点
    fun moveObjectToPoint(objectId: Int, pointIndex: Int, duration: Long = 2000) {
        if (pointIndex < 0 || pointIndex >= pathPoints.size) return
        
        val obj = movingObjects.find { it.id == objectId } ?: return
        val targetPoint = pathPoints[pointIndex]
        
        animateObjectToPosition(obj, targetPoint.x, targetPoint.y, duration)
    }
    
    // 移动对象到指定坐标
    fun moveObjectToCoordinate(objectId: Int, x: Float, y: Float, duration: Long = 2000) {
        val obj = movingObjects.find { it.id == objectId } ?: return
        animateObjectToPosition(obj, x, y, duration)
    }
    
    // 移动对象沿路径
    fun moveObjectAlongPath(objectId: Int, duration: Long = 5000) {
        if (pathPoints.size < 2) return
        
        val obj = movingObjects.find { it.id == objectId } ?: return
        
        currentAnimator?.cancel()
        
        // 计算总路径长度
        var totalLength = 0f
        val segments = mutableListOf<PathSegment>()
        
        for (i in 0 until pathPoints.size - 1) {
            val start = pathPoints[i]
            val end = pathPoints[i + 1]
            val length = distance(start, end)
            segments.add(PathSegment(start, end, totalLength, totalLength + length))
            totalLength += length
        }
        
        obj.trail.clear()
        
        currentAnimator = ValueAnimator.ofFloat(0f, totalLength).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            
            addUpdateListener { animator ->
                val currentDistance = animator.animatedValue as Float
                
                // 找到当前所在的路径段
                val segment = segments.find { 
                    currentDistance >= it.startDistance && currentDistance <= it.endDistance 
                }
                
                segment?.let {
                    val segmentProgress = (currentDistance - it.startDistance) / (it.endDistance - it.startDistance)
                    obj.currentX = it.start.x + (it.end.x - it.start.x) * segmentProgress
                    obj.currentY = it.start.y + (it.end.y - it.start.y) * segmentProgress
                    
                    // 添加轨迹点
                    if (obj.trail.isEmpty() || distance(obj.trail.last(), PointF(obj.currentX, obj.currentY)) > 5f) {
                        obj.trail.add(PointF(obj.currentX, obj.currentY))
                        if (obj.trail.size > 100) {
                            obj.trail.removeAt(0)
                        }
                    }
                    
                    invalidate()
                }
            }
            
            start()
        }
    }
    
    // 停止所有动画
    fun stopAnimation() {
        currentAnimator?.cancel()
        currentAnimator = null
    }
    
    // 清除所有移动对象
    fun clearMovingObjects() {
        stopAnimation()
        movingObjects.clear()
        invalidate()
    }
    
    // 重置所有
    fun reset() {
        stopAnimation()
        pathPoints.clear()
        movingObjects.clear()
        invalidate()
    }
    
    private fun animateObjectToPosition(obj: MovingObject, targetX: Float, targetY: Float, duration: Long) {
        currentAnimator?.cancel()
        
        val startX = obj.currentX
        val startY = obj.currentY
        
        obj.trail.clear()
        
        currentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                obj.currentX = startX + (targetX - startX) * progress
                obj.currentY = startY + (targetY - startY) * progress
                
                // 添加轨迹点
                if (obj.trail.isEmpty() || distance(obj.trail.last(), PointF(obj.currentX, obj.currentY)) > 5f) {
                    obj.trail.add(PointF(obj.currentX, obj.currentY))
                    if (obj.trail.size > 100) {
                        obj.trail.removeAt(0)
                    }
                }
                
                invalidate()
            }
            
            start()
        }
    }
    
    private fun distance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
    
    // 移动对象数据类
    private data class MovingObject(
        val id: Int,
        var currentX: Float,
        var currentY: Float,
        val trail: MutableList<PointF> = mutableListOf()
    )
    
    // 路径段数据类
    private data class PathSegment(
        val start: PointF,
        val end: PointF,
        val startDistance: Float,
        val endDistance: Float
    )
    
    // 地图元素数据类
    data class MapBuilding(
        val rect: RectF,
        val name: String,
        val color: Int
    )
    
    data class MapRoad(
        val rect: RectF,
        val color: Int,
        val isHorizontal: Boolean
    )
    
    data class MapArea(
        val rect: RectF,
        val name: String,
        val color: Int
    )
    
    // 公共方法：设置地图数据
    fun setMapData(buildings: List<MapBuilding>, roads: List<MapRoad>, areas: List<MapArea>) {
        mapBuildings.clear()
        mapRoads.clear()
        mapAreas.clear()
        
        mapBuildings.addAll(buildings)
        mapRoads.addAll(roads)
        mapAreas.addAll(areas)
        
        invalidate()
    }
    
    fun toggleMapVisibility() {
        showMap = !showMap
        invalidate()
    }
    
    fun setMapVisibility(visible: Boolean) {
        showMap = visible
        invalidate()
    }
}
