package pigeon.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.thoughtcrime.securesms.R;


public class MyDecoration extends RecyclerView.ItemDecoration {

    private int distance = 0;

    @Override
    public void getItemOffsets(Rect outRect, final View view, final RecyclerView parent, RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (distance <= 0) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    distance = 76;
                    View childView = parent.getChildAt(0);
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
                    layoutParams.setMargins(0, distance, 0, 0);
                    childView.setLayoutParams(layoutParams);
                    parent.scrollToPosition(1);
                }
            });
        }
        if (pos == 0) {
            layoutParams.setMargins(0, distance, 0, 0);
        } else if (pos == 1) {
            layoutParams.setMargins(0, 0, 0, 73);
        }
        view.setLayoutParams(layoutParams);
        super.getItemOffsets(outRect, view, parent, state);
    }
}
