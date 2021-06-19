package test.reactive.live;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class ObserverPattern {

    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for(int i = 1; i <= 10; i++){
                setChanged();
                //push
                notifyObservers(i);
            }
        }
    }

    public static void main(String[] args) {

        // <Pulling> 방식(next 메서드를 이용)
        Iterable<Integer> iter = Arrays.asList(1,2,3,4,5);
        // collection이 아니라도 iterable을 구현하면 for-each 구문으로 하나씩 탐색 가능
        // iterable(인터페이스), iterator(메서드)
        for(Integer i: iter){
//            System.out.println(i);
        }

        // 익명 클래스
//        Iterable<Integer> iter2 = new Iterable<Integer>() {
//            @Override
//            public Iterator<Integer> iterator() {
//                return null;
//            }
//        };

        // lambda
        Iterable<Integer> iter2 = () ->
            // return 생략
            new Iterator<Integer>() {
                int i = 0;
                final static int MAX = 10;
//                @Override
                public boolean hasNext() {
                    return i < MAX;
                }

//                @Override
                public Integer next() {
                    return ++i;
                }
            };

        for(Integer i : iter2){
//            System.out.println(i);
        }


        // <Observable> push 방식
        // Observer에게 event/data를 던져줌
        // Source(Observable) -> Event/Data -> Observer
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        // iterable과 달리 별개의 쓰레드에서 동작하는 코드를 손쉽게 작성 가능
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();

        // ObserverPattern의 문제점
        // 1. 데이터를 다 줬을 때 Complete의 개념이 없음
        // 2. Error 처리 -> ex.network 문제..-> 콜백 등을 통해 재시도해서 복구 가능한 예외인데 거기에 대한 고민이 없음
    }
}
