package com.example.battlecity.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.battlecity.*
import com.example.battlecity.constance.Direction
import com.example.battlecity.enum.Coordinate
import com.example.battlecity.enum.Element

private const val BULLET_WIDTH = 7
private const val BULLET_HEIGHT = 12

class BulletDrawer(private val container: FrameLayout) {
    private var canBulletGoFurther = true
    private var bulletThread: Thread? = null

    private fun checkBulletThreadAlive() = bulletThread != null && bulletThread!!.isAlive


    fun makeBulletMove(myTank: View, currentDirection: Direction, elementsOnContainer: MutableList<Element>) {
        canBulletGoFurther = true
        if (!checkBulletThreadAlive()) {
            bulletThread = Thread(Runnable {
                val bullet = createBullet(myTank, currentDirection)
                while (bullet.checkViewCanMoveThroughBorder(Coordinate(bullet.top, bullet.left)) && canBulletGoFurther) {
                    when (currentDirection) {
                        Direction.UP -> (bullet.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                        Direction.DOWN -> (bullet.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                        Direction.LEFT -> (bullet.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                        Direction.RIGHT -> (bullet.layoutParams as FrameLayout.LayoutParams).leftMargin += BULLET_HEIGHT
                    }
                    Thread.sleep(30)
                    chooseBehaviorInTermsOfDirections(
                        elementsOnContainer,
                        currentDirection,
                        Coordinate(
                            (bullet.layoutParams as FrameLayout.LayoutParams).topMargin,
                            (bullet.layoutParams as FrameLayout.LayoutParams).leftMargin))
                    container.runOnUiThread {
                        container.removeView(bullet)
                        container.addView(bullet)
                    }
                }
                container.runOnUiThread {
                    container.removeView(bullet)
                }
            })
            bulletThread!!.start()
        }
    }

    private fun chooseBehaviorInTermsOfDirections(
        elementsOnContainer: MutableList<Element>,
        currentDirection: Direction,
        bulletCoordinate: Coordinate
    ) {
        when (currentDirection) {
            Direction.DOWN, Direction.UP -> {
                compareCollections(
                    elementsOnContainer,
                    getCoordinatesForTopOrBottomDirection(bulletCoordinate)
                )
            }
            Direction.LEFT, Direction.RIGHT -> {
                compareCollections(
                    elementsOnContainer,
                    getCoordinatesForLeftOrRightDirection(bulletCoordinate)
                )
            }
        }
    }

    private fun compareCollections(
        elementsOnContainer: MutableList<Element>,
        detectedCoordinatesList: List<Coordinate>
    ) {
        detectedCoordinatesList.forEach {
            val element = getElementByCoordinates(it, elementsOnContainer)
            removeElementsAndStopBullet(element, elementsOnContainer)
        }
    }

    private fun removeElementsAndStopBullet(element: Element?, elementsOnContainer: MutableList<Element>) {
        if (element != null) {
            if (element.material.bulletCanGoThrough) {
                return
            }
            if (element.material.simpleBulletCanDestroy) {
                stopBullet()
                removeView(element)
                elementsOnContainer.remove(element)
            } else {
                stopBullet()
            }
        }
    }
    private fun stopBullet() {
        canBulletGoFurther = false
    }

    private fun removeView(element: Element) {
        val activity = container.context as Activity
        activity.runOnUiThread {
            container.removeView(activity.findViewById(element.viewId))
        }
    }

    private fun getCoordinatesForTopOrBottomDirection(bulletCoordinate: Coordinate): List<Coordinate> {
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % PIPS
        val rightCell = leftCell + PIPS
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % PIPS
        return listOf(
            Coordinate(topCoordinate, leftCell),
            Coordinate(topCoordinate, rightCell)
        )
    }

    private fun getCoordinatesForLeftOrRightDirection(bulletCoordinate: Coordinate): List<Coordinate> {
        val topCell = bulletCoordinate.top - bulletCoordinate.top % PIPS
        val bottomCell = topCell + PIPS
        val leftCoordinate = bulletCoordinate.left - bulletCoordinate.left % PIPS
        return listOf(
            Coordinate(topCell, leftCoordinate),
            Coordinate(bottomCell, leftCoordinate)
        )
    }

    private fun createBullet(myTank: View, currentDirection: Direction): ImageView {
        return ImageView(container.context)
            .apply {
                this.setImageResource(R.drawable.bullet)
                this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)
                val bulletCoordinate = getBulletCoordinates(this, myTank, currentDirection)
                (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
                (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
                this.rotation = currentDirection.rotation
            }
    }

    private fun getBulletCoordinates(
        bullet: ImageView,
        myTank: View,
        currentDirection: Direction
    ): Coordinate {
        val tankLeftTopCoordinate = Coordinate(myTank.top, myTank.left)
        return when (currentDirection) {
            Direction.UP ->
                Coordinate(
                    top = tankLeftTopCoordinate.top - bullet.layoutParams.height,
                    left = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.left,
                        bullet.layoutParams.width
                    )
                )
            Direction.DOWN ->
                Coordinate(
                    top = tankLeftTopCoordinate.top + myTank.layoutParams.height,
                    left = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.left,
                        bullet.layoutParams.width
                    )
                )
            Direction.LEFT ->
                Coordinate(
                    top = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.top,
                        bullet.layoutParams.height
                    ),
                    left = tankLeftTopCoordinate.left - bullet.layoutParams.width
                )
            Direction.RIGHT ->
                Coordinate(
                    top = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.top,
                        bullet.layoutParams.height
                    ),
                    left = tankLeftTopCoordinate.left + myTank.layoutParams.width
                )
        }
    }

    private fun getDistanceToMiddleOfTank(startCoordinate: Int, bulletSize: Int): Int {
        return startCoordinate + (PIPS - bulletSize / 2)
    }
}