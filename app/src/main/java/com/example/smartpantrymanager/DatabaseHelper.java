package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 2;

    // Table: pantry_items
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QTY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    // Table: recipes
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // Table: recipe_ingredients
    public static final String TABLE_RECIPE_ING = "recipe_ingredients";
    public static final String COL_RECIPE_ID = "recipe_id";
    public static final String COL_ING_NAME = "ingredient_name";
    public static final String COL_ING_QTY = "quantity";
    public static final String COL_ING_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // needed for ON DELETE CASCADE
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QTY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_NAME + " TEXT NOT NULL, "
                + COL_RECIPE_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_ING_NAME + " TEXT NOT NULL, "
                + COL_ING_QTY + " REAL NOT NULL, "
                + COL_ING_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------- PANTRY CRUD ----------

    // CREATE
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QTY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, values);
    }

    // READ (all)
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_NAME + " ASC");
        while (c.moveToNext()) {
            list.add(new PantryItem(
                    c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COL_EXPIRY))));
        }
        c.close();
        return list;
    }

    // READ (one)
    public PantryItem getPantryItem(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = new PantryItem(
                    c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COL_EXPIRY)));
        }
        c.close();
        return item;
    }

    // UPDATE
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QTY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        return db.update(TABLE_PANTRY, values, COL_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    // DELETE
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ---------- RECIPES ----------

    // Adds one recipe plus its ingredients. Each ingredient is "name|quantity|unit".
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put(COL_RECIPE_NAME, name);
        rv.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, rv);

        for (String ing : ingredients) {
            String[] p = ing.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put(COL_RECIPE_ID, recipeId);
            iv.put(COL_ING_NAME, p[0]);
            iv.put(COL_ING_QTY, Double.parseDouble(p[1]));
            iv.put(COL_ING_UNIT, p[2]);
            db.insert(TABLE_RECIPE_ING, null, iv);
        }
    }

    // Runs once, when the database is first created
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Tomato Omelette",
                "1. Beat the eggs with salt.\n2. Chop the tomato and fry it in oil for 2 minutes.\n3. Pour in the eggs and cook until set.",
                "egg|3|pcs", "tomato|1|pcs", "salt|1|tsp", "oil|1|tbsp");
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Melt the butter and scramble the eggs with salt.\n3. Serve on the toast.",
                "egg|2|pcs", "bread|2|pcs", "butter|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Put the cheese between the slices.\n3. Fry both sides until golden.",
                "bread|2|pcs", "cheese|50|g", "butter|1|tbsp");
        addRecipe(db, "Plain Pancakes",
                "1. Mix flour, sugar, egg and milk into a batter.\n2. Heat oil in a pan.\n3. Fry thin pancakes, about 1 minute per side.",
                "flour|200|g", "milk|300|ml", "egg|2|pcs", "sugar|1|tbsp", "oil|1|tbsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry the chopped onion in oil.\n2. Add the cooked rice and stir.\n3. Push aside, scramble the eggs, mix together and add salt.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry onion and garlic, then add chopped tomatoes and salt.\n3. Simmer for 10 minutes and mix with the pasta.",
                "pasta|200|g", "tomato|3|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|2|tbsp", "salt|1|tsp");
        addRecipe(db, "Garlic Butter Pasta",
                "1. Boil the pasta.\n2. Melt the butter and fry the crushed garlic.\n3. Toss the pasta in the butter and add salt.",
                "pasta|200|g", "garlic|3|pcs", "butter|2|tbsp", "salt|1|tsp");
        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash.\n3. Mix in the butter, milk and salt.",
                "potato|4|pcs", "butter|2|tbsp", "milk|100|ml", "salt|1|tsp");
        addRecipe(db, "Roast Potatoes",
                "1. Heat the oven to 200C.\n2. Cut the potatoes and toss with oil and salt.\n3. Bake for 40 minutes.",
                "potato|5|pcs", "oil|3|tbsp", "salt|1|tsp");
        addRecipe(db, "Chicken and Rice",
                "1. Fry the onion and chicken pieces in oil.\n2. Add the rice, salt and water.\n3. Cover and cook for 20 minutes.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Egg Noodles",
                "1. Boil the noodles and drain.\n2. Scramble the eggs in oil.\n3. Add the noodles and salt, then toss.",
                "noodles|200|g", "egg|2|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix with the eggs and flour.\n3. Fry spoonfuls in a pan until golden.",
                "banana|2|pcs", "egg|2|pcs", "flour|100|g");
        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic in oil.\n2. Add chopped tomatoes and salt.\n3. Simmer for 20 minutes, then blend.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Potato Soup",
                "1. Fry the onion in butter.\n2. Add chopped potatoes and water, then boil until soft.\n3. Mash roughly, add milk and salt.",
                "potato|3|pcs", "onion|1|pcs", "milk|200|ml", "butter|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Rice Pudding",
                "1. Put the rice and milk in a pot.\n2. Simmer for 30 minutes, stirring often.\n3. Stir in the sugar.",
                "rice|100|g", "milk|500|ml", "sugar|2|tbsp");
        addRecipe(db, "French Toast",
                "1. Beat the eggs with milk and sugar.\n2. Dip the bread slices.\n3. Fry in butter until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|1|tbsp");
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs with salt.\n2. Melt the butter in a pan and pour in the eggs.\n3. Add the cheese and fold.",
                "egg|3|pcs", "cheese|40|g", "butter|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Chicken Stir-fry",
                "1. Fry the chicken in oil until cooked.\n2. Add sliced onion and carrots.\n3. Stir-fry for 5 minutes and add salt.",
                "chicken|300|g", "onion|1|pcs", "carrot|2|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Cheesy Baked Potato",
                "1. Bake the potatoes at 200C for 50 minutes.\n2. Cut open and add butter.\n3. Top with cheese and salt.",
                "potato|2|pcs", "cheese|50|g", "butter|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Vegetable Rice",
                "1. Fry the onion and grated carrots in oil.\n2. Add the rice, salt and water.\n3. Cover and cook for 20 minutes.",
                "rice|200|g", "carrot|2|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
    }

    // READ: all recipes, each with its ingredient list
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow(COL_ID));
            list.add(new Recipe(
                    id,
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)),
                    getIngredientsForRecipe(db, id)));
        }
        c.close();
        return list;
    }

    // READ: one recipe by id (used by the detail screen)
    public Recipe getRecipe(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    id,
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)),
                    getIngredientsForRecipe(db, id));
        }
        c.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = db.query(TABLE_RECIPE_ING, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(
                    c.getString(c.getColumnIndexOrThrow(COL_ING_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_ING_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_UNIT))));
        }
        c.close();
        return list;
    }
}