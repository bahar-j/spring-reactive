package test.reactive.live.stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import test.reactive.live.stream.DelegateSub;

import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// pub -> data1-> mapPub -> data2 -> sub

public class PubSub2 {
    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]");
//        Publisher<Integer> map2Pub = mapPub(mapPub, s -> -s);
//        Publisher<Integer> sumPub = sumPub(pub);
        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder() ,(a, b) -> a.append(b+","));
        reducePub.subscribe(logSub());
    }

    // reduce
    // 1, 2, 3
    // 1 -> (0, 1) -> 0 + 1 = 1
    // 2 -> (1, 2) -> 1 + 2 = 3
    // 3 -> (3, 3) -> 3 + 3 = 6
    // 제네릭 정할 때 구체타입 예시 들어서 생각해놓고 바꾸면 좋음
    private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T, R>(sub){
                    R result = init;

                    @Override
                    public void onNext(T integer) {
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

//    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
//        // 익명 클래스로 subscribe 메서드 오버라이딩한 것 람다식으로 변경
//        return (sub) -> {
//                pub.subscribe(new DelegateSub(sub){
//                    int sum = 0;
//
//                    @Override
//                    public void onNext(Integer integer) {
//                        sum += integer;
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        System.out.println(sum);
//                        sub.onNext(sum);
//                        sub.onComplete();
//                    }
//                });
//        };
//    }

    // T 타입 퍼블리셔가 들어와서 R 타입으로 바뀌는 func
    // Function<input type, return type>
//    private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
//        return new Publisher<R>() {
//            @Override
//            public void subscribe(Subscriber<? super R> sub) {
//                //pub: 원래 제일 왼쪽의 pub
//                pub.subscribe(new DelegateSub<T, R>(sub) {
//                    @Override
//                    public void onNext(T val) {
//                        sub.onNext(f.apply(val));
//                    }
//                });
//            }
//        };
//    }

    // 제네릭 메소드에서는 <T> 타입 파라미터를 리턴 타입 앞에 줌
    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("subscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T val) {
                System.out.println("job: " + val);
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
