package test.reactive.live.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import test.reactive.ReactiveApplication;

@SpringBootApplication
public class RemoteService {

    @RestController
    public static class MyController {
        @GetMapping("/service")
        public String service(String req){
            System.out.println(req);
            return req + "/service";
        }
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8082");
        System.setProperty("server.tomcat.max-threads", "1000");
        SpringApplication.run(RemoteService.class, args);
    }
}
