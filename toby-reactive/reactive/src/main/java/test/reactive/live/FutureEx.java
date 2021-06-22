package test.reactive.live;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class FutureEx {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 처음에는 thread 하나도 없는 threadpool. 쓰면서 점차 쌓임
        ExecutorService es = Executors.newCachedThreadPool();

//        es.execute(() -> {
        Future<String> f = es.submit(() -> {
//            try {
                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            log.info("Async");
            return "Hello"; // return 할려면 submit -> Callable 구현
        }); // Runnable 구현

        System.out.println(f.isDone());
        Thread.sleep(2000);
        log.info("Exit");
        System.out.println(f.isDone());
        System.out.println(f.get());
//        log.info("Exit"); // -> future 쓰면 가장 마지막에 찍힘
    }
}