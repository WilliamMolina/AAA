/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.city;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author wdario.molina
 */
public class CityFactory {

    private static List<City> cities;
    private static boolean[] available;
    private static int cAvailable;

    public static void fillCities() {
        cities = new ArrayList<City>();
        cities.add(new City("Toronto", "http://www.rentseeker.ca/blog/wp-content/uploads/2016/01/Moving-to-Toronto-Tips-RentSeeker.ca_.jpg"));
        cities.add(new City("Sidney", "http://previews.123rf.com/images/gropgrop/gropgrop1506/gropgrop150600269/41643283-Sydney-Australia-city-skyline-vector-background-Flat-trendy-illustration-Stock-Vector.jpg"));
        cities.add(new City("New York", "http://previews.123rf.com/images/gropgrop/gropgrop1506/gropgrop150600268/41643282-New-York-city-architecture-vector-illustration-skyline-city-silhouette-skyscraper-flat-design-Stock-Vector.jpg"));
        cities.add(new City("Dubai","http://image.shutterstock.com/z/stock-vector-dubai-city-skyline-detailed-silhouette-flat-design-trendy-vector-illustration-213762358.jpg"));
        available = new boolean[cities.size()];
        for (int i = 0; i < available.length; i++) {
            available[i] = true;
        }
        cAvailable = cities.size();
    }

    public static City generateCity() {
        if (cAvailable > 0) {
            Random r = new Random();
            int i;
            do {
                i = r.nextInt(cities.size());
            } while (!available[i]);
            available[i] = false;
            cAvailable--;
            return cities.get(i);
        }
        return null;
    }

    public static void freeCity(City c) {
        int i = -1;
        int j = 0;
        for (City ci : cities) {
            if (ci.getName().equals(c.getName())) {
                i = j;
                break;
            }
            j++;
        }
        if (i != -1) {
            available[i] = true;
            cAvailable++;
        }
    }
}
