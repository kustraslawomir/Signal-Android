package pigeon.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.MainNavigator.REQUEST_CONFIG_CHANGES
import org.thoughtcrime.securesms.NewConversationActivity
import org.thoughtcrime.securesms.components.settings.app.AppSettingsActivity.Companion.home
import org.thoughtcrime.securesms.database.SignalDatabase.Companion.threads
import org.thoughtcrime.securesms.databinding.PigeonFragmentHomePageBinding
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.notifications.MarkReadReceiver
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

      newMessageButton.focusOnLeft()
      newMessageButton.setOnClickListener {
        handleNewMessage()
      }

      markAllReadButton.focusOnLeft()
      markAllReadButton.setOnClickListener {
        handleMarkAllRead()
      }
      settingsButton.focusOnLeft()
      settingsButton.setOnClickListener { handleAppSettings() }

      newGroupButton.focusOnLeft()
    }
  }

  private fun handleNewMessage() {
    startActivity(Intent(requireActivity(), NewConversationActivity::class.java))
  }

  private fun handleAppSettings() {
    requireActivity().startActivityForResult(home(requireContext()), REQUEST_CONFIG_CHANGES)
  }

  private fun handleMarkAllRead() {
    val context = requireContext()
    SignalExecutors.BOUNDED.execute {
      val messageIds = threads.setAllThreadsRead()
      ApplicationDependencies.getMessageNotifier().updateNotification(context)
      MarkReadReceiver.process(context, messageIds)
    }
  }
}