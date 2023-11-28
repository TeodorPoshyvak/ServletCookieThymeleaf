package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;


@WebServlet("/time/*")
public class TimeServlet extends HttpServlet {

    private TemplateEngine engine;
    String zonedDateTime;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();
        zonedDateTime = "";
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
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    if (Pattern.compile("UTC\\+\\d+").matcher(cookie.getValue()).find() || Pattern.compile("UTC\\-\\d+").matcher(cookie.getValue()).find()) {
                        Integer num = Integer.valueOf(cookie.getValue().substring(4));
                        if (cookie.getValue().length() <= 6 && num <= 18) {
                            cookieTime = cookie.getValue();
                            break;
                        }
                    }
                }
            }
        }

        String baseTimeUTC = ZonedDateTime.now(ZoneId.of(("UTC")))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        if (cookieTime != null && !cookieTime.isEmpty()) {
            baseTimeUTC = ZonedDateTime.now(ZoneId.of((cookieTime)))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        }

        Context context = new Context(req.getLocale(), Map.of("BaseTime", baseTimeUTC,
                "timezone", zonedDateTime,
                "lastTimezone", cookieTime));

        engine.process("UTCtime", context, resp.getWriter());
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String param = req.getParameter("timezone");
        param = (param != null) ? param.replace("%2B", "+").replace(" ", "") : "";
        if (param != null && !param.trim().isEmpty()) {
            Cookie cookie = new Cookie("lastTimezone", param.replace("%2B", "+"));
            cookie.setMaxAge(5);
            resp.addCookie(cookie);
            zonedDateTime = ZonedDateTime.now(ZoneId.of(param))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        }
        resp.sendRedirect("/Servlet/time");
    }
}
