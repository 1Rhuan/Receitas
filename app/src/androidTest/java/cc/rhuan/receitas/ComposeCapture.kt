package cc.rhuan.receitas

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/** Retry only transient image acquisition; interaction assertions are never retried. */
internal fun ComposeContentTestRule.captureActiveRoot(): Bitmap {
    repeat(3) { attempt ->
        waitForIdle()
        try {
            val popup = onAllNodes(isPopup())
            val dialog = onAllNodes(isDialog())
            if (popup.fetchSemanticsNodes().isNotEmpty() || dialog.fetchSemanticsNodes().isNotEmpty()) {
                val display = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                if (display != null) return display
            }
            val active = when {
                popup.fetchSemanticsNodes().isNotEmpty() -> popup.onLast()
                dialog.fetchSemanticsNodes().isNotEmpty() -> dialog.onLast()
                else -> onRoot()
            }
            return active.captureToImage().asAndroidBitmap()
        } catch (error: AssertionError) {
            if (error.message?.contains("PixelCopy") != true || attempt == 2) throw error
            Log.w("ComposeCapture", "Retrying PixelCopy (${attempt + 1}/3)", error)
        } catch (error: ComposeTimeoutException) {
            if (attempt == 2) throw error
            Log.w("ComposeCapture", "Retrying root capture (${attempt + 1}/3)", error)
        } catch (error: RuntimeException) {
            if (error.javaClass.simpleName != "PixelCopyException") throw error
            val display = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            if (display != null) return display
            if (attempt == 2) throw error
            Log.w("ComposeCapture", "Retrying image acquisition (${attempt + 1}/3)", error)
        }
    }
    error("Image acquisition did not complete")
}
