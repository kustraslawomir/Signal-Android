package pigeon.fragments

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.MainActivity
import org.thoughtcrime.securesms.MainNavigator.REQUEST_CONFIG_CHANGES
import org.thoughtcrime.securesms.NewConversationActivity
import org.thoughtcrime.securesms.components.settings.app.AppSettingsActivity.Companion.home
import org.thoughtcrime.securesms.database.SignalDatabase.Companion.threads
import org.thoughtcrime.securesms.databinding.PigeonFragmentHomePageBinding
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.groups.ui.creategroup.CreateGroupActivity
import org.thoughtcrime.securesms.messages.IncomingMessageObserver
import org.thoughtcrime.securesms.notifications.MarkReadReceiver
import org.thoughtcrime.securesms.permissions.Permissions
import pigeon.base.PigeonBaseFragment
import pigeon.extensions.focusOnLeft


class HomePageFragment : PigeonBaseFragment<PigeonFragmentHomePageBinding>() {

  private lateinit var mainActivity: MainActivity

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
  }

  override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): PigeonFragmentHomePageBinding {
    return PigeonFragmentHomePageBinding.inflate(inflater, container, false)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    mainActivity = context as MainActivity
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding?.run {

      newMessageButton.focusOnLeft()
      newMessageButton.setOnClickListener { handleNewMessage() }

      markAllReadButton.focusOnLeft()
      markAllReadButton.setOnClickListener {
        handleMarkAllRead()
      }
      settingsButton.focusOnLeft()
      settingsButton.setOnClickListener { handleAppSettings() }

      newGroupButton.focusOnLeft()
      newGroupButton.setOnClickListener { goToGroupCreation() }

      searchButton.focusOnLeft()
      searchButton.setOnClickListener {
        mainActivity.collapseHomePage()
      }
    }
  }

  private fun handleNewMessage() {
    startActivity(Intent(requireActivity(), NewConversationActivity::class.java))
  }

  private fun handleAppSettings() {
    requireActivity().startActivityForResult(home(requireContext()), REQUEST_CONFIG_CHANGES)
  }

  private fun goToGroupCreation() {
    requireActivity().startActivity(CreateGroupActivity.newIntent(requireContext()));
  }

  private fun handleMarkAllRead() {
    val context = requireContext()
    NotificationManagerCompat.from(context).cancelAll()
    val notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    notificationManager.cancelAll()
    SignalExecutors.BOUNDED.execute {
      val messageIds = threads.setAllThreadsRead()
      ApplicationDependencies.getMessageNotifier().updateNotification(context)
      MarkReadReceiver.process(context, messageIds)
    }
  }
}