package com.hero;

import com.hero.zcat.server.ServerBuilder;

public class ZCatApplication {
    public static void main(String[] args) {
        try {
            new ServerBuilder().build().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
