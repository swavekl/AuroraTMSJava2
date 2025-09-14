package com.auroratms.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filter which redirects all requests for deep linked UI resources to index.html so browser can load the SPA resources
 * and Angular can take over routing.
 * All api resources and static resources should go through normal handling
 */
@Configuration
@Component
public class ForwardServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest myRequest = (HttpServletRequest) request;
        String servletPath = myRequest.getServletPath();
        if (!servletPath.startsWith("/ui")) {
            chain.doFilter(myRequest, response);
        } else {
            System.out.println("redirecting to index.html a servletPath = " + servletPath);
            RequestDispatcher dispatcher = myRequest.getServletContext()
                    .getRequestDispatcher("/index.html");
            dispatcher.forward(myRequest, response);
        }
    }
}
