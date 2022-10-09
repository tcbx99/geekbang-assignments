package com.test;

import com.hero.servlet.HeroRequest;
import com.hero.servlet.HeroResponse;
import com.hero.servlet.HeroServlet;

public class TestUserServlet extends HeroServlet {
    @Override
    public void doGet(HeroRequest request, HeroResponse response) throws Exception {
        String who = request.getParameter("user");
        if (who == null) {
            who = "Anonymous";
        }
        response.write("Hello " + who);
    }

    @Override
    public void doPost(HeroRequest request, HeroResponse response) throws Exception {
        // do nothing
    }
}
