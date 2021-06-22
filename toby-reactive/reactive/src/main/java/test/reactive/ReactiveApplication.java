package test.reactive;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

@SpringBootApplication
@Slf4j
@EnableAsync
public class ReactiveApplication {
//	@RestController
//	public static class Controller {
//		@RequestMapping("/hello")
//		public Publisher<String> hello(String name){
//			return new Publisher<String>() {
//				@Override
//				public void subscribe(Subscriber<? super String> s) {
//					s.onSubscribe(new Subscription() {
//						@Override
//						public void request(long n) {
//							s.onNext("Hello " + name);
//							s.onComplete();
//						}
//
//						@Override
//						public void cancel() {
//
//						}
//					});
//				}
//			};
//		}
//	}

	@RestController
	public static class MyController {
//		@Autowired MyService myService;
		Queue<DeferredResult> results = new ConcurrentLinkedQueue<>();

//		@GetMapping("/callable")
//		public Callable<String> async() throws InterruptedException {
//			log.info("callable");
//			return () -> {
//				log.info("async");
//				Thread.sleep(2000);
//				return "hello";
//			};
//		};
//		public String callable() throws InterruptedException {
//			log.info("async");
//			Thread.sleep(2000);
//			return "hello";
//		}
		@GetMapping("/dr")
		public DeferredResult<String> callable() throws InterruptedException {
			log.info("defferedResult");
			DeferredResult<String> dr = new DeferredResult<>(600000L);
			results.add(dr);
			return dr;
		};

		@GetMapping("/dr/count")
		public String drcount() throws InterruptedException {
			return String.valueOf(results.size());
		};

		@GetMapping("/dr/event")
		public String drevent(String msg) throws InterruptedException {
			for(DeferredResult<String> dr : results){
				dr.setResult("Hello " + msg);
				results.remove(dr);
			}
			return "OK";
		};
	}

	@Component
	public static class MyService {
		// 한 개면 기본으로 tp 쓰지만, 여러 개 쓰는 경우 value로 설정
		@Async(value = "tp")
		public ListenableFuture<String> hello() throws InterruptedException {
			log.info("hello()");
			Thread.sleep(1000);
			return new AsyncResult<>("Hello");
		}
	}

	@Bean
	ThreadPoolTaskExecutor tp(){
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		// 첫 요청이 왔을 때 10개 만듦
		te.setCorePoolSize(10);
		// corePool이 다 차면 queue가 차고, queue도 다 차면 maxPool 만큼 찰 수 있음
		te.setMaxPoolSize(100);
		// 줄 쓰레드가 없으니 대기할 때 사용
		te.setQueueCapacity(200);
		te.setThreadNamePrefix("mythread");
		te.initialize();
		return te;
	};

	public static void main(String[] args) {
//		try (ConfigurableApplicationContext c = SpringApplication.run(ReactiveApplication.class, args)){
//
//		};
		SpringApplication.run(ReactiveApplication.class, args);
	}

//	@Autowired
//	MyService myService;

	// application 뜨면 바로 실행되는 메서드(ApplicationRunner를 리턴하는 메소드)
//	@Bean
//	ApplicationRunner run(){
//		return args -> {
//			log.info("run()");
//			// 비동기일 때는  Future로 받음
//			ListenableFuture<String> f = myService.hello();
//			f.addCallback(success-> System.out.println(success), error -> System.out.println(error.getMessage()));
//			f.cancel(true);
////			log.info("exit: " + f.isDone());
////			log.info("result: " + f.get());
//			log.info("exit");
//		};
//	};

}
