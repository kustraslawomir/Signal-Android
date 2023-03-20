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

  protected var pigeonTitleView: ConversationTitleView? = null
  private val mList: MutableList<String> = mutableListOf()
  private val mListGp: MutableList<String> = mutableListOf()
  private var sentToMe: String? = null
  private var isAddContacts = true

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

  private fun isPushGroupConversation(): Boolean {
    return getRecipient().isPushGroup
  }

  private fun initializeViews() {
    pigeonTitleView = findViewById(R.id.conversation_title_view)
    pigeonTitleView?.visibility = View.GONE
  }

  private fun isContact(recipientNumber: String) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
    } else {
      var cursor: Cursor? = null
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null)
        }
        if (cursor != null) {
          while (cursor.moveToNext()) {
            val contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            if (contactNumber == recipientNumber) {
              isAddContacts = false
              break
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        cursor?.close()
      }
    }
  }


  fun initMenuListGp() {
    mListGp.clear()
//    val enabled = !(isPushGroupConversation() && !isActiveGroup())
    val enabled = !(isPushGroupConversation())
    val Send_text = getString(R.string.conversation_activity__send) + " " + sentToMe
    if (enabled) {
      mListGp.add(Send_text)
      mListGp.add(getString(R.string.conversation__menu_voice_message))
      mListGp.add(getString(R.string.MessageRecord_group_call))
      mListGp.add(getString(R.string.conversation__menu_conversation_settings))
      mListGp.add(getString(R.string.conversation__menu_edit_group))
      mListGp.add(getString(R.string.conversation__menu_leave_group))
    } else {
      mListGp.add(getString(R.string.conversation__menu_conversation_settings))
      mListGp.add(getString(R.string.ExpirationDialog_disappearing_messages))
    }
  }

  private fun initMenuList() {
    mList.clear()
    val Send_text = getString(R.string.conversation_activity__send) + " " + sentToMe
    mList.add(Send_text)
    mList.add(getString(R.string.conversation__menu_voice_message))
    if (sentToMe != getString(R.string.note_to_self)) {
      mList.add(getString(R.string.conversation_callable_insecure__menu_call))
    }
    mList.add(getString(R.string.conversation__menu_conversation_settings))
    mList.add(getString(R.string.conversation_secure_verified__menu_reset_secure_session))
    if (isAddContacts) {
      mList.add(getString(R.string.conversation_add_to_contacts__menu_add_to_contacts))
    }
  }

  class TwoLineMenuAdapter : RecyclerView.Adapter<TwoLineMenuAdapter.ViewHolder> {
    private var mMenuList: MutableList<String>
    var mFocusHeight = 0
    var mNormalHeight = 0
    var mNormalPaddingX = 0
    var mFocusPaddingX = 0
    var mFocusTextSize = 0
    var mNormalTextSize = 0
    private val animDownAndGone: Animation? = null
    private val animDownAndVisible: Animation? = null
    private val animUpAndGone: Animation? = null
    private val animUpAndVisible: Animation? = null
    var rl: RelativeLayout? = null
    var mfts = 0
    var mfh = 0
    var mt = 0

    interface OnItemClickListener {
      fun onClick(position: Int)
    }

    var mListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
      mListener = listener
    }

    constructor(menulist: List<String>) {
      mMenuList = menulist.toMutableList()
    }

    constructor(relativeLayout: RelativeLayout?, marginTop: Int, menulist: List<String>) {
      mMenuList = menulist.toMutableList()
      mt = marginTop
      rl = relativeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view: View = LayoutInflater.from(parent.context).inflate(R.layout.pigeon_custom_menu_item, parent, false)
      val res = parent.context.resources
      //Resources res = getResources();
      mFocusHeight = res.getDimensionPixelSize(R.dimen.focus_item_height)
      mNormalHeight = res.getDimensionPixelSize(R.dimen.item_height)
      mFocusTextSize = res.getDimensionPixelSize(R.dimen.focus_item_textsize)
      mNormalTextSize = res.getDimensionPixelSize(R.dimen.item_textsize)
      mFocusPaddingX = res.getDimensionPixelSize(R.dimen.focus_item_padding_x)
      mNormalPaddingX = res.getDimensionPixelSize(R.dimen.item_padding_x)
      view.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
        startFocusAnimationSingleLine(v, hasFocus)
        if (hasFocus) {
          (view as TextView).setTextColor(Color.WHITE)
        } else {
          (view as TextView).setTextColor(Color.GRAY)
        }
      }
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = mMenuList[position]
      holder.itemView.setOnClickListener { mListener!!.onClick(position) }
      holder.menutext.text = item
      holder.menutext.tag = position
    }

    private fun startFocusAnimationSingleLine(v: View, focused: Boolean) {
      val va: ValueAnimator
      val item = v as TextView
      va = if (focused) {
        ValueAnimator.ofFloat(0f, 1f)
      } else {
        ValueAnimator.ofFloat(1f, 0f)
      }
      va.addUpdateListener { valueAnimator: ValueAnimator ->
        val scale = valueAnimator.animatedValue as Float
        val height = (mFocusHeight - mNormalHeight).toFloat() * scale + mNormalHeight.toFloat()
        val textsize = (mFocusTextSize - mNormalTextSize).toFloat() * scale + mNormalTextSize.toFloat()
        val padding = mNormalPaddingX.toFloat() - (mNormalPaddingX - mFocusPaddingX).toFloat() * scale
        val alpha = (0x81f + (0xff - 0x81).toFloat() * scale).toInt()
        val color = alpha * 0x1000000 + 0xffffff
        item.textSize = textsize.toInt().toFloat()
        item.setTextColor(color)
        item.setPadding(
          padding.toInt(), item.paddingTop,
          item.paddingRight, item.paddingBottom
        )
        item.layoutParams.height = height.toInt()
      }
      val FastOutLinearInInterpolator = FastOutLinearInInterpolator()
      va.interpolator = FastOutLinearInInterpolator
      if (focused) {
        va.duration = 270
        va.start()
      } else {
        va.duration = 270
        va.start()
      }
      item.ellipsize = TextUtils.TruncateAt.MARQUEE
    }

    fun removeData(position: Int) {
      mMenuList.removeAt(position)
      notifyItemRemoved(position)
      notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
      return mMenuList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      var menutext: TextView

      init {
        menutext = itemView.findViewById<View>(R.id.menu_item) as TextView
      }
    }
  }

}
