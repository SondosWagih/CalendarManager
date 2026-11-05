package app.calendarmanager

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun str(id: Int) = rule.activity.getString(id)

    @Test fun addCourseFromTheUiShowsItInTheList() {
        val name = "Course ${System.nanoTime()}"
        rule.onNode(hasText(str(R.string.nav_study)) and hasClickAction()).performClick()
        rule.onNodeWithText(str(R.string.tab_courses)).performClick()
        rule.onNodeWithContentDescription(str(R.string.add)).performClick()
        rule.onNodeWithText(str(R.string.field_name)).performTextInput(name)
        rule.onNodeWithText(str(R.string.save)).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText(str(R.string.save)).fetchSemanticsNodes().isEmpty() }
        rule.waitUntil(5_000) { rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(name).assertIsDisplayed()
    }
}
