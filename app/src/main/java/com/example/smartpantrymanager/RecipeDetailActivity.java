package com.example.smartpantrymanager;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class RecipeDetailActivity extends AppCompatActivity {

    // Key used to pass the recipe id through the Intent
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Recipe");
        toolbar.setTitleTextColor(Color.WHITE);

        TextView textTitle = findViewById(R.id.textRecipeTitle);
        TextView textIngredients = findViewById(R.id.textIngredients);
        TextView textSteps = findViewById(R.id.textSteps);
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        // Read the recipe id sent by the previous screen
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);

        if (recipe == null) {
            textTitle.setText("Recipe not found");
            return;
        }

        textTitle.setText(recipe.getName());
        textSteps.setText(recipe.getSteps());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            double q = ing.getQuantity();
            String qty = (q == Math.floor(q)) ? String.valueOf((long) q) : String.valueOf(q);
            sb.append("\u2022 ").append(qty).append(" ").append(ing.getUnit())
                    .append(" ").append(ing.getName()).append("\n");
        }
        textIngredients.setText(sb.toString().trim());
    }
}