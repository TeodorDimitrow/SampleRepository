package com.example.teodordimitrov.sampleapplication.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.animation.LayoutAnimationController;

import com.example.teodordimitrov.sampleapplication.R;

/**
 * Animation utils for most of the custom animations.
 *
 * @author teodor.dimitrov on 19.3.2018 г..
 */

public class AnimationUtils {

	public static void layoutFallDownAnimation (final RecyclerView recyclerView) {
		final Context context = recyclerView.getContext();
		final LayoutAnimationController controller =
				android.view.animation.AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

		recyclerView.setLayoutAnimation(controller);
		recyclerView.scheduleLayoutAnimation();
	}

	public static void layoutGoUpAnimation (final RecyclerView recyclerView) {
		final Context context = recyclerView.getContext();
		final LayoutAnimationController controller =
				android.view.animation.AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_go_up);

		recyclerView.setLayoutAnimation(controller);
		recyclerView.scheduleLayoutAnimation();
	}
}
