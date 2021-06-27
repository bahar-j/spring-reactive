package com.example.demo3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RestController
@Slf4j
public class Demo3Application {

	@GetMapping("/event/{id}")
	Mono<List<Event>> event(@PathVariable long id){
		List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
		return Mono.just(list);
	}

	@GetMapping("/events")
	Flux<Event> events(){
		return Flux.just(new Event(1L, "event1"), new Event(2L, "event2")).log();
	}

	@GetMapping(value = "/eventStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Event> eventStream(){
		List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
		return Flux.fromIterable(list);
	}

	public static void main(String[] args) {
		SpringApplication.run(Demo3Application.class, args);
	}

	@Data
	@AllArgsConstructor
	public static class Event {
		long id;
		String value;
	}
}
