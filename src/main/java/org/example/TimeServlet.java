package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


@WebServlet("/time/*")
public class TimeServlet extends HttpServlet {

    private TemplateEngine engine;
    TimeUTC timeUTC;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();
        timeUTC = new TimeUTC();
        JavaxServletWebApplication jswa = JavaxServletWebApplication.buildApplication(this.getServletContext());
        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(jswa);
        resolver.setPrefix("/WEB-INF/template/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        String cookieTime = "";
        Cookie[] cookies = req.getCookies();
        for (Cookie cookie : cookies) {
            if ("lastTimezone".equals(cookie.getName())) {
                if (cookie.getValue().contains("UTC+") || cookie.getValue().contains("UTC-")) {
                    Integer num = Integer.valueOf(cookie.getValue().substring(4));
                    if (cookie.getValue().length() <= 6 && num <= 18) {
                        cookieTime = cookie.getValue();
                        break;
                    }
                }
            }
        }

        String baseTimeUTC = ZonedDateTime.now(ZoneId.of(("UTC")))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        if (!cookieTime.isEmpty()) {
            baseTimeUTC = ZonedDateTime.now(ZoneId.of((cookieTime)))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        }

        Context context = new Context(req.getLocale(), Map.of("BaseTime", baseTimeUTC,
                "timezone", timeUTC,
                "lastTimezone", cookieTime));

        engine.process("UTCtime", context, resp.getWriter());
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String param = req.getParameter("timezone").replace("%2B", "+");
        if (param != null && !param.trim().isEmpty()) {
            Cookie cookie = new Cookie("lastTimezone", param.replace("%2B", "+"));
            cookie.setMaxAge(5);
            resp.addCookie(cookie);
            timeUTC.setTime(ZonedDateTime.now(ZoneId.of(param))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        }
        resp.sendRedirect("/Servlet/time");
    }
}
