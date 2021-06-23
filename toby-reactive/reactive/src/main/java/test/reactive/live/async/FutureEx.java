package test.reactive.live.async;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class FutureEx {

    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback sc;
        ExceptionCallback ec;

        // callable: 수행해야할 비동기 작업, sc: 콜백
        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
//            if (sc == null) throw null;
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                ec.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 처음에는 thread 하나도 없는 threadpool. 쓰면서 점차 쌓임
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            // if (1 == 1) 안하면 밑의 코드가 절대 실행 안되니까 컴파일 에러 뜸
            if (1 == 1) throw new RuntimeException("Async Error!"); // -> 어쨌든 완료됐으니까 done 호출함
            log.info("Async");
            return "Hello";
        }, new SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println(result);
            }
        }, new ExceptionCallback() {
            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }
        });

        // future 구현체 중 하나
//        FutureTask<String> f = new FutureTask<String>(() -> {
//            Thread.sleep(2000);
//            log.info("Async");
//            return "Hello"; // return 할려면 submit -> Callable 구현
//        }) { // 익명 클래스
//            @Override
//            protected void done() {
//                // 비동기 작업이 끝나면 호출됨
//                try {
//                    System.out.println(get());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        };

        es.execute(f);
        es.shutdown();
        log.info("Exit");
    }
}