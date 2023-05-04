package pigeon.animation;


import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class CenterLayoutManager extends LinearLayoutManager {

    static int lastPositon = 0;
    static int targetPosion = 0;

    public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        CenterSmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        smoothScroller.setFinalPosition(recyclerView.getAdapter().getItemCount() - 1);
        startSmoothScroll(smoothScroller);
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int lastPositon, int position) {
        this.lastPositon = lastPositon;
        this.targetPosion = position;
        smoothScrollToPosition(recyclerView, state, position);
    }

    public static class CenterSmoothScroller extends LinearSmoothScroller {

        private int mFinalPosition;

        private static float duration = 300f;

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        public void setFinalPosition(int finalPosition) {
            mFinalPosition = finalPosition;
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            if (lastPositon == 0 && targetPosion == 1) {
                viewStart = viewStart - 22;
                viewEnd = viewEnd - 22;
            } else if (targetPosion == 2) {
                viewStart = viewStart + 5;
                viewEnd = viewEnd + 5;
            }
            return 92 - (viewStart + viewEnd) / 2;
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            float newDuration = duration / (Math.abs(targetPosion - lastPositon));
            return newDuration / displayMetrics.densityDpi;
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            return super.calculateTimeForScrolling(dx);
        }

    }

    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        public TopSnappedSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return this.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return -1;
        }
    }
}