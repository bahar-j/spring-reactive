package test.reactive.live;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Flow.*;

public class PubSub {
    public static void main(String[] args) throws InterruptedException {

        Iterable<Integer> itr = Arrays.asList(1,2,3,4,5); // DB에서 가져온 Collection 데이터
        ExecutorService es = Executors.newSingleThreadExecutor();

        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = itr.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        // Future<?> f = es.submit()으로도 가능
                        // 결과를 받아서 거기에 따라 cancel하거나 할 수 있음
                        es.execute(() -> {
                            int i = 0;
                            try {
                                while(i++ < n){
                                    if (it.hasNext()){
                                        subscriber.onNext(it.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch(RuntimeException e){
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                // 현재 버퍼에 얼마나 여유가 있는지, 쓰레드 사용률, CPU 상황 등에 따라서 넘겨줄 수 있음
                // 사실 원래 스케쥴러로 조절 가능
                // 동일한 쓰레드에서 request해야함
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " onNext: " + item);
//                if (--bufferSize <= 0){
//                    bufferSize = 2;
                this.subscription.request(1);
//                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
                // 그냥 exception이 뜨는게 아니라 onError를 통해
                // 상황 체크하고 재시도 등 여러 동작 가능

            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        };

        p.subscribe(s);

        es.awaitTermination(1, TimeUnit.MINUTES);
        es.shutdown();
    }
}
