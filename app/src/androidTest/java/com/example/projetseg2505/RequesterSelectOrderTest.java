package com.example.projetseg2505;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class RequesterSelectOrderTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void enterCredentials() {
        Espresso.onView(withId(R.id.emailEditText))
                .perform(typeText("a"), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.passwordEditText))
                .perform(typeText("a"), ViewActions.closeSoftKeyboard());
        Espresso.onView(withId(R.id.loginButton)).perform(click());

        Espresso.onView(withId(R.id.sendToPlaceOrderLayout)).perform(click());

        Espresso.onView(withId(R.id.computerCase)).perform(click());
        onData(anything())
                .atPosition(3)
                .perform(click());

        Espresso.onView(withId(R.id.motherboard)).perform(click());
        onData(anything())
                .atPosition(2)
                .perform(click());

        Espresso.onView(withId(R.id.memoryStick)).perform(click());
        onData(anything())
                .atPosition(2)
                .perform(click());

        Espresso.onView(withId(R.id.inputMemoryAmount)).perform(click());
        Espresso.onView(withId(R.id.inputMemoryAmount)).perform(typeText("1"));

        Espresso.onView(withId(R.id.hardDrive)).perform(click());
        onData(anything())
                .atPosition(2)
                .perform(click());

        Espresso.onView(withId(R.id.monitors)).perform(click());
        onData(anything())
                .atPosition(2)
                .perform(click());

        Espresso.onView(withId(R.id.inputMonitorAmount)).perform(click());
        Espresso.onView(withId(R.id.inputMonitorAmount)).perform(typeText("1"));

        Espresso.onView(withId(R.id.keyboardMouse)).perform(click());
        onData(anything())
                .atPosition(2)
                .perform(click());

        Espresso.onView(withId(R.id.webBrowser)).perform(click());
        onData(anything())
                .atPosition(2)
                .perform(click());

        Espresso.onView(withId(R.id.developmentTools)).perform(click());
        onData(anything())
                .atPosition(1)
                .perform(click());


        Espresso.onView(withId(R.id.createOrderButton)).perform(click());

        Espresso.onView(withId(R.id.returnButtonCreateOrder)).perform(click());


        Espresso.onView(withId(R.id.logoutButton)).perform(click());





    }
}
