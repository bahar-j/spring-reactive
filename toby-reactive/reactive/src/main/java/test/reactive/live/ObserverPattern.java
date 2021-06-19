package test.reactive.live;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

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
                System.out.println(arg);
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        io.run();
    }
}
