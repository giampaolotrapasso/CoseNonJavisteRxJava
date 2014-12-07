package it.cosenonjaviste.rxjava.thirdpost;

import java.util.concurrent.*;

import org.apache.commons.lang3.tuple.ImmutablePair;

import rx.*;
import rx.functions.*;
import rx.schedulers.Schedulers;

import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.ClientResponse.Status;

public class ThirdPostLastExample {

	public static final ExecutorService executorService = Executors.newFixedThreadPool(2);
	public static Boolean stop = false;

	static Func1<String, String> currentWeatherUrl = s -> "http://api.openweathermap.org/data/2.5/weather?q=" + s;

	static Func1<JsonElement, String> currentWeatherExtractor = jsonElement -> {

		String currentWeather = jsonElement.getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();

		return currentWeather;

	};
	
	private static void debug(String message) {
		System.out.printf("%-28.28s: %-10.10s%n", Thread.currentThread().getName(), message);
	}
	
	private static void debugPair(ImmutablePair<String, String> pair) {
		System.out.printf("%-28.28s: (%s,%s)%n", Thread.currentThread().getName(), pair.left, pair.right);
	}
	
	static Action1<? super String> printString = cityName -> debug(cityName);
	static Action1<ImmutablePair<String, String>> printPair = pair -> debugPair(pair);

	public static Observable<ImmutablePair<String, String>> getWeather(final String cityName, Func1<String, String> urlComposer, Func1<JsonElement, String> extractFunction) {

		Observable.OnSubscribe<ImmutablePair<String, String>> obs = subscriber -> {

			executorService.submit(() -> {
				try {
					String url = urlComposer.call(cityName);

					Client client = Client.create();
					WebResource webResource = client.resource(url);

					ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

					if (response.getStatus() != Status.OK.getStatusCode()) {
						throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
					}

					String output = response.getEntity(String.class);

					JsonElement json = new JsonParser().parse(output);

					String weather = extractFunction.call(json);

					subscriber.onNext(ImmutablePair.of(cityName, weather));
					subscriber.onCompleted();
				} catch (Exception e) {
					subscriber.onError(e);
				}
			});

		};

		return Observable.create(obs).subscribeOn(Schedulers.from(executorService));
	}

	public static void main(String[] args) throws InterruptedException {

		Observer<ImmutablePair<String, String>> cityObserver = new Observer<ImmutablePair<String, String>>() {

			@Override
			public void onCompleted() {
				stop = true;
			}

			@Override
			public void onError(Throwable e) {
				System.out.println(e);
			}

			@Override
			public void onNext(ImmutablePair<String, String> pair) {
				System.out.printf("(%s,%s)%n", pair.left, pair.right);
			}
		};

		Observable<String> allCities = Observable.just("Zurich", "London", "Paris", "Berlin", "Roma", "Madrid", "Wien");

		allCities.doOnNext(printString).flatMap(cityName -> getWeather(cityName, currentWeatherUrl, currentWeatherExtractor)).doOnNext(printPair).subscribe(cityObserver);

		while (!stop) {
			TimeUnit.SECONDS.sleep(1);
		}
		executorService.shutdown();
		System.out.println("Completed");

	}

}
