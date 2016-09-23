/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wondercode
 */
public class Scheduler extends Thread {

    private boolean explode = false;
    private List<Integer> clients;
    private List<Integer> pxs;
    private List<Integer> distance;
    private List<Integer> targets;
    private int explosion;

    @Override
    public void run() {
        clients = new ArrayList<Integer>();
        pxs = new ArrayList<Integer>();
        distance = new ArrayList<Integer>();
        targets = manager.getTargets();
        clients.add(0);
        pxs.add(-150);
        int amount = manager.getCities().size();
        Random random = new Random();
        explosion = random.nextInt(amount + 1);
        int i = 0;
        for (i = 0; i < amount; i++) {
            distance.add(targets.get(i) + 1);
            if (targets.get(i) >= explosion) {
                distance.set(i, -1);
            }
        }
        System.out.println("Explosion at " + explosion);
        if (explosion == 0) {
            explode = true;
        }
        while (!explode) {
            try {
                for (i = 0; i < pxs.size(); i++) {
                    pxs.set(i, pxs.get(i) + 10);
                }
                manager.sendMessage(pxs, clients);
                Thread.sleep(50);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Explode");
        try {
            manager.fin(distance);
        } catch (IOException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isExplode() {
        return explode;
    }

    public void setExplode(boolean explode) {
        this.explode = explode;
    }

    public List<Integer> getClients() {
        return clients;
    }

    public void setClients(List<Integer> clients) {
        this.clients = clients;
    }

    public void addClient() {
        if (clients.size() < 2) {
            clients.add(clients.get(0) + 1);
            if(clients.get(1)==explosion){
                clients.remove(1);
            }else{
                pxs.add(-150);
            }            
        }
    }

    public void removeClient() {
        clients.remove(0);
        pxs.remove(0);
        if (clients.isEmpty()||clients.get(0) == explosion) {
            explode = true;
        }
    }

    public List<Integer> getPxs() {
        return pxs;
    }

    public void setPxs(List<Integer> pxs) {
        this.pxs = pxs;
    }

    public int getExplosion() {
        return explosion;
    }

    public void setExplosion(int explosion) {
        this.explosion = explosion;
    }

}
