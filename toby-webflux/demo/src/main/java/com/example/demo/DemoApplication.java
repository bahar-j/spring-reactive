package com.example.demo;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@SpringBootApplication
public class DemoApplication {

	static final String URL1 = "http://localhost:8082/service?req={req}";
	static final String URL2 = "http://localhost:8082/service2?req={req}";

	// 같은 프로젝트에서 netty랑 톰캣 같이 띄우는게 안됨
//	@Bean
//	NettyReactiveWebServerFactory nettyReactiveWebServerFactory(){
//		return new NettyReactiveWebServerFactory();
//	}

	@Autowired
	MyService myService;

	//AsyncRestTemplate과 유사
	WebClient client = WebClient.create();

	@GetMapping("/rest")
	public Mono<String> rest(int idx){
//		Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange();
//		Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
//		return body;
		return client.get().uri(URL1, idx).exchange() // Mono<ClientResponse>
				.flatMap(clientResponse -> clientResponse.bodyToMono(String.class)) // clientResponse -> Mono<String>
				.flatMap(res1 -> client.get().uri(URL2, res1).exchange()) // Stirng -> Mono<ClientResponse>
				.flatMap(c -> c.bodyToMono(String.class)) // clientResponse -> Mono<String>
				.map(res2 -> myService.work(res2)); // Mono<String> -> Mono<String>
	}

	public static void main(String[] args) {
		System.setProperty("reactor.ipc.netty.workerCount", "2");
		System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
		SpringApplication.run(DemoApplication.class, args);
	}

	@Service
	public static class MyService {
		public String work(String req){
			return req + "/asyncwork";
		}
	}

}
