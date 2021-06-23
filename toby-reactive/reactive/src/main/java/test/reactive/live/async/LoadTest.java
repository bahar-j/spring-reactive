package test.reactive.live.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8082/service?req={req}";

        CyclicBarrier barrier = new CyclicBarrier(101);

//        StopWatch main = new StopWatch();
//        main.start();

        for(int i = 0; i < 100; i++){
            //callable로 변경(submit, return값 있음)
            es.submit(() -> {
                // multi thread safe
                int idx = counter.addAndGet(1);
                log.info("Thread {}", idx);

                // runnable 인터페이스면 메소드 밖으로 던질 수 없음
                barrier.await();

                StopWatch sw = new StopWatch();
                sw.start();

                String res = rt.getForObject(url, String.class, idx);

                sw.stop();
                log.info("Elapsed: {} {} / {}", idx, sw.getTotalTimeSeconds(), res);
                return null;
            });
        }

        barrier.await();
        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());
    }
}
