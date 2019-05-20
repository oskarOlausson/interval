package se.umu.oskar.interval.controller.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import se.umu.oskar.interval.R;

public class FinishedWorkout extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);
        setupToolbar();

        ImageView check = findViewById(R.id.image_check);
        check.setOnClickListener(l -> goToMenu());

        if (savedInstanceState == null) {
            Animation cool = AnimationUtils.loadAnimation(this, R.anim.check_animation);
            Animation moveUp = AnimationUtils.loadAnimation(this, R.anim.item2_to_item1);
            check.startAnimation(cool);

            TextView wellDone = findViewById(R.id.text_well_done);
            wellDone.setVisibility(View.INVISIBLE);

            cool.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    wellDone.setVisibility(View.VISIBLE);
                    wellDone.startAnimation(moveUp);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        goToMenu();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundResource(R.color.green);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(l -> goToMenu());
    }

    private void goToMenu() {
        finish();
    }
}
