package cc.rhuan.receitas

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreationNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun grantNotifications() {
        if (Build.VERSION.SDK_INT >= 33) InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission("cc.rhuan.receitas", Manifest.permission.POST_NOTIFICATIONS)
    }
    @Test fun prescriptionsPageOpensRecipeRegistration() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("floating-navigation").assertIsDisplayed()
        compose.onNodeWithContentDescription("Nova receita").performClick()
        compose.onNodeWithText("Vamos cadastrar sua receita").assertIsDisplayed()
    }
    @Test fun medicationsPageOpensStandaloneRegistration() {
        compose.onNodeWithContentDescription("Medicamentos", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("floating-navigation").assertIsDisplayed()
        compose.onNodeWithContentDescription("Novo medicamento").performClick()
        compose.onNodeWithText("Qual medicamento deseja acompanhar?").assertIsDisplayed()
    }
}
