package org.thoughtcrime.securesms.conversation

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.HidingLinearLayout
import org.thoughtcrime.securesms.components.reminder.ReminderView
import org.thoughtcrime.securesms.components.settings.app.subscription.DonationPaymentComponent
import org.thoughtcrime.securesms.components.settings.app.subscription.StripeRepository
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.Debouncer
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme
import org.thoughtcrime.securesms.util.DynamicTheme
import org.thoughtcrime.securesms.util.views.Stub
import pigeon.extensions.isSignalVersion
import pigeon.navigation.KeyEventBehaviour
import pigeon.navigation.PigeonKeyEventBehaviourImpl
import java.util.concurrent.TimeUnit

open class ConversationActivity : PassphraseRequiredActivity(), ConversationParentFragment.Callback, DonationPaymentComponent {

  companion object {
    private const val STATE_WATERMARK = "share_data_watermark"
  }

  private val transitionDebouncer: Debouncer = Debouncer(150, TimeUnit.MILLISECONDS)
  private lateinit var fragment: ConversationParentFragment
  private var shareDataTimestamp: Long = -1L
  private val keyEventBehaviour: KeyEventBehaviour = PigeonKeyEventBehaviourImpl()

  private val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
  override fun onPreCreate() {
    dynamicTheme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    supportPostponeEnterTransition()
    transitionDebouncer.publish { supportStartPostponedEnterTransition() }
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

    if (!isSignalVersion()) {
      window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    if (savedInstanceState != null) {
      shareDataTimestamp = savedInstanceState.getLong(STATE_WATERMARK, -1L)
    } else if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) {
      shareDataTimestamp = System.currentTimeMillis()
    }
    setContentView(R.layout.conversation_parent_fragment_container)

    if (savedInstanceState == null) {
      replaceFragment(intent!!)
    } else {
      fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as ConversationParentFragment
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    transitionDebouncer.clear()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putLong(STATE_WATERMARK, shareDataTimestamp)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    setIntent(intent)
    replaceFragment(intent!!)
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayResultPublisher.onNext(DonationPaymentComponent.GooglePayResult(requestCode, resultCode, data))
  }

  private fun replaceFragment(intent: Intent) {
    fragment = ConversationParentFragment.create(intent)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .disallowAddToBackStack()
      .commitNowAllowingStateLoss()
  }

  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    return fragment.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
  }

  override fun onResume() {
    super.onResume()
    dynamicTheme.onResume(this)
  }

  override fun getShareDataTimestamp(): Long {
    return shareDataTimestamp
  }

  override fun setShareDataTimestamp(timestamp: Long) {
    shareDataTimestamp = timestamp
  }

  override fun onInitializeToolbar(toolbar: Toolbar) {
    toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.ic_arrow_left_24)
    toolbar.setNavigationOnClickListener { finish() }
  }

  fun getRecipient(): Recipient {
    return fragment.recipient
  }

  fun getTitleView(): View {
    return fragment.titleView
  }

  fun getComposeText(): View {
    return fragment.composeText
  }

  fun getQuickAttachmentToggle(): HidingLinearLayout {
    return fragment.quickAttachmentToggle
  }

  fun getReminderView(): Stub<ReminderView> {
    return fragment.reminderView
  }

  override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
    if (isSignalVersion()) {
      return super.dispatchKeyEvent(event)
    }
    keyEventBehaviour.dispatchConversationKeyEvent(event!!, supportFragmentManager)
    return super.dispatchKeyEvent(event)
  }

  override val stripeRepository: StripeRepository by lazy { StripeRepository(this) }
  override val googlePayResultPublisher: Subject<DonationPaymentComponent.GooglePayResult> = PublishSubject.create()

}
