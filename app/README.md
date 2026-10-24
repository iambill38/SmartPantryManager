# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user tracks the ingredients they have at home, and the app suggests only the recipes they can cook right now with those ingredients, with no shopping trip needed.

Built for Mobile App Development 700 (Richfield).

Repository: https://github.com/iambill38/SmartPantryManager

## Features
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- Pantry list screen using a RecyclerView with a custom adapter
- 20 pre-loaded recipes, seeded into the database on first run
- Suggested Recipes screen using strict matching: a recipe appears only if every ingredient is in the pantry in at least the required quantity
- Matching handles plurals ("tomato" and "tomatoes") and unit differences (g and kg, ml and l, tsp and tbsp)
- Recipe Detail screen with ingredients and method
- Settings screen with an expiring-soon alert toggle
- Input validation on the add/edit form
- Friendly message when no recipes match

## Database choice: SQLite
I chose SQLite (using SQLiteOpenHelper) because it runs locally on the device, needs no internet or server setup, and is the approach covered in the module. The data persists after the app is closed. The database has three tables: `pantry_items`, `recipes` and `recipe_ingredients`.

## Setup and run instructions
1. Install Android Studio.
2. Clone the repository:
   `git clone https://github.com/iambill38/SmartPantryManager.git`
3. Open the project folder in Android Studio and wait for Gradle to sync.
4. Create an emulator in Device Manager (for example Pixel 6) or connect an Android phone.
5. Click Run.

## Tech
- Java (no Kotlin), Android Studio
- SQLite, RecyclerView, Intents, SharedPreferences
- Minimum SDK: API 24

## Author
christian kongolo nkulu - 402110948