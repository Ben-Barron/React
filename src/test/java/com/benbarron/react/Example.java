package com.benbarron.react;

public class Example {

    public static void main(String[] args) {
        Observable<Integer> observable = Observables.generate(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onNext(4);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }

            observer.onNext(5);
            observer.onComplete();
        });

        observable.map(i -> i.toString())
                .subscribe(System.out::println,() -> System.out.println("complete"), t -> System.out.println(t.getMessage()));
    }
}
