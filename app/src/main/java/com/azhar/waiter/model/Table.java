package com.azhar.waiter.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


// This class represents a table of the restaurant, with all its orders.
// Implements the Serializable interface so that it can be passed inside a bundle object.
// ----------------------------------------------------------------------------

public class Table implements Serializable {

    // Object attributes
    private String mName;
    private ArrayList<Order> mOrders;


    // Class constructor
    // (creates a new table with name and with no orders)
    public Table(String name) {

        mName = name;
        mOrders = new ArrayList<Order>();
    }


    // Class getters:

    public String getName() {
        return mName;
    }

    public ArrayList<Order> getOrders() {
        return mOrders;
    }

    // Other methods:

    // Returns the count of orders
    public int orderCount() {
        return mOrders.size();
    }

    // Returns the order at a given position, or null if pos is out of range
    public Order getOrderAtPosition(int pos) {

        if (pos < 0 || pos >= mOrders.size() )
            return null;

        return mOrders.get(pos);
    }

    // Adds a new order to the table
    public boolean addOrder(Order newOrder) {
        return mOrders.add(newOrder);
    }

    // Removes an order from the table
    public boolean removeOrder(Order order) {
        return mOrders.remove(order);
    }

    // Resets the order list (returns a copy to the old list)
    public ArrayList<Order> resetTable() {

        ArrayList<Order> oldList = new ArrayList<Order>();
        for (Order o: mOrders)
            oldList.add(o);

        mOrders.clear();
        return oldList;
    }

    // Assigns the given list to the table's order list (the existing list is cleared and removed)
    // Returns false if the given list is null, otherwise returns true
    public boolean restoreTable(ArrayList<Order> oldList) {

        if (oldList==null)
            return false;

        mOrders.clear();
        for (Order o: oldList)
            mOrders.add(o);

        return true;
    }

    // Gets the price for all the table orders (before taxes)
    public BigDecimal priceBeforeTax() {

        BigDecimal total = new BigDecimal(0);

        for (Order order: mOrders)
            total = total.add( order.price() );

        return total;
    }

    // Returns a HashMap<Dish,Integer> where the keys are all the dishes ordered in the table,
    // and the values are the number of occurrences of each dish.
    public HashMap<Dish,Integer> getOrdersMap() {

        HashMap<Dish,Integer> hm = new HashMap();

        for (Order order: mOrders) {

            Dish dish = order.getDish();

            Integer count = hm.get(dish);
            hm.put(dish, count == null ? 1 : count + 1);
        }

        return hm;
    }
}
