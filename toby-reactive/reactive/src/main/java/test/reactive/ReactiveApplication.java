package test.reactive;

import io.netty.channel.nio.NioEventLoopGroup;
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
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;

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
		//??????????????? ?????? ????????? ?????? ???????????? ?????????, ???????????? ????????? ?????? ??????
		AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
		@Autowired MyService myService;
//		@Autowired MyService myService;
//		Queue<DeferredResult> results = new ConcurrentLinkedQueue<>();

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
//		@GetMapping("/dr")
//		public DeferredResult<String> callable() throws InterruptedException {
//			log.info("defferedResult");
//			DeferredResult<String> dr = new DeferredResult<>(600000L);
//			results.add(dr);
//			return dr;
//		};
//
//		@GetMapping("/dr/count")
//		public String drcount() throws InterruptedException {
//			return String.valueOf(results.size());
//		};
//
//		@GetMapping("/dr/event")
//		public String drevent(String msg) throws InterruptedException {
//			for(DeferredResult<String> dr : results){
//				dr.setResult("Hello " + msg);
//				results.remove(dr);
//			}
//			return "OK";
//		};

		// ?????????
		@GetMapping("/emitter")
		public ResponseBodyEmitter emitter() throws InterruptedException {
			ResponseBodyEmitter emitter = new ResponseBodyEmitter();

			Executors.newSingleThreadExecutor().submit(() -> {
				try {
					for(int i = 1; i <= 50; i++){
						// http ???????????? ??????
						emitter.send("<p>Stream" + i + "</p>");
						Thread.sleep(100);
					}
				} catch(Exception e){

				}
			});

			return emitter;
		};

		// ??????
//		@GetMapping("/rest")
//		public String rest(int idx){
//			String res = rt.getForObject("http://localhost:8082/service?req={req}", String.class,"hello"+idx);
//			return res;
//		}

		@GetMapping("/rest")
		public DeferredResult<String> rest(int idx){
			DeferredResult<String> dr = new DeferredResult<>();

//			ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8082/service?req={req}", String.class,"hello"+idx);
//			f1.addCallback(s -> {
//				dr.setResult(s.getBody() + "/work");
//			}, e -> {
//				dr.setErrorResult(e.getMessage());
//			});

			toCF(rt.getForEntity("http://localhost:8082/service?req={req}", String.class, "h" + idx))
					.thenCompose(s -> {
						//????????? ???????????? ????????? api??? ???????????? ?????? ???????????? ????????? ?????????
//						if (1==1) throw new RuntimeException("error");
						return toCF(rt.getForEntity("http://localhost:8082/service2?req={req}", String.class, s.getBody()));
					})
					//.thenCompose(s2 -> toCF(myService.work(s2.getBody())))
					// ?????? ???????????? ?????? ?????? ???????????? ?????? ?????? ???
					//.thenApply(s2 -> myService.work(s2.getBody()))
					.thenApplyAsync(s2 -> myService.work(s2.getBody()))
					.thenAccept(s3 -> dr.setResult(s3))
					.exceptionally(e -> {
						dr.setErrorResult(e.getMessage());
						return (Void) null; //return ??????
					});
			return dr;
		}

		// ?????? ListenableFuture??? CompletableFuture??? ????????? ????????? ??? ??????
		<T> CompletableFuture<T> toCF(ListenableFuture<T> lf){
			CompletableFuture<T> cf = new CompletableFuture<T>();
			lf.addCallback(s -> cf.complete(s), e -> cf.completeExceptionally(e));
			return cf;
		}
	}

	@Service
	public static class MyService {
		// ??? ?????? ???????????? tp ?????????, ?????? ??? ?????? ?????? value??? ??????
		@Async(value = "tp")
		public ListenableFuture<String> hello() throws InterruptedException {
			log.info("hello()");
			Thread.sleep(1000);
			return new AsyncResult<>("Hello");
		}

//		@Async
//		public ListenableFuture<String> work(String req){
//			return new AsyncResult<>(req + "/asyncwork");
//		}

		// ?????? ???????????? ?????? ??????
		public String work(String req){
			return req + "/asyncwork";
		}
	}

	@Bean
	ThreadPoolTaskExecutor tp(){
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		// ??? ????????? ?????? ??? 10??? ??????
		te.setCorePoolSize(10);
		// corePool??? ??? ?????? queue??? ??????, queue??? ??? ?????? maxPool ?????? ??? ??? ??????
		te.setMaxPoolSize(100);
		// ??? ???????????? ????????? ????????? ??? ??????
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

	// application ?????? ?????? ???????????? ?????????(ApplicationRunner??? ???????????? ?????????)
//	@Bean
//	ApplicationRunner run(){
//		return args -> {
//			log.info("run()");
//			// ???????????? ??????  Future??? ??????
//			ListenableFuture<String> f = myService.hello();
//			f.addCallback(success-> System.out.println(success), error -> System.out.println(error.getMessage()));
//			f.cancel(true);
////			log.info("exit: " + f.isDone());
////			log.info("result: " + f.get());
//			log.info("exit");
//		};
//	};

}
