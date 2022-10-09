package com.test;

import com.hero.zcat.server.ServerBuilder;

public class ServletTest {
    public static void main(String[] args) {
        try {
            new ServerBuilder()
                    .addServlet("/user", new TestUserServlet())
                    .addServlet("/add", new TestAddServlet())
                    .build()
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
