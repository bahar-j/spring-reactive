package com.example.demo;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@SpringBootApplication
public class DemoApplication {

	static final String URL1 = "http://localhost:8082/service?req={req}";
	static final String URL2 = "http://localhost:8082/service2?req={req}";

	AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

	@Autowired
	MyService myService;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GetMapping("/rest")
	public DeferredResult<String> rest(int idx){
		DeferredResult<String> dr = new DeferredResult<>();

		// getForEntity: 응답코드, 헤더, 바디 다 받아옴(http response 통째로)
		toCF(rt.getForEntity(URL1, String.class, "h" + idx))
				// s(response 전체)에서 바디만 가져와서 다시 api 호출
				.thenCompose(s -> {
					return toCF(rt.getForEntity(URL2, String.class, s.getBody()));
				})
				// Async 붙이면 새로운 쓰레드에 태워서 비동기 방식으로 동작하고 기존 쓰레드는 return
				.thenApplyAsync(s2 -> myService.work(s2.getBody()))
				.thenAccept(s3 -> dr.setResult(s3))
				.exceptionally(e -> {
					dr.setErrorResult(e.getMessage());
					// object 타입으로 리턴하기 위해 (Void) 붙임
					return (Void) null;
				});
		return dr;
	}

	<T> CompletableFuture<T> toCF(ListenableFuture<T> lf){
		CompletableFuture<T> cf = new CompletableFuture<T>();
		lf.addCallback(s -> cf.complete(s), e -> cf.completeExceptionally(e));
		return cf;
	}

	@Service
	public static class MyService {
		public String work(String req){
			return req + "/asyncwork";
		}
	}

}
