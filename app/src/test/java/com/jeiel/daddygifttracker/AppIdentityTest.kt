package com.jeiel.daddygifttracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppIdentityTest {
  @Test
  fun appNameMatchesReleaseListing() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    assertEquals("경조사 인맥 관리", context.getString(R.string.app_name))
  }
}
