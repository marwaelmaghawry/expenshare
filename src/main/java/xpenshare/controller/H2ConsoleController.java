package xpenshare.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/h2-console")
public class H2ConsoleController {

    @Get("/")
    public String redirect() {
        return "<html><body>" +
                "<iframe src=\"/h2-console/index.html\" width=\"100%\" height=\"1000px\"></iframe>" +
                "</body></html>";
    }
}
