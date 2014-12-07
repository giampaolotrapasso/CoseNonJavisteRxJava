package it.cosenonjaviste.rxjava.thirdpost;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ThirdPostBasicExamples {

  public static void logStringWithThread(String string) {
    System.out.printf("%-26.26s: %-10.10s%n", Thread.currentThread().getName(), string);
  }

  public static Action1<? super String> debugString = string -> logStringWithThread(string);

  public static void main(String[] args) {

    Observable<String> allCities = Observable.just("Zurich", "London", "Paris", "Berlin", "Roma", "Madrid", "Wien");

    // very first example, everything goes on main thread
    /*
    allCities
        .doOnNext(debugString)
        .map(s -> s + "-")
        .doOnNext(debugString)
        .map(s -> "-" + s)
        .subscribe(debugString);
    */
 
    // pay attention, RxJava can change thread even if you are not aware of
    // for example, delay function makes computation asynch, so if you need
    // main thread to do some UI updates, make sure you switch back to main
    // with observeOn method
    /*
    allCities
        .doOnNext(debugString)
        .delay(500, TimeUnit.MILLISECONDS)
        .map(s -> s + "-")
        .doOnNext(debugString)
        .map(s -> "-" + s)
        .subscribe(debugString);
    */

    // subscribeOn tells RxJava what scheduler use to start the emission of
    // elements
    // If you call twice the method, second call is ignored.
    /*
    allCities
        .subscribeOn(Schedulers.computation())
        .doOnNext(debugString)
        .map(s -> s + "-")
        .doOnNext(debugString)
        .map(s -> "-" + s)
        .subscribe(debugString);
    */
    
    //If we put observeOn at the beginning of the chain, we have the same effect as
    // subscribeOn
    /*
    allCities
        .observeOn(Schedulers.computation())
        .doOnNext(debugString)
        .map(s -> s + "-")
        .doOnNext(debugString)
        .map(s -> "-" + s)
        .subscribe(debugString);
    */
   
    /*
    allCities
        .doOnNext(debugString)
        .observeOn(Schedulers.computation())
        .map(s -> "-" + s)
        .doOnNext(debugString)
        .map(s -> s + "-")
        .subscribe(debugString);
        */
     
    
    allCities
        .subscribeOn(Schedulers.computation())
        .doOnNext(debugString)
        .map(s -> "-" + s)
        .observeOn(Schedulers.io())
        .doOnNext(debugString)
        .map(s -> s + "-")
        .observeOn(Schedulers.computation())
        .subscribe(debugString);
     

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
