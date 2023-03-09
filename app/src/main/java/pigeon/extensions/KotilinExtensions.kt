package pigeon.extensions

import android.animation.ValueAnimator
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import org.thoughtcrime.securesms.R


fun View.focusOnLeft(vararg childs: TextView) {

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

        (this as? TextView)?.setupTextSize(hasFocus)
        this.setupEllipsize(hasFocus)
        childs.forEach {
          (it as? TextView)?.setupTextSize(hasFocus)
          it.setupEllipsize(hasFocus)
        }
      }
    }
    this.onFocusChangeListener = focus
  }
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

fun View.recyclerFocusOnLeft(vararg childs: TextView) {

  if (!isSignalVersion()) {
    val BUTTON_SCALE_FOCUS = 1.3f
    val BUTTON_SCALE_NON_FOCUS = 1.0f
    val BUTTON_TRANSLATION_X_FOCUS = 12.0f
    val BUTTON_TRANSLATION_X_NON_FOCUS = 1.0f

    val focus = View.OnFocusChangeListener { _, hasFocus ->
      val scale: Float = if (hasFocus) {
        BUTTON_SCALE_FOCUS
      } else {
        BUTTON_SCALE_NON_FOCUS
      }

      val translationX: Float = if (hasFocus) {
        BUTTON_TRANSLATION_X_FOCUS
      } else {
        BUTTON_TRANSLATION_X_NON_FOCUS
      }

      ViewCompat.animate(this)
        .scaleX(scale)
        .scaleY(scale)
        .translationX(translationX)
        .start()

      val color = if (hasFocus) {
        R.color.white_focus
      } else {
        R.color.white_not_focus
      }

      childs.forEach {
        it.setTextColor(ContextCompat.getColor(this.context, color))
      }
    }
    this.onFocusChangeListener = focus
  }
}

fun View.focusOnRight(vararg childs: View) {

  if (!isSignalVersion()) {
    val BUTTON_SCALE_FOCUS = 1.5f
    val BUTTON_SCALE_NON_FOCUS = 1.0f

    val focus = View.OnFocusChangeListener { _, hasFocus ->
      val scale: Float = if (hasFocus) {
        BUTTON_SCALE_FOCUS
      } else {
        BUTTON_SCALE_NON_FOCUS
      }

      this.post {
        this.setupEllipsize(hasFocus)
        childs.forEach {
          it.setupEllipsize(hasFocus)
          if (it.tag != "header") {
            val params = it.layoutParams as ViewGroup.MarginLayoutParams

            if (hasFocus) {
              params.width = 210
              params.marginStart = 60
            } else {
              params.width = 310
              params.marginStart = 0
            }

            it.layoutParams = params
            it.requestLayout()
            ViewCompat.animate(it)
              .scaleX(scale)
              .scaleY(scale)
              .start()
          }
        }
      }

    }
    this.onFocusChangeListener = focus
  }
}

fun TextView.setBigText() {
  this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f)
}

fun EditText.animateGroup(parent: TextView) {
  if (!isSignalVersion()) {
    val BUTTON_SCALE_FOCUS = 1.3f
    val BUTTON_SCALE_NON_FOCUS = 1.0f
    val BUTTON_TRANSLATION_X_FOCUS = 12.0f
    val BUTTON_TRANSLATION_X_NON_FOCUS = 1.0f
    val BUTTON_TRANSLATION_X_FOCUS_PARENT = -30.0f
    val BUTTON_TRANSLATION_X_NON_FOCUS_PARENT = 1.0f

    val focus = View.OnFocusChangeListener { _, hasFocus ->
      val scale: Float = if (hasFocus) {
        BUTTON_SCALE_FOCUS
      } else {
        BUTTON_SCALE_NON_FOCUS
      }

      val translationX: Float = if (hasFocus) {
        BUTTON_TRANSLATION_X_FOCUS
      } else {
        BUTTON_TRANSLATION_X_NON_FOCUS
      }

      val translationParentX: Float = if (hasFocus) {
        BUTTON_TRANSLATION_X_FOCUS_PARENT
      } else {
        BUTTON_TRANSLATION_X_NON_FOCUS_PARENT
      }

      ViewCompat.animate(this)
        .scaleX(scale)
        .scaleY(scale)
        .translationX(translationX)
        .start()

      ViewCompat.animate(parent)
        .translationX(translationParentX)
        .start()

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