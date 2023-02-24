package pigeon.adapters

import android.animation.ValueAnimator
import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.RecyclerView
import org.thoughtcrime.securesms.R
import pigeon.constants.RegistrationConstants

class EnterCodeAdapter constructor(private val context: Context, private val listener: OnItemClickListener, private val list: ArrayList<String>) : RecyclerView.Adapter<EnterCodeAdapter.ViewHolder>() {

  companion object{
    private const val mFocusTextSize = 0
    private const val mNormalTextSize = 0
    private const val mFocusPaddingX = 0
    private const val mNormalPaddingX = 0
    private var mMinFinished = -1
    private var mSecFinished = -1

    fun resetTimer() {
      mMinFinished = -1
      mSecFinished = -1
    }

    private fun initTimer() {
      mMinFinished = 1
      mSecFinished = 0
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view: View = LayoutInflater.from(context).inflate(R.layout.pigeon_singleline_item, parent, false)
    view.onFocusChangeListener = OnFocusChangeListener { view, b ->
      val tv = view.findViewById<View>(R.id.item_singleline_tv) as TextView
      if (b) {
        tv.isSelected = true
        tv.ellipsize = TextUtils.TruncateAt.MARQUEE
      } else {
        tv.ellipsize = TextUtils.TruncateAt.END
      }
      updateFocusView(view, b, tv)
    }
    return ViewHolder(view)
  }

  private fun updateFocusView(parent: View, hasFocus: Boolean, tv: TextView) {
    val animator: ValueAnimator = if (hasFocus) {
      ValueAnimator.ofFloat(0f, 1f)
    } else {
      ValueAnimator.ofFloat(1f, 0f)
    }
    animator.addUpdateListener { valueAnimator ->
      val scale = valueAnimator.animatedValue as Float
      val textSizes: Float = (mFocusTextSize - mNormalTextSize).toFloat() * scale + mNormalTextSize
      val padding: Float = mNormalPaddingX.toFloat() - (mNormalPaddingX - mFocusPaddingX).toFloat() * scale
      val alpha = (0x81f + (0xff - 0x81).toFloat() * scale).toInt()
      val color = alpha * 0x1000000 + 0xffffff
      tv.setTextColor(color)
      tv.textSize = textSizes
      parent.setPadding(padding.toInt(), parent.paddingTop, parent.paddingRight, parent.paddingBottom)
    }
    animator.interpolator = FastOutLinearInInterpolator()
    if (hasFocus) {
      animator.duration = 270
      animator.start()
    } else {
      animator.duration = 270
      animator.start()
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val tv = holder.itemView.findViewById<TextView>(R.id.item_singleline_tv)
    tv.text = list[position]
    if (position == 1) {
      tv.isClickable = false
      tv.isFocusable = false
      holder.itemView.isFocusable = false
      val str = context.getString(R.string.RegistrationActivity_call_me_instead_available_in,
        mMinFinished, mSecFinished)
      val callMeStrArray: Array<String?> = str.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      if (callMeStrArray[1] != null) {
        tv.text = callMeStrArray[1]!!.trim { it <= ' ' }
      }
    } else {
      holder.itemView.setOnClickListener { view: View? -> listener.onItemClicked(view, position) }
    }
  }

  private val mCallMeTimer: CountDownTimer = object : CountDownTimer((RegistrationConstants.FIRST_CALL_AVAILABLE_AFTER * 1000).toLong(), 1000) {
    override fun onTick(l: Long) {
      if (l / 1000 % 15 == 0L) {
        mMinFinished = (l / 1000 / 60).toInt()
        mSecFinished = (l / 1000 % 60).toInt()
        notifyItemChanged(1)
      }
    }

    override fun onFinish() {}
  }

  init {
    initTimer()
    mCallMeTimer.start()
  }

  override fun getItemCount(): Int {
    return list.size
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
  interface OnItemClickListener {
    fun onItemClicked(view: View?, position: Int)
  }
}
