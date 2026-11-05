package app.calendarmanager

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.calendarmanager.ui.navigation.TopLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun allFiveDestinationsAreReachable() {
        assertEquals5()
        TopLevel.entries.forEach { dest ->
            rule.onNode(hasText(rule.activity.getString(dest.label)) and hasClickAction()).performClick()
            rule.onNodeWithTag(dest.testTag).assertIsDisplayed()
        }
    }

    @Test fun settingsOpensAndBackReturns() {
        rule.onNodeWithContentDescription(rule.activity.getString(R.string.settings)).performClick()
        rule.onNodeWithTag("screen_settings").assertIsDisplayed()
        rule.onNodeWithContentDescription(rule.activity.getString(R.string.back)).performClick()
        rule.onNodeWithTag(TopLevel.DASHBOARD.testTag).assertIsDisplayed()
    }

    private fun assertEquals5() = org.junit.Assert.assertEquals(5, TopLevel.entries.size)
}
