package com.hero.zcat;

import com.hero.servlet.HeroRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeroRequestImpl implements HeroRequest {
    private final HttpRequest delegate;

    private final String path;

    private final Map<String, List<String>> parameters;

    public HeroRequestImpl(HttpRequest delegate) {
        QueryStringDecoder decoder = new QueryStringDecoder(delegate.uri());
        this.delegate = delegate;
        path = decoder.path();
        parameters = decoder.parameters();
    }

    @Override
    public String getUri() {
        return delegate.uri();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getMethod() {
        return delegate.method().name();
    }

    @Override
    public Map<String, List<String>> getParameters() {
        HashMap<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    @Override
    public List<String> getParameters(String name) {
        return new ArrayList<>(parameters.get(name));
    }

    @Override
    public String getParameter(String name) {
        List<String> parametersOfName = parameters.get(name);
        if (parametersOfName == null || parametersOfName.isEmpty()) {
            return null;
        }
        return parametersOfName.get(0);
    }
}
