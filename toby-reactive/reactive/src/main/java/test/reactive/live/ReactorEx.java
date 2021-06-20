package test.reactive.live;

import reactor.core.publisher.Flux;

public class ReactorEx {

    public static void main(String[] args) {
        // Flux: Publisher interface의 구현체
        // filter, *interval*, buffer, flatmap..
        // 리소스 요청을 보내고 어떤게 먼저 돌아올지 모르는데,
        // 어떤 롤을 가지고 데이터를 subscriber가 최종적으로 반환할지
        Flux.<Integer>create(e -> {
            e.next(1);
            e.next(2);
            e.next(3);
            e.complete();
        }).log().map(s->s*10).reduce(0, (a,b) -> a+b).log().subscribe();
    }
}
