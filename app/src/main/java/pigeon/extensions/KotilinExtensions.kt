package pigeon.extensions

import android.animation.ValueAnimator
import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.longmessage.TAG
import pigeon.animation.CenterLayoutManager
import pigeon.animation.MyDecoration

fun RecyclerView.test() {
  layoutManager = CenterLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
  val snapHelper = LinearSnapHelper()
  snapHelper.attachToRecyclerView(this)
  onFlingListener = snapHelper
  addItemDecoration(MyDecoration())
}

private fun setTextSwitcher(hasFocus: Boolean, view: View) {
  when (hasFocus) {
    false -> {
      view.animation = (AnimationUtils.loadAnimation(view.context, R.anim.upandvisible))
      view.animation = (AnimationUtils.loadAnimation(view.context, R.anim.upandgone))
    }
   true -> {
      view.animation = (AnimationUtils.loadAnimation(view.context, R.anim.downandvisible))
      view.animation = (AnimationUtils.loadAnimation(view.context, R.anim.downandgone))
    }
  }
}

fun View.focusOnLeft() {

  if (isPigeonVersion()) {
    this.alpha = if (this.isEnabled) {
      1.0f
    } else {
      0.5f
    }
    val focus = View.OnFocusChangeListener { _, hasFocus ->
      this.post {

        this.alpha = if (this.isEnabled) {
          1.0f
        } else {
          0.5f
        }
        val params = this.layoutParams as ViewGroup.MarginLayoutParams

        if (hasFocus) {
          params.marginStart = 5
        } else {
          params.marginStart = 30
        }

        this.layoutParams = params
        this.requestLayout()

        this.getAllChildren().forEach {
          if (it.tag != "header") {
            (it as? TextView)?.setupTextSize(hasFocus)
            setTextSwitcher(hasFocus, it)
          }
          it.setupEllipsize(hasFocus)
        }

      }
    }
    this.onFocusChangeListener = focus
  }
}

fun RecyclerView.showWhenScrolledToBottom(view: View) {
  this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      super.onScrollStateChanged(recyclerView, newState)
      val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
      org.signal.core.util.logging.Log.w(TAG, "position: $position")
      view.shouldBeVisibleIf(position == 0)
    }
  })
}

fun View.shouldBeVisibleIf(condition: Boolean) {
  visibility = if (condition) {
    View.VISIBLE
  } else {
    View.GONE
  }
}

fun View.getAllChildren(): List<View> {
  val result = ArrayList<View>()
  if (this !is ViewGroup) {
    result.add(this)
  } else {
    for (index in 0 until this.childCount) {
      val child = this.getChildAt(index)
      result.addAll(child.getAllChildren())
    }
  }
  return result
}

fun TextView.setupTextSize(hasFocus: Boolean) {
  val animationDuration: Long = 0

  val animator = if (hasFocus) {
    ValueAnimator.ofFloat(this.textSize, (this.textSize * 1.5).toFloat())
  } else {
    ValueAnimator.ofFloat(this.textSize, (this.textSize / 1.5).toFloat())
  }

  animator.duration = animationDuration

  animator.addUpdateListener { valueAnimator ->
    val animatedValue = valueAnimator.animatedValue as Float
    this.textSize = animatedValue
  }

  animator.start()

}

fun View.setupEllipsize(hasFocus: Boolean) {
  val color = if (hasFocus) {
    R.color.white_focus
  } else {
    R.color.white_not_focus
  }
  if (this is TextView) {
    this.setTextColor(ContextCompat.getColor(this.context, color))
  }
  if (hasFocus) {
    if (this is TextView && this !is EditText) {
      this.ellipsize = TextUtils.TruncateAt.MARQUEE
      this.isSelected = true
    }
  } else {
    if (this is TextView) {
      this.ellipsize = TextUtils.TruncateAt.END
    }
  }
}

fun View.focusOnRight() {

  if (!isSignalVersion()) {
    val focus = View.OnFocusChangeListener { _, hasFocus ->
      this.getAllChildren().forEach {
        if (it.tag != "header") {
          (it as? TextView)?.setupTextSize(hasFocus)
        }
        it.setupEllipsize(hasFocus)
      }
    }
    this.onFocusChangeListener = focus
  }
}

fun EditText.animateGroup(parent: TextView) {
  if (!isSignalVersion()) {

    val focus = View.OnFocusChangeListener { _, hasFocus ->
      this.post {
        val params = this.layoutParams as ViewGroup.MarginLayoutParams

        if (hasFocus) {
          params.marginStart = 5
        } else {
          params.marginStart = 30
        }

        this.layoutParams = params
        this.requestLayout()
      }

      parent.post {
        val params = parent.layoutParams as ViewGroup.MarginLayoutParams

        if (hasFocus) {
          params.marginStart = 5
        } else {
          params.marginStart = 30
        }

        parent.layoutParams = params
        parent.requestLayout()
      }

      if (this.tag != "header") {
        (this as? TextView)?.setupTextSize(hasFocus)
      }
      this.setupEllipsize(hasFocus)

      if (parent.tag != "header") {
        (parent as? TextView)?.setupTextSize(hasFocus)
      }
      parent.setupEllipsize(hasFocus)

      val parentColor = if (hasFocus) {
        R.color.white_focus
      } else {
        R.color.white_not_focus
      }

      parent.setTextColor(ContextCompat.getColor(this.context, parentColor))
      this.setTextColor(ContextCompat.getColor(this.context, parentColor))
    }
    this.onFocusChangeListener = focus
  }
}

fun View.focusColor(vararg childs: TextView) {
  if (!isSignalVersion()) {
    val focus = View.OnFocusChangeListener { _, hasFocus ->
      this.setupEllipsize(hasFocus)
      childs.forEach { it.setupEllipsize(hasFocus) }
    }
    this.onFocusChangeListener = focus
  }
}