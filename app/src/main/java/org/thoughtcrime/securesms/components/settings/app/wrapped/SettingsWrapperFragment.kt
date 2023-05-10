package org.thoughtcrime.securesms.components.settings.app.wrapped

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.thoughtcrime.securesms.R
import pigeon.extensions.focusOnLeft
import pigeon.extensions.isSignalVersion

/**
 * Wraps a fragment to give it a Settings style toolbar. This class should be used sparingly, and
 * is really only here as stop-gap as we migrate more settings screens to the new UI
 */
abstract class SettingsWrapperFragment : Fragment(R.layout.settings_wrapper_fragment) {

  protected lateinit var toolbar: Toolbar
    private set

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    toolbar = view.findViewById(R.id.toolbar)

    toolbar.setNavigationOnClickListener {
      onBackPressed()
    }

    if (!isSignalVersion()){
      toolbar.isVisible = false
      view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
    }

    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, OnBackPressed())

    childFragmentManager
      .beginTransaction()
      .replace(R.id.wrapped_fragment, getFragment())
      .commit()
  }

  abstract fun getFragment(): Fragment

  fun setTitle(@StringRes titleId: Int) {
    toolbar.setTitle(titleId)
  }

  private fun onBackPressed() {
    if (!childFragmentManager.popBackStackImmediate()) {
      requireActivity().onNavigateUp()
    }
  }

  private inner class OnBackPressed : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      onBackPressed()
    }
  }
}
