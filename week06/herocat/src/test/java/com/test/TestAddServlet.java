package com.test;

import com.hero.servlet.HeroRequest;
import com.hero.servlet.HeroResponse;
import com.hero.servlet.HeroServlet;

public class TestAddServlet extends HeroServlet {
    @Override
    public void doGet(HeroRequest request, HeroResponse response) throws Exception {
        int a = Integer.parseInt(request.getParameter("a"));
        int b = Integer.parseInt(request.getParameter("b"));
        response.write(String.valueOf(a + b));
    }

    @Override
    public void doPost(HeroRequest request, HeroResponse response) throws Exception {
        // do nothing
    }
}
