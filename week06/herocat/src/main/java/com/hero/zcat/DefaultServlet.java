package com.hero.zcat;

import com.hero.servlet.HeroRequest;
import com.hero.servlet.HeroResponse;
import com.hero.servlet.HeroServlet;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class DefaultServlet extends HeroServlet {
    @Override
    public void doGet(HeroRequest request, HeroResponse response) throws Exception {
        String uri = request.getUri();
        String path = request.getPath();
        path = path.replaceFirst("/", "");
        // Here we read resources from jars
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(path);
        if (resourceStream == null) {
            response.write("404 - no this servlet : " + (uri.contains("?") ? uri.substring(0, uri.lastIndexOf("?")) : uri));
            return;
        }
        try {
            StringWriter sw = new StringWriter();
            IOUtils.copy(resourceStream, sw, StandardCharsets.UTF_8);
            String data = sw.toString();
            response.write(data);
        } finally {
            resourceStream.close();
        }
    }


    @Override
    public void doPost(HeroRequest request, HeroResponse response) throws Exception {
        String uri = request.getUri();
        response.write("404 - no this servlet : " + (uri.contains("?") ? uri.substring(0, uri.lastIndexOf("?")) : uri));
    }
}
