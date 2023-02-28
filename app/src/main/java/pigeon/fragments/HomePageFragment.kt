package pigeon.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.thoughtcrime.securesms.MainNavigator.REQUEST_CONFIG_CHANGES
import org.thoughtcrime.securesms.NewConversationActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.app.AppSettingsActivity.Companion.home
import org.thoughtcrime.securesms.databinding.EnterCodeOptionDialogBinding
import org.thoughtcrime.securesms.databinding.PigeonFragmentHomePageBinding
import org.thoughtcrime.securesms.permissions.Permissions
import pigeon.base.PigeonBaseFragment
import pigeon.extensions.focusOnLeft

class HomePageFragment : PigeonBaseFragment<PigeonFragmentHomePageBinding>() {
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }


  override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): PigeonFragmentHomePageBinding {
    return PigeonFragmentHomePageBinding.inflate(inflater, container, false)
  }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      binding?.run {
        newMessageButton.setOnClickListener { startActivity(Intent(requireActivity(), NewConversationActivity::class.java)) }
        newMessageButton.focusOnLeft()
        newGroupButton.focusOnLeft()
        markAllReadButton.focusOnLeft()
        settingsButton.focusOnLeft()
        settingsButton.setOnClickListener { goToAppSettings() }
        searchButton.focusOnLeft()
      }
    }

    private fun goToAppSettings() {
        requireActivity().startActivityForResult(home(requireContext()), REQUEST_CONFIG_CHANGES)
    }
}