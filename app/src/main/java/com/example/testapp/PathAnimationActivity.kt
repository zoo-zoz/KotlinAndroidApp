package com.example.testapp

import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PathAnimationActivity : AppCompatActivity() {
    
    private lateinit var pathAnimationView: PathAnimationView
    private lateinit var tvHint: TextView
    private lateinit var btnAddPoint: Button
    private lateinit var btnClearPath: Button
    private lateinit var btnAddObject: Button
    private lateinit var btnClearObjects: Button
    private lateinit var btnMoveToPoint: Button
    private lateinit var btnMoveToCoord: Button
    private lateinit var btnMoveAlongPath: Button
    private lateinit var btnStopAnimation: Button
    private lateinit var btnReset: Button
    private lateinit var btnToggleMap: Button
    
    private lateinit var etObjectId: EditText
    private lateinit var etPointIndex: EditText
    private lateinit var etObjectId2: EditText
    private lateinit var etCoordX: EditText
    private lateinit var etCoordY: EditText
    private lateinit var etObjectId3: EditText
    
    private var isAddingPoints = false
    private var objectIdCounter = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_animation)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "路径动画控制"
        
        initViews()
        setupListeners()
        initializeMap()
    }
    
    private fun initializeMap() {
        // 等待视图测量完成后初始化地图
        pathAnimationView.post {
            val width = pathAnimationView.width.toFloat()
            val height = pathAnimationView.height.toFloat()
            
            if (width > 0 && height > 0) {
                createSampleMap(width, height)
            }
        }
    }
    
    private fun createSampleMap(width: Float, height: Float) {
        val buildings = mutableListOf<PathAnimationView.MapBuilding>()
        val roads = mutableListOf<PathAnimationView.MapRoad>()
        val areas = mutableListOf<PathAnimationView.MapArea>()
        
        // 创建区域
        // 左上区域 - 公园
        areas.add(PathAnimationView.MapArea(
            RectF(50f, 50f, width * 0.35f, height * 0.4f),
            "中央公园",
            Color.parseColor("#A5D6A7")
        ))
        
        // 右上区域 - 商业区
        areas.add(PathAnimationView.MapArea(
            RectF(width * 0.6f, 50f, width - 50f, height * 0.35f),
            "商业区",
            Color.parseColor("#FFE082")
        ))
        
        // 左下区域 - 住宅区
        areas.add(PathAnimationView.MapArea(
            RectF(50f, height * 0.65f, width * 0.4f, height - 50f),
            "住宅区",
            Color.parseColor("#BBDEFB")
        ))
        
        // 右下区域 - 工业区
        areas.add(PathAnimationView.MapArea(
            RectF(width * 0.65f, height * 0.6f, width - 50f, height - 50f),
            "工业区",
            Color.parseColor("#FFCCBC")
        ))
        
        // 创建道路
        // 横向主干道
        roads.add(PathAnimationView.MapRoad(
            RectF(0f, height * 0.45f, width, height * 0.55f),
            Color.parseColor("#BDBDBD"),
            true
        ))
        
        // 纵向主干道
        roads.add(PathAnimationView.MapRoad(
            RectF(width * 0.45f, 0f, width * 0.55f, height),
            Color.parseColor("#BDBDBD"),
            false
        ))
        
        // 创建建筑物
        // 左上区域建筑
        buildings.add(PathAnimationView.MapBuilding(
            RectF(80f, 80f, 180f, 150f),
            "图书馆",
            Color.parseColor("#5C6BC0")
        ))
        
        buildings.add(PathAnimationView.MapBuilding(
            RectF(220f, 100f, 320f, 180f),
            "博物馆",
            Color.parseColor("#7E57C2")
        ))
        
        // 右上区域建筑
        buildings.add(PathAnimationView.MapBuilding(
            RectF(width * 0.65f, 80f, width * 0.85f, 180f),
            "购物中心",
            Color.parseColor("#FF7043")
        ))
        
        buildings.add(PathAnimationView.MapBuilding(
            RectF(width * 0.65f, 220f, width * 0.75f, 300f),
            "酒店",
            Color.parseColor("#FFA726")
        ))
        
        // 左下区域建筑
        buildings.add(PathAnimationView.MapBuilding(
            RectF(100f, height * 0.7f, 180f, height * 0.85f),
            "小区A",
            Color.parseColor("#42A5F5")
        ))
        
        buildings.add(PathAnimationView.MapBuilding(
            RectF(220f, height * 0.72f, 300f, height * 0.87f),
            "小区B",
            Color.parseColor("#29B6F6")
        ))
        
        // 右下区域建筑
        buildings.add(PathAnimationView.MapBuilding(
            RectF(width * 0.7f, height * 0.65f, width * 0.85f, height * 0.8f),
            "工厂A",
            Color.parseColor("#8D6E63")
        ))
        
        buildings.add(PathAnimationView.MapBuilding(
            RectF(width * 0.7f, height * 0.85f, width * 0.85f, height - 80f),
            "仓库",
            Color.parseColor("#A1887F")
        ))
        
        // 中心区域建筑
        buildings.add(PathAnimationView.MapBuilding(
            RectF(width * 0.42f, height * 0.42f, width * 0.58f, height * 0.58f),
            "市政厅",
            Color.parseColor("#EF5350")
        ))
        
        pathAnimationView.setMapData(buildings, roads, areas)
    }
    
    private fun initViews() {
        pathAnimationView = findViewById(R.id.pathAnimationView)
        tvHint = findViewById(R.id.tvHint)
        btnAddPoint = findViewById(R.id.btnAddPoint)
        btnClearPath = findViewById(R.id.btnClearPath)
        btnAddObject = findViewById(R.id.btnAddObject)
        btnClearObjects = findViewById(R.id.btnClearObjects)
        btnMoveToPoint = findViewById(R.id.btnMoveToPoint)
        btnMoveToCoord = findViewById(R.id.btnMoveToCoord)
        btnMoveAlongPath = findViewById(R.id.btnMoveAlongPath)
        btnStopAnimation = findViewById(R.id.btnStopAnimation)
        btnReset = findViewById(R.id.btnReset)
        btnToggleMap = findViewById(R.id.btnToggleMap)
        
        etObjectId = findViewById(R.id.etObjectId)
        etPointIndex = findViewById(R.id.etPointIndex)
        etObjectId2 = findViewById(R.id.etObjectId2)
        etCoordX = findViewById(R.id.etCoordX)
        etCoordY = findViewById(R.id.etCoordY)
        etObjectId3 = findViewById(R.id.etObjectId3)
    }
    
    private fun setupListeners() {
        // 添加路径点模式
        btnAddPoint.setOnClickListener {
            isAddingPoints = !isAddingPoints
            if (isAddingPoints) {
                btnAddPoint.text = "停止添加"
                tvHint.text = "点击画布添加路径点"
                tvHint.visibility = android.view.View.VISIBLE
                Toast.makeText(this, "点击画布添加路径点", Toast.LENGTH_SHORT).show()
            } else {
                btnAddPoint.text = "添加路径点"
                tvHint.visibility = android.view.View.GONE
            }
        }
        
        // 清除路径
        btnClearPath.setOnClickListener {
            pathAnimationView.clearPathPoints()
            Toast.makeText(this, "路径已清除", Toast.LENGTH_SHORT).show()
        }
        
        // 添加对象
        btnAddObject.setOnClickListener {
            // 在画布中心添加对象
            val centerX = pathAnimationView.width / 2f
            val centerY = pathAnimationView.height / 2f
            pathAnimationView.addMovingObject(objectIdCounter, centerX, centerY)
            Toast.makeText(this, "已添加对象 #$objectIdCounter", Toast.LENGTH_SHORT).show()
            objectIdCounter++
        }
        
        // 清除对象
        btnClearObjects.setOnClickListener {
            pathAnimationView.clearMovingObjects()
            objectIdCounter = 1
            Toast.makeText(this, "对象已清除", Toast.LENGTH_SHORT).show()
        }
        
        // 移动到路径点
        btnMoveToPoint.setOnClickListener {
            val objectId = etObjectId.text.toString().toIntOrNull()
            val pointIndex = etPointIndex.text.toString().toIntOrNull()
            
            if (objectId == null) {
                Toast.makeText(this, "请输入有效的对象ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (pointIndex == null) {
                Toast.makeText(this, "请输入有效的路径点索引", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (pointIndex >= pathAnimationView.getPathPointCount()) {
                Toast.makeText(this, "路径点索引超出范围 (0-${pathAnimationView.getPathPointCount() - 1})", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            pathAnimationView.moveObjectToPoint(objectId, pointIndex, 2000)
            Toast.makeText(this, "对象 #$objectId 移动到路径点 P$pointIndex", Toast.LENGTH_SHORT).show()
        }
        
        // 移动到坐标
        btnMoveToCoord.setOnClickListener {
            val objectId = etObjectId2.text.toString().toIntOrNull()
            val x = etCoordX.text.toString().toFloatOrNull()
            val y = etCoordY.text.toString().toFloatOrNull()
            
            if (objectId == null) {
                Toast.makeText(this, "请输入有效的对象ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (x == null || y == null) {
                Toast.makeText(this, "请输入有效的坐标", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            pathAnimationView.moveObjectToCoordinate(objectId, x, y, 2000)
            Toast.makeText(this, "对象 #$objectId 移动到 ($x, $y)", Toast.LENGTH_SHORT).show()
        }
        
        // 沿路径移动
        btnMoveAlongPath.setOnClickListener {
            val objectId = etObjectId3.text.toString().toIntOrNull()
            
            if (objectId == null) {
                Toast.makeText(this, "请输入有效的对象ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (pathAnimationView.getPathPointCount() < 2) {
                Toast.makeText(this, "请先添加至少2个路径点", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            pathAnimationView.moveObjectAlongPath(objectId, 5000)
            Toast.makeText(this, "对象 #$objectId 沿路径移动", Toast.LENGTH_SHORT).show()
        }
        
        // 停止动画
        btnStopAnimation.setOnClickListener {
            pathAnimationView.stopAnimation()
            Toast.makeText(this, "动画已停止", Toast.LENGTH_SHORT).show()
        }
        
        // 切换地图显示
        btnToggleMap.setOnClickListener {
            pathAnimationView.toggleMapVisibility()
            btnToggleMap.text = if (btnToggleMap.text == "隐藏地图") "显示地图" else "隐藏地图"
        }
        
        // 重置
        btnReset.setOnClickListener {
            pathAnimationView.reset()
            objectIdCounter = 1
            isAddingPoints = false
            btnAddPoint.text = "添加路径点"
            tvHint.visibility = android.view.View.VISIBLE
            tvHint.text = "点击画布添加路径点"
            Toast.makeText(this, "已重置", Toast.LENGTH_SHORT).show()
        }
        
        // 画布点击事件
        pathAnimationView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isAddingPoints) {
                pathAnimationView.addPathPoint(event.x, event.y)
                val pointCount = pathAnimationView.getPathPointCount()
                Toast.makeText(this, "已添加路径点 P${pointCount - 1}", Toast.LENGTH_SHORT).show()
                
                if (pointCount >= 2) {
                    tvHint.visibility = android.view.View.GONE
                }
                true
            } else {
                false
            }
        }
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
        pathAnimationView.stopAnimation()
    }
}
