package it.cosenonjaviste.rxjava;

import java.util.List;
import java.util.ArrayList;

import rx.*;
import rx.functions.*;

public class FirstPost {

	public static void main(String[] args) {

		// https://www.google.it/#q=rxjava&safe=off&start=30

		List<String> cityList = new ArrayList<>();
		cityList.add("Berlin");
		cityList.add("Roma");
		cityList.add("Madrid");
		cityList.add("Wien");

		Observable<String> cities = Observable.from(cityList);

		Observable<String> moreCities = Observable.just("Zurich", "London", "Paris");

		Observable<String> allCities = cities.concatWith(moreCities);

		Observer<String> traveller = new Observer<String>() {

			@Override
			public void onCompleted() {
				System.out.println("My trip is finished");
			}

			@Override
			public void onError(Throwable e) {
				System.out.println("I won't complete my trip!");
			}

			@Override
			public void onNext(String t) {
				System.out.println("I've just visited " + t);
			}

		};

		allCities.subscribe(traveller);

		// using only Action to observe the cities
		Action1<String> weather = new Action1<String>() {
			@Override
			public void call(String city) {
				System.out.println("The weather is sunny in " + city);
			}

		};

		allCities.subscribe(weather);

		// using lambda
		allCities.subscribe(city -> System.out.println("The weather is sunny in " + city));

		// applying standard operator
		allCities.map(city -> city.toUpperCase()).filter(city -> city.length() > 5).take(3).subscribe(city -> System.out.println("I LOVE " + city));

		cities.zipWith(moreCities, (String s1, String s2) -> "From " + s1 + " to " + s2).doOnNext(s -> System.out.println("#Debug: " + s)).map(s -> s.toLowerCase()).subscribe(System.out::println);

	}

}
