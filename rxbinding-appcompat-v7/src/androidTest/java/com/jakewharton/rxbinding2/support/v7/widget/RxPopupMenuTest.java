package com.jakewharton.rxbinding2.support.v7.widget;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import com.jakewharton.rxbinding2.RecordingObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class) public final class RxPopupMenuTest {
  @Rule public final ActivityTestRule<RxPopupMenuTestActivity> activityRule =
      new ActivityTestRule<>(RxPopupMenuTestActivity.class);

  private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

  private PopupMenu view;

  @Before public void setUp() {
    view = activityRule.getActivity().popupMenu;
  }

  @Test @UiThreadTest public void itemClicks() {
    Menu menu = view.getMenu();
    MenuItem item1 = menu.add(0, 1, 0, "Hi");
    MenuItem item2 = menu.add(0, 2, 0, "Hey");

    RecordingObserver<MenuItem> o = new RecordingObserver<>();
    RxPopupMenu.itemClicks(view).subscribe(o);
    o.assertNoMoreEvents();

    menu.performIdentifierAction(2, 0);
    assertThat(o.takeNext()).isSameAs(item2);

    menu.performIdentifierAction(1, 0);
    assertThat(o.takeNext()).isSameAs(item1);

    o.dispose();

    menu.performIdentifierAction(2, 0);
    o.assertNoMoreEvents();
  }

  @Test public void dismisses() {
    RecordingObserver<Object> o = new RecordingObserver<>();
    RxPopupMenu.dismisses(view)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.show();
      }
    });
    o.assertNoMoreEvents();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.dismiss();
      }
    });
    assertThat(o.takeNext()).isNotNull();

    o.dispose();
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.show();
      }
    });
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.dismiss();
      }
    });

    o.assertNoMoreEvents();
  }
}
