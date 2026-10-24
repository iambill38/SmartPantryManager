package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int EXPIRY_WARNING_DAYS = 3;

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView textEmpty;
    private TextView textAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep content out from under the status/navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        textEmpty = findViewById(R.id.textEmpty);
        textAlert = findViewById(R.id.textAlert);

        // Toolbar: title, white icons, and menu navigation to other screens
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Pantry");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.main_menu);
        Menu menu = toolbar.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable icon = menuItem.getIcon();
            if (icon != null) {
                icon.mutate().setTint(Color.WHITE);
                menuItem.setIcon(icon);
            }
        }
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        adapter = new PantryAdapter(new PantryAdapter.OnItemActionListener() {
            @Override
            public void onEdit(PantryItem item) {
                // Explicit Intent: open the form and pass the item id
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra(AddEditActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(PantryItem item) {
                confirmDelete(item);
            }
        });

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditActivity.class)));
    }

    // Reload the list (and the alert banner) every time this screen comes back into view
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setItems(items);
        textEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        updateExpiryBanner(items);
    }

    // Show the warning only if the user has alerts switched on in Settings
    private void updateExpiryBanner(List<PantryItem> items) {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean alertsOn = prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true);
        int count = alertsOn ? countExpiringSoon(items) : 0;

        if (count > 0) {
            String word = (count == 1) ? "item expires" : "items expire";
            textAlert.setText("Warning: " + count + " " + word + " within "
                    + EXPIRY_WARNING_DAYS + " days (or already expired).");
            textAlert.setVisibility(View.VISIBLE);
        } else {
            textAlert.setVisibility(View.GONE);
        }
    }

    // Counts items whose expiry date is today, in the past, or within the next 3 days
    private int countExpiringSoon(List<PantryItem> items) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayMillis = today.getTimeInMillis();

        int count = 0;
        for (PantryItem item : items) {
            String expiry = item.getExpiryDate();
            if (expiry == null || expiry.isEmpty()) {
                continue;
            }
            try {
                Date date = format.parse(expiry);
                if (date == null) {
                    continue;
                }
                long days = Math.round((date.getTime() - todayMillis) / 86400000.0);
                if (days <= EXPIRY_WARNING_DAYS) {
                    count++;
                }
            } catch (Exception ignored) {
                // Unreadable date: skip this item
            }
        }
        return count;
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}