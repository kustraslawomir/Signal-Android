package pigeon.widget

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import pigeon.extensions.focusOnLeft

class MyPreference : Preference {

  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    super(context, attrs, defStyleAttr, defStyleRes)


  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    super(context, attrs, defStyleAttr)

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context?) : super(context)

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    holder.itemView.focusOnLeft()
    super.onBindViewHolder(holder)
  }
}