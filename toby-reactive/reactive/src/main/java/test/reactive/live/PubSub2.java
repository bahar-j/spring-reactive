package test.reactive.live;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// pub -> data1-> mapPub -> data2 -> sub

public class PubSub2 {
    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
//        Publisher<Integer> map2Pub = mapPub(mapPub, s -> -s);
//        Publisher<Integer> sumPub = sumPub(pub);
       Publisher<Integer> reducePub = reducePub(pub, 0 ,(BiFunction<Integer, Integer, Integer>) (a, b) -> a + b);
        reducePub.subscribe(logSub());
    }

    // reduce
    // 1, 2, 3
    // 1 -> (0, 1) -> 0 + 1 = 1
    // 2 -> (1, 2) -> 1 + 2 = 3
    // 3 -> (3, 3) -> 3 + 3 = 6
    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bf) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub){
                    int result = init;

                    @Override
                    public void onNext(Integer integer) {
                        // 이렇게 하면 + 아니라도 가능
                        result = bf.apply(result, integer);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        // 익명 클래스로 subscribe 메서드 오버라이딩한 것 람다식으로 변경
        return (sub) -> {
                pub.subscribe(new DelegateSub(sub){
                    int sum = 0;

                    @Override
                    public void onNext(Integer integer) {
                        sum += integer;
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(sum);
                        sub.onNext(sum);
                        sub.onComplete();
                    }
                });
        };
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                //pub: 원래 제일 왼쪽의 pub
                pub.subscribe(new DelegateSub(sub) {
                    @Override
                    public void onNext(Integer integer) {
                        sub.onNext(f.apply(integer));
                    }
                });
            }
        };
    }

    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("subscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("job: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("error detected");
            }

            @Override
            public void onComplete() {
                System.out.println("completed");
            }
        };
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(s -> sub.onNext(s));
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {
                        System.out.println("job canceled");
                    }
                });
            }
        };
    }
}
