/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.ws;

import com.google.gson.Gson;
import com.wondercode.city.City;
import com.wondercode.city.CityFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author wdario.molina
 */
@ServerEndpoint("/subscribe")
public class manager {

    private static List<Session> planes = Collections.synchronizedList(new ArrayList<Session>());
    private static List<Session> waitingList = Collections.synchronizedList(new ArrayList<Session>());
    private static List<City> cities = Collections.synchronizedList(new ArrayList<City>());
    private static List<City> waitingCities = Collections.synchronizedList(new ArrayList<City>());
    private static List<Integer> ready = Collections.synchronizedList(new ArrayList<Integer>());
    private static List<Integer> targets = Collections.synchronizedList(new ArrayList<Integer>());
    private static List<Integer> points = Collections.synchronizedList(new ArrayList<Integer>());
    private static boolean playing = false;
    private static Gson gson = new Gson();
    private static Scheduler scheduler;

    @OnOpen
    public void onOpen(Session peer) {

        //Add user to list
        if (planes.isEmpty()) {
            //If is the first user, fill the cities
            CityFactory.fillCities();
        }
        //Generate a city, then assign it to the new user
        City c = CityFactory.generateCity();
        if (c != null) {
            if (playing) {
                //Add user to waiting list
                waitingList.add(peer);
                waitingCities.add(c);
                try {
                    peer.getBasicRemote().sendText(gson.toJson(new Message("Currently playing. You must wait."), Message.class));
                } catch (IOException ex) {
                    Logger.getLogger(manager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //Add user
                planes.add(peer);
                ready.add(0);
                cities.add(c);
                targets.add(-1);
                points.add(0);
                try {
                    AssignCity ac=new AssignCity();
                    ac.setCity(c);
                    ac.setType("assignCity");
                    ac.setMessage("Estàs en " + cities.get(cities.size() - 1).getName());
                    peer.getBasicRemote().sendText(gson.toJson(ac));                    
                    CityResult cr = new CityResult();
                    List<City> cits = new ArrayList<City>();
                    int i = 0;
                    for (City city : cities) {
                        if (!city.equals(c)) {
                            cits.add(city);
                            planes.get(i).getBasicRemote().sendText(gson.toJson(new Message(c.getName(), "newCity"), Message.class));
                        }
                        i++;
                    }
                    cr.setCities(cits);
                    cr.setMessage("cities");
                    cr.setType("list");
                    peer.getBasicRemote().sendText(gson.toJson(cr, CityResult.class));
                } catch (IOException ex) {

                }
            }

        } else {
            try {
                peer.getBasicRemote().sendText(gson.toJson(new Message("En este momento no tenemos instalaciones en su ciudad"), Message.class));
            } catch (IOException ex) {

            }
        }

    }

    @OnMessage
    public void onMessage(String message, Session client) throws IOException, EncodeException {
        //Get index of user
        Message m = gson.fromJson(message, Message.class);
        int index = planes.indexOf(client);
        switch (m.getType()) {
            case "play":
                if (!playing && index != -1) {
                    if (ready.get(index) != 1) {
                        ready.set(index, 1);
                        for (Session plane : planes) {
                            if (!plane.equals(client)) {
                                plane.getBasicRemote().sendText(gson.toJson(new Message("User :" + (index + 1) + " is ready to play."), Message.class));
                            }
                        }
                    }
                }
                if (!playing && readyCount() == ready.size()) {
                    if (readyCount() > 1) {
                        scheduler = new Scheduler();
                        scheduler.start();
                        playing = true;
                        for (Session plane : planes) {
                            plane.getBasicRemote().sendText(gson.toJson(new Message("Let's play."), Message.class));
                        }
                    } else if (readyCount() == 1) {
                        planes.get(0).getBasicRemote().sendText(gson.toJson(new Message("You're alone.\n Wait for new users."), Message.class));
                    }
                }
                if (playing && (readyCount() == 0 || readyCount() == 1)) {
                    playing = false;
                    if (scheduler != null) {
                        scheduler.stop();
                    }
                    restart();
                }
                break;
            case "target":
                if (!playing && index != -1) {
                    int i = 0;
                    for (City city : cities) {
                        if (city.getName().equals(m.getMessage())) {
                            targets.set(index, i);
                            client.getBasicRemote().sendText(gson.toJson(new Message("Your target has been assigned correctly", "assigned")));
                        }
                        i++;
                    }
                }
                break;
            case "clearTarget":
                if (targets.get(index) != -1) {
                    targets.set(index, -1);
                    ready.set(index, 0);
                }
                break;
            case "delegate":
                if (scheduler != null) {
                    scheduler.addClient();
                }
                break;
            case "reuse":
                if (scheduler != null) {
                    scheduler.removeClient();
                }
                break;
        }
    }

    @OnClose
    public void onClose(Session peer) throws EncodeException, IOException {
        int i = planes.indexOf(peer);
        planes.remove(peer);
        ready.remove(i);
        points.remove(i);
        for (Session plane : planes) {
            Message m = new Message(cities.get(i).getName(), "drop");
            plane.getBasicRemote().sendText(gson.toJson(m, Message.class));
        }
        if (!playing) {
            targets.remove(i);
        }
        CityFactory.freeCity(cities.remove(i));
        if (!playing && readyCount() == ready.size()) {
            if (readyCount() > 1) {
                playing = true;
                for (Session plane : planes) {
                    plane.getBasicRemote().sendText(gson.toJson(new Message("Let's play."), Message.class));
                }
            } else if (readyCount() == 1) {
                planes.get(0).getBasicRemote().sendText(gson.toJson(new Message("You're alone.\n Wait for new users."), Message.class));
            }
        }
        if (playing && (readyCount() == 0 || readyCount() == 1)) {
            playing = false;
            if (scheduler != null) {
                scheduler.stop();
            }
            restart();
        }
    }
    static void fin(List<Integer> distance) throws IOException{
        int mayor=0;
        int countMayor=0;
        int indexMayor=-1;
        int j=0;
        for(Integer i:distance){
            if(i>mayor){
                mayor=i;
                countMayor=1;
                indexMayor=j;
            }else if(mayor==i){
                countMayor++;
            }
            j++;
        }
        if(indexMayor!=-1&&countMayor==1){
            //There is a winner
            System.out.println("Winner is "+(indexMayor+1));
            points.set(indexMayor,points.get(indexMayor)+1);
            int l=0;
            for(Session plane:planes){
                if(l==indexMayor){
                    plane.getBasicRemote().sendText(gson.toJson(new Message("You win!","winner")));
                }else{
                    plane.getBasicRemote().sendText(gson.toJson(new Message("User "+(indexMayor+1)+" won","loser")));
                }
                l++;
            }
        }
        planes.addAll(waitingList);
        cities.addAll(waitingCities);
        int l = waitingList.size();
        for (int i = 0; i < l; i++) {
            ready.add(0);
            targets.add(-1);
            points.add(0);
        }
        int i = 0;
        PointMessage pm=new PointMessage();
        pm.setPoints(points);
        pm.setType("points");
        pm.setMessage("List of scores");
        for (Session plane : planes) {
            CityResult cr = new CityResult();
            List<City> cits = new ArrayList<City>();
            j = 0;
            for (City city : cities) {
                if (i != j) {
                    cits.add(city);
                }
                j++;
            }
            cr.setCities(cits);
            cr.setMessage("cities");
            cr.setType("list");
            plane.getBasicRemote().sendText(gson.toJson(cr, CityResult.class));
            plane.getBasicRemote().sendText(gson.toJson(pm));
            i++;
        }
        l = ready.size();
        for (i = 0; i < l; i++) {
            ready.set(i, 0);
        }
        waitingList = Collections.synchronizedList(new ArrayList<Session>());
        waitingCities = Collections.synchronizedList(new ArrayList<City>());
        
    }

    private void restart() throws IOException {
        planes.addAll(waitingList);
        cities.addAll(waitingCities);
        int l = waitingList.size();
        for (int i = 0; i < l; i++) {
            ready.add(0);
            targets.add(-1);
        }
        int i = 0, j;
        for (Session plane : planes) {
            CityResult cr = new CityResult();
            List<City> cits = new ArrayList<City>();
            j = 0;
            for (City city : cities) {
                if (i != j) {
                    cits.add(city);
                }
                j++;
            }
            cr.setCities(cits);
            cr.setMessage("cities");
            cr.setType("list");
            plane.getBasicRemote().sendText(gson.toJson(cr, CityResult.class));
            i++;
        }
        l = ready.size();
        for (i = 0; i < l; i++) {
            ready.set(i, 0);
        }
        waitingList = Collections.synchronizedList(new ArrayList<Session>());
        waitingCities = Collections.synchronizedList(new ArrayList<City>());
    }

    private int readyCount() {
        int c = 0;
        for (Integer i : ready) {
            if (i == 1) {
                c++;
            }
        }
        return c;
    }

    public static List<Session> getWaitingList() {
        return waitingList;
    }

    public static void setWaitingList(List<Session> waitingList) {
        manager.waitingList = waitingList;
    }

    public static List<City> getWaitingCities() {
        return waitingCities;
    }

    public static void sendMessage(List<Integer> pxs, List<Integer> clients) throws IOException {
        int j = 0;
        for (Integer i : clients) {
            planes.get(i).getBasicRemote().sendText(gson.toJson(new Message("" + pxs.get(j), "move")));
            j++;
        }
    }

    public static void setWaitingCities(List<City> waitingCities) {
        manager.waitingCities = waitingCities;
    }

    public static List<Integer> getTargets() {
        return targets;
    }

    public static void setTargets(List<Integer> targets) {
        manager.targets = targets;
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static void setPlaying(boolean playing) {
        manager.playing = playing;
    }

    public static Gson getGson() {
        return gson;
    }

    public static void setGson(Gson gson) {
        manager.gson = gson;
    }

    public static List<Session> getPlanes() {
        return planes;
    }

    public static void setPlanes(List<Session> planes) {
        manager.planes = planes;
    }

    public static List<City> getCities() {
        return cities;
    }

    public static void setCities(List<City> cities) {
        manager.cities = cities;
    }

    public static List<Integer> getReady() {
        return ready;
    }

    public static void setReady(List<Integer> ready) {
        manager.ready = ready;
    }

}
