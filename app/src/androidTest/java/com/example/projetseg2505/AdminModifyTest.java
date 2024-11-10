package com.example.projetseg2505;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminModifyTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        // Initial setup if needed
    }

    @Test
    public void enterCredentials() {
        try {
            Espresso.onView(withId(R.id.emailEditText))
                    .perform(typeText("admin@gmail.com"), ViewActions.closeSoftKeyboard());

            Espresso.onView(withId(R.id.passwordEditText))
                    .perform(typeText("admin"), ViewActions.closeSoftKeyboard());

            Espresso.onView(withId(R.id.loginButton)).perform(click());
            Espresso.onView(withId(R.id.emailEditText)).perform(click());
            Espresso.onView(withId(R.id.emailEditText)).perform(typeText("b"));
            Espresso.onView(withId(R.id.senToEditDeleteRequesterButton)).perform(click());

            Espresso.onView(withId(R.id.firstNameEditText)).perform(click());
            Espresso.onView(withId(R.id.emailEditText)).perform(typeText("Bennett"));
            Espresso.onView(withId(R.id.lastNameEditText)).perform(click());
            Espresso.onView(withId(R.id.emailEditText)).perform(typeText("Williams"));
            Thread.sleep(500);
            Espresso.onView(withId(R.id.returnButton)).perform(click());
            Thread.sleep(500);
            Espresso.onView(withId(R.id.logoutButton)).perform(click());






}
