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
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


@WebServlet("/time/*")
public class TimeServlet extends HttpServlet {

    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();
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

        String baseTimeUTC = "";
        if (Objects.isNull(req.getPathInfo()) || req.getPathInfo().equals("/")) {
            if (cookieTime != null && !cookieTime.isEmpty()) {
                baseTimeUTC = ZonedDateTime.now(ZoneId.of((cookieTime)))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
            }else {
                baseTimeUTC = ZonedDateTime.now(ZoneId.of(("UTC")))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
            }

        }

        String parameter = req.getParameter("timezone");
        parameter = parameter != null ?  URLEncoder.encode(parameter, "UTF-8").toUpperCase().replaceAll("%2B", "+").replaceAll("%2D", "-") : "";
        Cookie lastTimezone = new Cookie("lastTimezone", parameter);
        resp.addCookie(lastTimezone);


        String parameterTimeZone = "";
        if(!parameter.isEmpty()) {
             parameterTimeZone = ZonedDateTime
                    .now(ZoneId.of(parameter))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        }

        Context context = new Context(req.getLocale(), Map.of("BaseTime", baseTimeUTC,
                "timezone", parameterTimeZone));
        engine.process("UTCtime", context, resp.getWriter());
        resp.getWriter().close();
    }
}
