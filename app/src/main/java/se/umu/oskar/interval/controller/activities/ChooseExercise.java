package se.umu.oskar.interval.controller.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.Tags;
import se.umu.oskar.interval.controller.adapters.ExerciseAdapter;
import se.umu.oskar.interval.files.ExerciseCatalog;
import se.umu.oskar.interval.view.DialogMaker;
import se.umu.oskar.interval.view.ViewUtilities;

public class ChooseExercise extends AppCompatActivity {

    private ExerciseCatalog catalog;
    private ExerciseAdapter adapter;
    private View deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        catalog = getCatalog(savedInstanceState);

        setUpToolBar();

        RecyclerView recyclerView = findViewById(R.id.exercises);
        recyclerView.setLayoutManager(ViewUtilities.verticalLayoutManager(this));

        adapter = new ExerciseAdapter(catalog.exercises, e -> {
            Intent resultIntent = new Intent();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            resultIntent.putExtra(String.class.toString(), e);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        if (savedInstanceState != null) {
            adapter.restoreInstanceState(savedInstanceState);
        }

        deleteButton = findViewById(R.id.button_delete);
        updateEditMode();

        deleteButton.setOnClickListener(l -> {
            adapter.deleteSelected();
            updateEditMode(false);
        });

        recyclerView.setAdapter(adapter);
    }

    private void updateEditMode(boolean editMode) {
        adapter.setEditMode(editMode);
        updateEditMode();
    }

    private void updateEditMode() {
        invalidateOptionsMenu();
        deleteButton.setVisibility(adapter.isInEditMode() ? View.VISIBLE : View.GONE);
    }

    private ExerciseCatalog getCatalog(Bundle savedInstanceState) {
        final ExerciseCatalog catalog;

        if (savedInstanceState == null) {
            try {
                catalog = new ExerciseCatalog(this);
            } catch (IOException e) {
                throw new RuntimeException("Could not load exercise-catalog");
            }
        } else {
            ArrayList<String> exercises =
                    savedInstanceState.getStringArrayList(String.class.toString());

            catalog = new ExerciseCatalog(this, exercises);
        }

        return catalog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (adapter.isInEditMode()) {
            getMenuInflater().inflate(R.menu.cancel, menu);
        } else {
            getMenuInflater().inflate(R.menu.delete_and_add, menu);
        }

        return true;
    }

    public void addExercise(String name) {
        name = name.replace("\n", " ").trim();

        if (!catalog.exercises.contains(name)) {
            catalog.exercises.add(0, name);
            adapter.notifyItemInserted(0);
        } else {
            Toast.makeText(this, R.string.already_used, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                DialogMaker.stringDialog(this, R.string.create_exercise, R.string.create, R.string.hint_exercise, this::addExercise);
                return true;
            case R.id.delete:
                updateEditMode(true);
                return true;
            case R.id.cancel:
                updateEditMode(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            catalog.writeContentsToFile();
        } catch (IOException e1) {
            Log.w(Tags.oskarTag, "Could not save exercises to file");
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter.isInEditMode()) {
            adapter.setEditMode(false);
            updateEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(String.class.toString(), catalog.exercises);
        adapter.onSaveInstanceState(outState);
    }

    private void setUpToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setBackgroundResource(R.color.white);
        toolbar.setNavigationOnClickListener(l -> finish());
    }
}
