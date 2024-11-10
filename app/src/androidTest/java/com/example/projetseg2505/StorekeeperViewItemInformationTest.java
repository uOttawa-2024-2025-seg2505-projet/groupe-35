package com.example.projetseg2505;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StorekeeperViewItemInformationTest {

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
                    .perform(typeText("storekeeper@gmail.com"), ViewActions.closeSoftKeyboard());

            Espresso.onView(withId(R.id.passwordEditText))
                    .perform(typeText("storekeeper"), ViewActions.closeSoftKeyboard());

            Espresso.onView(withId(R.id.loginButton)).perform(click());

            // Navigate to input for modifying/removing item
            Espresso.onView(withId(R.id.modifyRemoveDescriptionItemInput)).perform(click());
            Espresso.onView(withId(R.id.modifyRemoveDescriptionItemInput)).perform(typeText("Development Tool X"));
            Thread.sleep(500);
            // View item information and return
            Espresso.onView(withId(R.id.viewItemInformationsButton)).perform(click());
            Thread.sleep(500);
            Espresso.onView(withId(R.id.returnButton)).perform(click());

            Espresso.onView(withId(R.id.modifyRemoveDescriptionItemInput)).perform(typeText("Development Tool X"));

            Espresso.onView(withId(R.id.sendToModifyRemoveItemLayoutButton)).perform(click());
            Espresso.onView(withId(R.id.incrementButton)).perform(click());
            Thread.sleep(200);
            Espresso.onView(withId(R.id.incrementButton)).perform(click());
            Thread.sleep(200);
            Espresso.onView(withId(R.id.incrementButton)).perform(click());
            Thread.sleep(200);
            Espresso.onView(withId(R.id.returnButton)).perform(click());


            Thread.sleep(200);
            // Log out
            Espresso.onView(withId(R.id.logoutButton)).perform(click());]

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
