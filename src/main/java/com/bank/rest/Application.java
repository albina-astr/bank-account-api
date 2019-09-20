package com.bank.rest;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.core.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class Application {

    public static void main(String[] args) throws IOException {
        HttpServer server = createHttpServer();
        server.start();
        System.out.println("Server started on " + getURI());
        System.in.read();
        server.stop(1);
    }

    private static HttpServer createHttpServer() throws IOException {
        ResourceConfig resourceConfig = new PackagesResourceConfig("com.bank.rest");
        resourceConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,Boolean.TRUE);

        return HttpServerFactory.create(getURI(), resourceConfig);
    }

    private static URI getURI() {
        return UriBuilder.fromUri("http://" + getHostName() + "/").port(8085).build();
    }

    private static String getHostName() {
        String hostName = "localhost";
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }

}