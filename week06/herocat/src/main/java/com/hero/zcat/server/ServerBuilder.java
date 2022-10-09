package com.hero.zcat.server;

import com.hero.servlet.HeroServlet;

import java.util.HashMap;

public class ServerBuilder {

    private final HashMap<String, HeroServlet> servlets = new HashMap<>();

    public ServerBuilder addServlet(String path, HeroServlet servlet) {
        servlets.put(path, servlet);
        return this;
    }

    public ZCatServer build() {
        return new ZCatServer(servlets);
    }
}
