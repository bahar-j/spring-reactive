package com.example.demo2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@SpringBootApplication
@RestController
@Slf4j
@EnableAsync
public class Demo2Application {

	@Autowired
	MyService myService;

	@GetMapping("/")
	Mono<String> hello(){
		log.info("pos1");
		Mono m = Mono.just(generateHello()).log();

		// 밑의 두 줄은 같은 코드
//		Mono m = Mono.fromSupplier(() -> generateHello()).log();
//		Mono<String> m = Mono.fromSupplier(new Supplier<String>() {
//			@Override
//			public String get() {
//				return generateHello();
//			}
//		}).log();
//		m.subscribe(); -> 스프링 없이도 subscribe 가능

		// m.block() 예제대로 하면 해당 쓰레드(netty의 nio workThread)에선 block할 수 없다는 에러가 뜸
		try {
			log.info(myService.work(m).get().toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		log.info("pos2");
		return m;
	}

	private String generateHello(){
		log.info("method generateHello()");
		return "Hello Mono";
	}

	@Service
	public static class MyService {
		@Async
		public CompletableFuture<String> work(Mono<String> m){
			return CompletableFuture.completedFuture(m.block());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Demo2Application.class, args);
	}

}
