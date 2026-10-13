package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView textNoMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        textNoMatches = findViewById(R.id.textNoMatches);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("Suggested Recipes");
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);

        adapter = new RecipeAdapter(recipe ->
                // Recipe Detail screen is built in the next task
                Toast.makeText(this, "Detail screen coming next", Toast.LENGTH_SHORT).show());

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
    }

    // Re-run the matching every time the screen is shown
    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(
                db.getAllPantryItems(), db.getAllRecipes());
        adapter.setRecipes(suggested);
        textNoMatches.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);
    }
}