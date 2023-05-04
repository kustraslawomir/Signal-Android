package pigeon.extensions;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;

public class MenuItem {
    private final static String TAG = "appmgr.MenuItem";

    protected static Activity _activity;

    public enum Direction {UP, DOWN};

    private static Animation upandvisible;
    private static Animation upandvisible2;
    private static Animation downandvisible;
    private static Animation downandvisible2;

    public static void init(Activity a) {
        _activity = a;
        upandvisible = AnimationUtils.loadAnimation(a, R.anim.upandvisible);
        upandvisible2 = AnimationUtils.loadAnimation(a, R.anim.upandvisible);
        downandvisible = AnimationUtils.loadAnimation(a, R.anim.downandvisible);
        downandvisible2 = AnimationUtils.loadAnimation(a, R.anim.downandvisible);
    }

    public static void term() {
        _activity = null;
    }

    public static void animate(View view, Direction direction) {
        switch (direction) {
            case UP:
                view.startAnimation(upandvisible);
                break;
            case DOWN:
                view.startAnimation(downandvisible);
                break;
        }
    }

}
