package com.azhar.waiter.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


// This class represents an available dish on the restaurants menu.
// Implements the Serializable interface so that it can be passed inside a bundle object.
// ----------------------------------------------------------------------------

public class Dish implements Serializable {

    // Object attributes
    public final String name;
    public final String description;
    public final URL imageUrl;
    public final BigDecimal price;
    public final ArrayList<Allergen> allergens;


    // Class constructor
    // (initializes all attributes but doesn't populate the allergen list)
    public Dish(String name, String description, String imageUrl, BigDecimal price) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.allergens = new ArrayList<Allergen>();

        URL url;
        try                             {   url = new URL(imageUrl);  }
        catch (MalformedURLException e) {   url = null;               }

        this.imageUrl = url;
    }


    // Other methods:

    // Adds a new allergen to the dish
    public void addAllergen(Allergen newAllergen) {
        allergens.add(newAllergen);
    }

    // Returns a string with the dish info (for debugging)
    @Override
    public String toString() {

        String res = "\nName: " + name + ", Description: " + description + ", Price: " + price + ", Image: ";

        if (imageUrl == null)
            res += "<none>";
        else
            res += imageUrl;

        res += "\nAllergens: " + allergens.size();

        for (Allergen a: allergens) {

            res += "\n" + a;
        }

        return res;
    }
}
