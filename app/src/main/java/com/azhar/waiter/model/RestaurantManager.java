package com.azhar.waiter.model;

import android.content.Context;
import android.util.Log;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;


// This class contains and manages all the data in the app model.
// This class is a singleton object, and all its public methods are static.
// ----------------------------------------------------------------------------

public class RestaurantManager {

    // Class attributes
    private static RestaurantManager singleton;   // The singleton object stored in the class


    // Object attributes
    private Date mMenuDate;
    private String mCurrency;
    private BigDecimal mTaxRate;

    private ArrayList<Allergen> availableAllergens;
    private ArrayList<Dish> availableDishes;
    private ArrayList<Table> tables;


    // Class constructors:

    // Initializes all attributes, but doesn't populate the allergen and dish lists
    // (the constructor is private, use newInstance() to create a new object from outside)
    private RestaurantManager(Date menuDate, String currency, BigDecimal taxRate, int tableCount, String tablePrefix) {

        mMenuDate = menuDate;
        mCurrency = currency;
        mTaxRate = taxRate;
        this.availableAllergens = new ArrayList<Allergen>();
        this.availableDishes = new ArrayList<Dish>();
        this.tables = new ArrayList<Table>();

        for (int i=1; i<=tableCount; i++)
            tables.add(new Table(tablePrefix + " " + i));
    }

    // Creates a new instance of the singleton object
    public static void newInstance(Date menuDate, String currency, BigDecimal taxRate, int tableCount, String tablePrefix) {

        singleton = new RestaurantManager(menuDate, currency, taxRate, tableCount, tablePrefix);
    }


    // Class getters:

    public static Date getMenuDate() {

        if (singleton == null)
            return null;

        return singleton.mMenuDate;
    }

    public static String getCurrency() {

        if (singleton == null)
            return null;

        return singleton.mCurrency;
    }

    public static BigDecimal getTaxRate() {

        if (singleton == null)
            return null;

        return singleton.mTaxRate;
    }

    public static ArrayList<Dish> getDishes() {

        if (singleton == null)
            return null;

        return singleton.availableDishes;
    }

    public static ArrayList<Table> getTables() {

        if (singleton == null)
            return null;

        return singleton.tables;
    }


    // These methods are 'syntactic sugar' class getters

    public static Dish getDishAtPos(int pos) {

        if (singleton == null)
            return null;

        try {
            return singleton.availableDishes.get(pos);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static Table getTableAtPos(int pos) {

        if (singleton == null)
            return null;

        try {
            return singleton.tables.get(pos);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static Order getOrderAtPos_InTable(int orderPos, int tablePos) {

        if (singleton == null)
            return null;

        Table table = getTableAtPos(tablePos);

        if ( table == null )
            return null;

        try {
            return table.getOrders().get(orderPos);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }


    // Other methods:

    // Indicates if the singleton object has been properly initialized
    public static boolean isSingletonReady() {
        return ( singleton != null);
    }

    // Returns the count of available allergens
    public static int allergenCount() {

        if (singleton == null)
            return 0;

        return singleton.availableAllergens.size();
    }

    // Returns the count of available dishes
    public static int dishCount() {

        if (singleton == null)
            return 0;

        return singleton.availableDishes.size();
    }

    // Returns the count of tables
    public static int tableCount() {

        if (singleton == null)
            return 0;

        return singleton.tables.size();
    }

    // Adds a new allergen to the list of available allergens
    public static boolean addAllergen(Allergen newAllergen) {

        if (singleton == null)
            return false;

        return singleton.availableAllergens.add(newAllergen);
    }

    // Adds a new dish to the list of available dishes
    public static boolean addDish(Dish newDish) {

        if (singleton == null)
            return false;

        return singleton.availableDishes.add(newDish);
    }

    // Returns the available allergen with a given taskId, or null if taskId doesn't exist
    public static Allergen getAllergenById(int searchId) {

        if (singleton == null)
            return null;

        for (Allergen a: singleton.availableAllergens) {

            if (a.id == searchId) {
                return a;
            }
        }

        return null;
    }

    // Returns the available dish at a given position, or null if pos is out of range
    public static Dish getDishAtPosition(int pos) {

        if (singleton == null || pos < 0 || pos >= singleton.availableDishes.size() )
            return null;

        return singleton.availableDishes.get(pos);
    }

    // Returns the table at a given position, or null if pos is out of range
    public static Table getTableAtPosition(int pos) {

        if (singleton == null || pos < 0 || pos >= singleton.tables.size() )
            return null;

        return singleton.tables.get(pos);
    }

    // Assigns to a dish the allergens corresponding to a given array of ids
    // (if some taskId doesn't correspond to any available allergen, it is ignored)
    public static void setDishAllergens(Dish dish, ArrayList<Integer> allergenIds) {

        for (int id: allergenIds) {

            Allergen allergen = getAllergenById(id);

            if (allergen != null)
                dish.addAllergen(allergen);
            else
                Log.d("RestaurantManager", "WARNING: no available allergen with id: " + id);
        }
    }

    // Gets the final price for all the orders of a given table (including taxes)
    public static BigDecimal finalPriceForTable(Table table) {

        if (singleton == null)
            return null;

        BigDecimal base = table.priceBeforeTax();
        BigDecimal taxes = base.multiply(singleton.mTaxRate);

        return base.add(taxes);
    }


    // Methods for debugging only:

    // Returns a string with the significant data of this restaurant manager (for debugging)
    public static String contentToString() {

        if (singleton == null)
            return "<The singleton RestaurantManager was not instantiated yet>";

        String res = "Date: " + getMenuDate();
        res += "\nCurrency: " + getCurrency();
        res += "\nTax rate: " + getTaxRate();
        res += "\nTables: " + tableCount();
        res += "\nTotal Allergens: " + allergenCount();
        res += "\nTotal Dishes: " + dishCount();

        for (int i=0; i<dishCount(); i++) {
            res += "\n" + getDishAtPosition(i);
        }

        return res;
    }

    // Generates test data
    public static void generateRandomOrders(Context context) {

        if (singleton == null)
            return;

        Log.d("RestaurantManager","\nGenerating random test data...\n");

        if (singleton.tables.size() < 1 || singleton.availableDishes.size() < 1 )
            return;

        ArrayList<String> specialRequests = new ArrayList<String>();

        specialRequests.add("");
        specialRequests.add( context.getString(R.string.txt_request_01) );
        specialRequests.add( context.getString(R.string.txt_request_02) );
        specialRequests.add( context.getString(R.string.txt_request_03) );
        specialRequests.add( context.getString(R.string.txt_request_04) );
        specialRequests.add( context.getString(R.string.txt_request_05) );
        specialRequests.add( context.getString(R.string.txt_request_06) );
        specialRequests.add( context.getString(R.string.txt_request_07) );
        specialRequests.add( context.getString(R.string.txt_request_08) );

        // How many tables will have some order
        int tablesOrdering = Utils.randomInt(1, singleton.tables.size() );

        for (int i=0; i<tablesOrdering-1; i++) {

            Table table = singleton.tables.get(i);

            // How many orders will have this table
            int orderCount = Utils.randomInt(1, singleton.availableDishes.size() );

            for (int j=1; j<orderCount; j++) {

                // Dish to order
                int dishIndex = Utils.randomInt(1, singleton.availableDishes.size() ) - 1;
                int noteIndex = Utils.randomInt(1, specialRequests.size()) - 1;

                Order newOrder = new Order(singleton.availableDishes.get(dishIndex), specialRequests.get(noteIndex));
                table.addOrder(newOrder);
            }
        }
    }
}
