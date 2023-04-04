package org.thoughtcrime.securesms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.components.voice.VoiceNoteMediaController;
import org.thoughtcrime.securesms.components.voice.VoiceNoteMediaControllerOwner;
import org.thoughtcrime.securesms.conversationlist.RelinkDevicesReminderBottomSheetFragment;
import org.thoughtcrime.securesms.devicetransfer.olddevice.OldDeviceTransferLockedDialog;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.main.MainActivityListHostFragment;
import org.thoughtcrime.securesms.stories.Stories;
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabRepository;
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsViewModel;
import org.thoughtcrime.securesms.util.AppStartup;
import org.thoughtcrime.securesms.util.CachedInflater;
import org.thoughtcrime.securesms.util.CommunicationActions;
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.FeatureFlags;
import org.thoughtcrime.securesms.util.SplashScreenUtil;
import org.thoughtcrime.securesms.util.WindowUtil;

import static pigeon.extensions.BuildExtensionsKt.isSignalVersion;

public class MainActivity extends PassphraseRequiredActivity implements VoiceNoteMediaControllerOwner {

  public static final int RESULT_CONFIG_CHANGED = Activity.RESULT_FIRST_USER + 901;

  private final DynamicTheme  dynamicTheme = new DynamicNoActionBarTheme();
  private final MainNavigator navigator    = new MainNavigator(this);

  private VoiceNoteMediaController      mediaController;
  private ConversationListTabsViewModel conversationListTabsViewModel;

  private boolean onFirstRender = false;

  public static @NonNull Intent clearTop(@NonNull Context context) {
    Intent intent = new Intent(context, MainActivity.class);

    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

    return intent;
  }

  @Override
  protected void onPreCreate() {
    super.onPreCreate();
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    AppStartup.getInstance().onCriticalRenderEventStart();
    super.onCreate(savedInstanceState, ready);

    setContentView(R.layout.main_activity);
    final View content = findViewById(android.R.id.content);
    content.getViewTreeObserver().addOnPreDrawListener(
        new ViewTreeObserver.OnPreDrawListener() {
          @Override
          public boolean onPreDraw() {
            // Use pre draw listener to delay drawing frames till conversation list is ready
            if (onFirstRender) {
              content.getViewTreeObserver().removeOnPreDrawListener(this);
              return true;
            } else {
              return false;
            }
          }
        });


    mediaController = new VoiceNoteMediaController(this, true);

    ConversationListTabRepository         repository = new ConversationListTabRepository();
    ConversationListTabsViewModel.Factory factory    = new ConversationListTabsViewModel.Factory(repository);

    handleGroupLinkInIntent(getIntent());
    handleProxyInIntent(getIntent());
    handleSignalMeIntent(getIntent());

    CachedInflater.from(this).clear();

    conversationListTabsViewModel = new ViewModelProvider(this, factory).get(ConversationListTabsViewModel.class);
    updateTabVisibility();
    findViewById(R.id.pre_loader_value).requestFocus();
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    if (SignalStore.misc().isOldDeviceTransferLocked()) {
      OldDeviceTransferLockedDialog.show(getSupportFragmentManager());
    }

    if (SignalStore.misc().getShouldShowLinkedDevicesReminder()) {
      SignalStore.misc().setShouldShowLinkedDevicesReminder(false);
      RelinkDevicesReminderBottomSheetFragment.show(getSupportFragmentManager());
    }

    updateTabVisibility();
  }

  private void handleGroupLinkInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialGroupLinkUrl(this, data.toString());
    }
  }

  @Override
  public Intent getIntent() {
    return super.getIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                      Intent.FLAG_ACTIVITY_NEW_TASK |
                                      Intent.FLAG_ACTIVITY_SINGLE_TOP);
  }

  private void handleProxyInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialProxyLinkUrl(this, data.toString());
    }
  }

  private void handleSignalMeIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialSignalMeUrl(this, data.toString());
    }
  }

  private void updateTabVisibility() {
    if (isSignalVersion()) {
      if (Stories.isFeatureEnabled()) {
        findViewById(R.id.conversation_list_tabs).setVisibility(View.VISIBLE);
        WindowUtil.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.signal_colorSurface2));
      } else {
        findViewById(R.id.conversation_list_tabs).setVisibility(View.GONE);
        WindowUtil.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.signal_colorBackground));
        conversationListTabsViewModel.onChatsSelected();
      }
    }
  }

  public void collapseHomePage() {
    MainActivityListHostFragment fragment = (MainActivityListHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    if (fragment != null) {
      fragment.showSearchBar();
      findViewById(R.id.homePageFragment).setVisibility(View.GONE);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    SplashScreenUtil.setSplashScreenThemeIfNecessary(this, SignalStore.settings().getTheme());
  }

  @Override
  public void onBackPressed() {
    if (isTaskRoot() && !isHomePageVisible()) {
      expandHomePage();
    }
    hideArchivedConversations();
    if (!navigator.onBackPressed()) {
      super.onBackPressed();
    }
  }

  public boolean isHomePageVisible() {
    return findViewById(R.id.homePageFragment).getVisibility() == View.VISIBLE;
  }

  public void expandHomePage() {
    MainActivityListHostFragment fragment = (MainActivityListHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    if (fragment != null) {
      fragment.hideSearchBar();
      findViewById(R.id.homePageFragment).setVisibility(View.VISIBLE);
    }
  }

  public void hideArchivedConversations() {
    MainActivityListHostFragment fragment = (MainActivityListHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    if (fragment != null) {
      expandHomePage();
      fragment.hideArchivedConversations();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleGroupLinkInIntent(intent);
    handleProxyInIntent(intent);
    handleSignalMeIntent(intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == MainNavigator.REQUEST_CONFIG_CHANGES && resultCode == RESULT_CONFIG_CHANGED) {
      recreate();
    }
  }

  public @NonNull MainNavigator getNavigator() {
    return navigator;
  }

  public void onFirstRender() {
    onFirstRender = true;
  }

  @Override
  public @NonNull VoiceNoteMediaController getVoiceNoteMediaController() {
    return mediaController;
  }

  public void showMainContentExt() {
    FrameLayout view = findViewById(R.id.pre_loader);
    if (view != null) {
      view.setVisibility(View.GONE);
    }
  }

}
