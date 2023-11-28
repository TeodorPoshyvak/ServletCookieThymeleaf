package org.example;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DateTimeException;
import java.util.*;

@WebFilter("/time")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
                if (Objects.isNull(req.getPathInfo()) || req.getPathInfo().equals("/")) {
                    chain.doFilter(req, res);
                }
        } catch (DateTimeException e) {
            res.setStatus(400);
            res.setContentType("text/html");
            res.getWriter().write("<html><h3> ERROR: ${ERROR} </h3><body>".replace("${ERROR}", "Invalid timezone"));
            res.getWriter().close();
        }

    }
}