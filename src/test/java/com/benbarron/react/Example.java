package com.benbarron.react;

public class Example {

    public static void main(String[] args) {
        Observable<Integer> observable = Observables.generate(observer -> {
            System.out.println("Subscribe");

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

        ConnectableObservable<String> hot = observable.map(i -> i.toString())
                .publish();

        hot.subscribe(System.out::println,() -> System.out.println("complete1"), t -> System.out.println(t.getMessage()));
        hot.subscribe(System.out::println,() -> System.out.println("complete2"), t -> System.out.println(t.getMessage()));
        hot.subscribe(System.out::println,() -> System.out.println("complete3"), t -> System.out.println(t.getMessage()));

        hot.connect();


    }
}
