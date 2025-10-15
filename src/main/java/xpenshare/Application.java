package xpenshare;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;
import jakarta.inject.Inject;

@OpenAPIDefinition(
        info = @Info(
                title = "ExpenShare",
                version = "0.0"
        )
)
public class Application {

    @Inject
    static H2ConsoleServer h2ConsoleServer;  // force bean creation

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
