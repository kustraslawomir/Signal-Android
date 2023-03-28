package org.thoughtcrime.securesms.components;


import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatToggleButton;

import org.thoughtcrime.securesms.R;

public class AccessibleToggleButton extends AppCompatToggleButton {

  private OnCheckedChangeListener listener;

  public AccessibleToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public AccessibleToggleButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AccessibleToggleButton(Context context) {
    super(context);
  }

  @Override
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    super.setOnCheckedChangeListener(listener);
    this.listener = listener;
  }

  public void setChecked(boolean checked, boolean notifyListener) {
    if (!notifyListener) {
      super.setOnCheckedChangeListener(null);
    }

    String state = getContext().getString(R.string.preferences_off);
    if (checked) {
      state = getContext().getString(R.string.preferences_on);
    }
    this.setText(getContext().getString(R.string.WebRtcCallView__mute) + ":" + state);

    super.setChecked(checked);

    if (!notifyListener) {
      super.setOnCheckedChangeListener(listener);
    }
  }

  public OnCheckedChangeListener getOnCheckedChangeListener() {
    return this.listener;
  }

}
