package com.auroratms.server;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

// Spring Boot 2.0 way redirect http requests to https
@Component
public class ContainerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /**
     * Configure redirecting from http
     * @param factory
     */
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setRedirectPort(8443);
        connector.setSecure(false);
        factory.addAdditionalTomcatConnectors(connector);
    }
}
