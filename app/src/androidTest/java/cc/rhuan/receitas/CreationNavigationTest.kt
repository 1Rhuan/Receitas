package cc.rhuan.receitas

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class CreationNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun grantNotifications() {
        if (Build.VERSION.SDK_INT >= 33) InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission("cc.rhuan.receitas", Manifest.permission.POST_NOTIFICATIONS)
    }
    @Test fun prescriptionsPageOpensRecipeRegistration() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("floating-navigation").assertIsDisplayed()
        compose.onNodeWithContentDescription("Criar medicamento").assertDoesNotExist()
        compose.onNodeWithContentDescription("Criar receita").performClick()
        compose.onNodeWithText("Vamos cadastrar sua receita").assertIsDisplayed()
    }
    @Test fun medicationsPageOpensStandaloneRegistration() {
        compose.onNodeWithContentDescription("Medicamentos", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("floating-navigation").assertIsDisplayed()
        compose.onNodeWithContentDescription("Criar receita").assertDoesNotExist()
        compose.onNodeWithContentDescription("Criar medicamento").performClick()
        compose.onNodeWithText("Qual medicamento deseja acompanhar?").assertIsDisplayed()
    }
    @Test fun contextualActionMovesNavigationWithoutResizingItems() {
        fun bounds(label: String) = compose.onNodeWithContentDescription(label, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        compose.onNodeWithContentDescription("Início", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("navigation-create").assertDoesNotExist()
        val initialCapsule = compose.onNodeWithTag("navigation-capsule").fetchSemanticsNode().boundsInRoot
        val initial = listOf("Início", "Receitas", "Medicamentos").map(::bounds)
        for (destination in listOf("Receitas", "Medicamentos")) {
            compose.onNodeWithContentDescription(destination, useUnmergedTree = true).performClick()
            compose.waitForIdle()
            compose.onNodeWithTag("navigation-create").assertIsDisplayed()
            val capsule = compose.onNodeWithTag("navigation-capsule").fetchSemanticsNode().boundsInRoot
            assertEquals(initialCapsule.width, capsule.width, 0.5f)
            assertTrue(capsule.left < initialCapsule.left)
            val current = listOf("Início", "Receitas", "Medicamentos").map(::bounds)
            initial.zip(current).forEach { (before, after) ->
                assertEquals(before.width, after.width, 0.5f)
                assertEquals(before.height, after.height, 0.5f)
                assertEquals(before.left - initialCapsule.left, after.left - capsule.left, 0.5f)
            }
        }
    }
}
