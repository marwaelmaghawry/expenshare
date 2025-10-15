package xpenshare;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import org.h2.tools.Server;
import jakarta.inject.Singleton;

import java.sql.SQLException;

@Singleton
public class H2ConsoleServer {

    @EventListener
    public void onStartup(StartupEvent event) {
        try {
            Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
            System.out.println("H2 console started at http://localhost:8082");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
