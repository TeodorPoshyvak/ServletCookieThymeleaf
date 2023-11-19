package org.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter printWriter = resp.getWriter();
        printWriter.write("<html><body>");


        synchronized (this) {
            if (Objects.isNull(req.getPathInfo()) || req.getPathInfo().equals("/")) {
                printWriter.write("<p> TimeZone: ${dateAndTime} </p>"
                        .replace("${dateAndTime}", ZonedDateTime
                                .now(ZoneId.of("UTC+2"))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))));
            }

            String param = URLEncoder.encode(req.getParameter("timezone"), "UTF-8");
            if (!Objects.isNull(param)) {
                printWriter.write(" <p>TimeZone: ${UTC} </p>"
                        .replace("${UTC}", ZonedDateTime
                                .now(ZoneId.of(param))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))));
            }
        }

        printWriter.write("</body></html>");
        printWriter.close();
    }
}
