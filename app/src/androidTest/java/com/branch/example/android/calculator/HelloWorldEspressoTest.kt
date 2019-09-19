package com.branch.example.android.calculator

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.branch.example.android.calculator.controllers.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HelloWorldEspressoTest {

    @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun runAllTheTests() {
        val display = onView(withId(R.id.computations_display))

        // base case, click a digit and see it displayed
        onView(withId(R.id.one)).perform(click())
        display.check(matches(withText("1")))

        // base delete case, delete single entry from an expression with single entry
        onView(withId(R.id.delete)).perform(click())
        display.check(matches(withText("")))

        // cannot start the expression with an operator unless it's "-"
        onView(withId(R.id.multiply)).perform(click())
        onView(withId(R.id.divide)).perform(click())
        onView(withId(R.id.plus)).perform(click())
        display.check(matches(withText("")))
        onView(withId(R.id.minus)).perform(click())
        display.check(matches(withText("-")))

        // cannot have multiple operators in a row
        onView(withId(R.id.multiply)).perform(click())
        onView(withId(R.id.divide)).perform(click())
        onView(withId(R.id.plus)).perform(click())
        onView(withId(R.id.plus)).perform(click())
        display.check(matches(withText("-")))

        // delete the last element only
        onView(withId(R.id.one)).perform(click())
        onView(withId(R.id.minus)).perform(click())
        onView(withId(R.id.five)).perform(click())
        onView(withId(R.id.delete)).perform(click())
        display.check(matches(withText("-1-")))

        // clicking on a number repeatedly makes a larger number
        onView(withId(R.id.five)).perform(click())
        onView(withId(R.id.five)).perform(click())
        display.check(matches(withText("-1-55")))

        // deletion works on the whole number, not it's last digit
        onView(withId(R.id.delete)).perform(click())
        display.check(matches(withText("-1-")))

        // division by 0 is not allowed
        onView(withId(R.id.five)).perform(click())
        onView(withId(R.id.divide)).perform(click())
        onView(withId(R.id.zero)).perform(click())
        display.check(matches(withText("-1-5/")))

        // result is correct
        onView(withId(R.id.one)).perform(click())
        onView(withId(R.id.zero)).perform(click())
        display.check(matches(withText("-1-5/10")))
        onView(withId(R.id.equal)).perform(click())
        display.check(matches(withText("-1.5")))
    }
}