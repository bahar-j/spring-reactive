package test.reactive.live;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

// Generic 클래스로 변환
public class DelegateSub<T, R> implements Subscriber<T> {
    Subscriber sub;

    public DelegateSub(Subscriber<? super R> sub) {
        this.sub = sub;
    }

    @Override
    public void onSubscribe(Subscription s) {
        sub.onSubscribe(s);
    }

    @Override
    public void onNext(T integer) {
        sub.onNext(integer);
    }

    @Override
    public void onError(Throwable t) {
        sub.onError(t);
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}
