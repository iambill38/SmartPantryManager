package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class AddEditActivity extends AppCompatActivity {

    // Key used to pass the item id through the Intent
    public static final String EXTRA_ITEM_ID = "item_id";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp"};

    private DatabaseHelper db;
    private TextInputLayout layoutName, layoutQuantity;
    private TextInputEditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private int itemId = -1; // -1 means "adding a new item"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("Add Ingredient");
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        spinnerUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        // If MainActivity sent an item id, we are editing
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            toolbar.setTitle("Edit Ingredient");
            loadItem();
        }

        editExpiry.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> editExpiry.setText(""));
        buttonCancel.setOnClickListener(v -> finish());
        buttonSave.setOnClickListener(v -> saveItem());
    }

    // Fill the form with the existing item's values
    private void loadItem() {
        PantryItem item = db.getPantryItem(itemId);
        if (item == null) {
            finish();
            return;
        }
        editName.setText(item.getName());
        double q = item.getQuantity();
        editQuantity.setText(q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q));
        editExpiry.setText(item.getExpiryDate());
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String current = editExpiry.getText() == null ? "" : editExpiry.getText().toString();
        if (!current.isEmpty()) {
            try {
                String[] p = current.split("-");
                cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
                // bad date text: just open on today
            }
        }
        new DatePickerDialog(this,
                (view, year, month, day) -> editExpiry.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // Validate the form, then create or update the item
    private void saveItem() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = editName.getText() == null ? "" : editName.getText().toString().trim();
        String qtyText = editQuantity.getText() == null ? "" : editQuantity.getText().toString().trim();
        String expiry = editExpiry.getText() == null ? "" : editExpiry.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        boolean valid = true;

        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            layoutName.setError("Name must contain letters");
            valid = false;
        }

        double quantity = 0;
        if (qtyText.isEmpty()) {
            layoutQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    layoutQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) {
            return; // stay on the form so the user can fix the errors
        }

        PantryItem item = new PantryItem(itemId, name, quantity, unit, expiry);
        if (itemId == -1) {
            db.addPantryItem(item);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(item);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}