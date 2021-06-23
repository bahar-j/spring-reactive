package test.reactive.live.async;

import lombok.extern.slf4j.Slf4j;

import javax.management.RuntimeErrorException;
import java.util.concurrent.*;

@Slf4j
public class CompletableFutureEx {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(10);
//        CompletableFuture<Integer> f = CompletableFuture.completedFuture(1);
        // 혹은
        // CompletableFuture<Integer> f = new CompletableFuture<>();
        // f.completeExceptionally(new RuntimeException());

        // 에러 일으키기
        // f.completeExceptionally(new RuntimeException());

//        System.out.println(f.get());

        // runAsync : runnable 구현 -> 비동기 작업의 결과물을 사용 불가
//        CompletableFuture.runAsync(() -> log.info("runAsync"))
//                        .thenRun(() -> log.info("thenRunAsync")) // 해당 백그라운드 쓰레드에서 이어서
//                        .thenRun(() -> log.info("thenRunAsync2"));

        CompletableFuture
                .supplyAsync(() -> {
                    log.info("applyAsync {}");
//                    if (1==1) throw new RuntimeException();
                    return 1;
                }, es)
                .thenCompose(s -> { // flatMap과 유사
                    log.info("thenApply {}", s);
                    return CompletableFuture.completedFuture(s+1);
                })
                .thenApplyAsync(s2 -> { //map과 유사
                    log.info("thenApply2 {}", s2);
                    return s2*30;
                }, es)
                .exceptionally(e -> -10)
                .thenAcceptAsync(s3 -> {
                    log.info("thenAccept {}", s3);
                    es.shutdown();
                }, es);

        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);

    }
}
