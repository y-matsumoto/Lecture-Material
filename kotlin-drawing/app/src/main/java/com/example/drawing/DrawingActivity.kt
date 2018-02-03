package com.example.drawing

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.Menu
import android.view.MenuItem


class DrawingActivity : ActionBarActivity(), ColorPickerDialog.OnColorChangedListener {
    private val COLOR_MENU_ID = 1
    private val CLEAR_MENU_ID = COLOR_MENU_ID + 1
    private val SETTING_MENU_ID = COLOR_MENU_ID + 2
    private var drawingView: DrawingView? = null
    private val boardWidth = 1000
    private val boardHeight = 2000

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val point = getRealSize(this)
        drawingView = DrawingView(
                this@DrawingActivity,
                point.x,
                point.y)
        setContentView(drawingView)
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, COLOR_MENU_ID, 0, "Color").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        menu.add(0, CLEAR_MENU_ID, 1, "Clear").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        menu.add(0, SETTING_MENU_ID, 2, "Setting")
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == COLOR_MENU_ID -> {
                ColorPickerDialog(this, this, -0x10000).show()
                true
            }
            item.itemId == CLEAR_MENU_ID -> {
                drawingView?.clear()
                true
            }
            item.itemId == SETTING_MENU_ID -> {
                // noop
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun colorChanged(newColor: Int) {
        drawingView?.setColor(newColor)
    }

    @SuppressLint("NewApi")
    fun getRealSize(activity: Activity): android.graphics.Point {
        val display = activity.windowManager.defaultDisplay
        val point = android.graphics.Point(0, 0)
        display.getRealSize(point)
        return point
    }

}
