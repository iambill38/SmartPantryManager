package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    // Returns ONLY recipes where every ingredient is in the pantry in at least the required amount
    public static List<Recipe> getSuggestedRecipes(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, totals)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    // One missing or too-small ingredient means the whole recipe fails
    private static boolean canMake(Recipe recipe, Map<String, Double> totals) {
        if (recipe.getIngredients().isEmpty()) {
            return false;
        }
        for (RecipeIngredient ing : recipe.getIngredients()) {
            String key = makeKey(ing.getName(), ing.getUnit());
            Double have = totals.get(key);
            double need = toBaseQuantity(ing.getQuantity(), ing.getUnit());
            if (have == null || have < need - 0.000001) {
                return false;
            }
        }
        return true;
    }

    // Adds up the pantry per ingredient, so two "egg" entries count together
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = makeKey(item.getName(), item.getUnit());
            double qty = toBaseQuantity(item.getQuantity(), item.getUnit());
            Double existing = totals.get(key);
            totals.put(key, existing == null ? qty : existing + qty);
        }
        return totals;
    }

    // Key = clean ingredient name + unit type, e.g. "tomato|count"
    private static String makeKey(String name, String unit) {
        return normalizeName(name) + "|" + unitType(unit);
    }

    // "Tomatoes" and "tomato" both become "tomato"
    static String normalizeName(String name) {
        String n = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (n.endsWith("ies") && n.length() > 3) {
            return n.substring(0, n.length() - 3) + "y";
        }
        if (n.endsWith("oes")) {
            return n.substring(0, n.length() - 2);
        }
        if (n.endsWith("s") && !n.endsWith("ss")) {
            return n.substring(0, n.length() - 1);
        }
        return n;
    }

    private static String unitType(String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
                return "volume";
            case "pcs":
                return "count";
            default:
                return "other-" + unit;
        }
    }

    // Converts to a base unit: grams, millilitres, or pieces
    private static double toBaseQuantity(double qty, String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "kg":
            case "l":
                return qty * 1000;
            case "tbsp":
                return qty * 15;
            case "tsp":
                return qty * 5;
            default:
                return qty; // g, ml, pcs
        }
    }
}