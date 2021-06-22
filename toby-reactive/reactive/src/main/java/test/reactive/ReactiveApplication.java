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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@SpringBootApplication
@Slf4j
@EnableAsync
public class ReactiveApplication {
	@RestController
	public static class Controller {
		@RequestMapping("/hello")
		public Publisher<String> hello(String name){
			return new Publisher<String>() {
				@Override
				public void subscribe(Subscriber<? super String> s) {
					s.onSubscribe(new Subscription() {
						@Override
						public void request(long n) {
							s.onNext("Hello " + name);
							s.onComplete();
						}

						@Override
						public void cancel() {

						}
					});
				}
			};
		}
	}

	@Component
	public static class MyService {
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
		te.setCorePoolSize(10);
		te.setMaxPoolSize(100);
		te.setQueueCapacity(200);
		te.setThreadNamePrefix("mythread");
		te.initialize();
		return te;
	};

	public static void main(String[] args) {
		try (ConfigurableApplicationContext c = SpringApplication.run(ReactiveApplication.class, args)){

		};
	}

	@Autowired
	MyService myService;

	// application 뜨면 바로 실행되는 메서드(ApplicationRunner를 리턴하는 메소드)
	@Bean
	ApplicationRunner run(){
		return args -> {
			log.info("run()");
			// 비동기일 때는  Future로 받음
			ListenableFuture<String> f = myService.hello();
			f.addCallback(success-> System.out.println(success), error -> System.out.println(error.getMessage()));
			f.cancel(true);
//			log.info("exit: " + f.isDone());
//			log.info("result: " + f.get());
			log.info("exit");
		};
	};

}
