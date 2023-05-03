package kr.ac.duksung.rebit
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt

class SystemUiController(private val window: Window) {

    fun setStatusBarColor(@ColorInt color: Int, darkIcons: Boolean = false) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
        if (darkIcons) {
//            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
//                    WindowManager.LayoutParams.FLAG_DARK_STATUS_BAR
        }
    }

    fun setNavigationBarColor(@ColorInt color: Int, darkIcons: Boolean = false) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
        if (darkIcons) {
//            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
//                    WindowManager.LayoutParams.FLAG_DARK_NAVIGATION_BAR
        }
    }

    fun setSystemBarsColor(@ColorInt color: Int, darkIcons: Boolean = false) {
        setStatusBarColor(color, darkIcons)
        setNavigationBarColor(color, darkIcons)
    }
}
