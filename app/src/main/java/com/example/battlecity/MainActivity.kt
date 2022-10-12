package com.example.battlecity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import com.example.battlecity.constance.Direction
import com.example.battlecity.databinding.ActivityMainBinding
import com.example.battlecity.drawers.*
import com.example.battlecity.enum.Coordinate
import com.example.battlecity.enum.Element
import com.example.battlecity.enum.Material
import com.example.battlecity.models.Tank

const val PIPS = 30
const val VERTICAL_CELL_AMOUNT = 24
const val HORIZONTAL_CELL_AMOUNT = 39
const val VERTICAL_MAX_SIZE = PIPS * VERTICAL_CELL_AMOUNT
const val HORIZONTAL_MAX_SIZE = PIPS * HORIZONTAL_CELL_AMOUNT


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var editMode = false
    private val playerTank = Tank(
        Element(
            R.id.image_tank,
            Material.PLAYER_TANK,
            Coordinate(0, 0),
            Material.PLAYER_TANK.width,
            Material.PLAYER_TANK.height
        ), Direction.UP
    )

    private val gridDrawer by lazy {
        GridDrawer(binding.containerFrame)
    }
    private val elementsDrawer by lazy {
        ElementsDrawer(binding.containerFrame)
    }
    private val bulletDrawer by lazy {
        BulletDrawer(binding.containerFrame)
    }
    private val levelStorage by lazy {
        LevelStorage(this)
    }
    private val enemyDrawer by lazy {
        EnemyDrawer(binding.containerFrame,elementsDrawer.elementsOnContainer)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnUp.setOnClickListener {move(Direction.UP)}
        binding.btnDown.setOnClickListener {move(Direction.DOWN)}
        binding.btnLeft.setOnClickListener {move(Direction.LEFT)}
        binding.btnRight.setOnClickListener {move(Direction.RIGHT)}
        binding.floatingActionButton.setOnClickListener {
            bulletDrawer.makeBulletMove(
                binding.imageTank,
                playerTank.direction,
                elementsDrawer.elementsOnContainer
              )
        }


        binding.containerFrame.layoutParams = FrameLayout.LayoutParams(
            VERTICAL_MAX_SIZE,
            HORIZONTAL_MAX_SIZE

        )
        binding.imageDelete.setOnClickListener {
            elementsDrawer.currentMaterial = Material.EMPTY
        }
        binding.imageBrick.setOnClickListener {
            elementsDrawer.currentMaterial = Material.BRICK
        }
        binding.imageGrass.setOnClickListener {
            elementsDrawer.currentMaterial = Material.GRASS
        }
        binding.imageConcrete.setOnClickListener {
            elementsDrawer.currentMaterial = Material.CONCRETE
        }
        binding.imageEagle.setOnClickListener {
            elementsDrawer.currentMaterial = Material.EAGLE
        }

        binding.containerFrame.setOnTouchListener { _, event ->
            elementsDrawer.onTouchContainer(event.x, event.y)
            return@setOnTouchListener true
        }
        elementsDrawer.drawElementsList(levelStorage.loadLevel())
        hideSettings()
        elementsDrawer.elementsOnContainer.add(playerTank.element)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                switchEditMode()
                 true
            }
            R.id.menu_save -> {
                levelStorage.saveLevel(elementsDrawer.elementsOnContainer)
                 true
            }
            R.id.menu_play -> {
                startTheGame()
                 true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun startTheGame() {
        if (editMode) {
            return
        }
        enemyDrawer.startEnemyCreation()
        enemyDrawer.moveEnemyTanks()
    }
    private fun switchEditMode() {
        editMode = !editMode
        if (editMode) {
            showSettings()
        } else {
            hideSettings()
        }
    }

    private fun showSettings() {
        gridDrawer.drawGrid()
        binding.linMaterialContainer.visibility = VISIBLE
    }

    private fun hideSettings() {
        gridDrawer.removeGrid()
        binding.linMaterialContainer.visibility = GONE
    }
    private fun move(direction: Direction) {
        playerTank.move(direction, binding.containerFrame, elementsDrawer.elementsOnContainer)
    }

}