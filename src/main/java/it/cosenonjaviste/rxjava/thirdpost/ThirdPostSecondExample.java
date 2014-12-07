package it.cosenonjaviste.rxjava.thirdpost;

import rx.Observable;
import rx.functions.*;
import rx.schedulers.Schedulers;

import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.ClientResponse.Status;

public class ThirdPostSecondExample {

  private static void logStringWithThread(String message) {
    System.out.printf("%-26.26s: %-70.70s%n", Thread.currentThread().getName(), message);
  }

  static Action1<? super String> debugString = string -> logStringWithThread(string);
  
  static Action1<? super String> debugForecast = string -> logStringWithThread(string +" (forecast)");

  static Func1<String, String> currentWeatherUrl = s -> "http://api.openweathermap.org/data/2.5/weather?q=" + s;
  static Func1<String, String> forecastWeatherUrl = s -> "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + s + "&mode=json&units=metric&cnt=2";

  static Func1<JsonElement, String> extractCurrentWeather = jsonElement -> {

    String currentWeather = jsonElement.getAsJsonObject()
        .get("weather").getAsJsonArray().get(0)
        .getAsJsonObject().get("description").getAsString();

    return currentWeather;

  };

  static Func1<JsonElement, String> extractTomorrowWeather = jsonElement -> {

    String tomorrowWeather = jsonElement.getAsJsonObject()
        .get("list").getAsJsonArray().get(1)
        .getAsJsonObject().get("weather")
        .getAsJsonArray().get(0)
        .getAsJsonObject().get("description").getAsString();

    return tomorrowWeather;

  };

  public static Observable<String> getWeather(final String cityName, Func1<String, String> urlComposer, Func1<JsonElement, String> extractFunction) {

    Observable.OnSubscribe<String> onSubScribe = subscriber -> {

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

        subscriber.onNext(weather);
        subscriber.onCompleted();
      } catch (Exception e) {
        subscriber.onError(e);
      }
    };

    return Observable.create(onSubScribe);
  }

  public static Observable<String> getCurrentWeather(String cityName) {
    return getWeather(cityName, currentWeatherUrl, extractCurrentWeather);
  }

  public static Observable<String> getTomorrowWeather(String cityName) {
    return getWeather(cityName, forecastWeatherUrl, extractTomorrowWeather);
  }

  public static void main(String[] args) {

    Observable<String> allCities = Observable.just("Zurich", "London", "Paris", "Berlin", "Roma", "Madrid", "Wien");

    
    Observable<String> restCalls = allCities.flatMap(
        cityName -> Observable.zip(
            getCurrentWeather(cityName),
            getTomorrowWeather(cityName),
            (today, tomorrow) -> "today: " + today + ", tomorrow: " + tomorrow).map(weather -> cityName + "->" + weather)
        );
        

    restCalls.subscribe(System.out::println);
    
    
    

    /*
    Observable<String> restCalls = allCities.doOnNext(debugString).flatMap(
        cityName -> Observable.zip(
            getCurrentWeather(cityName).doOnNext(debugString),
            getTomorrowWeather(cityName).subscribeOn(Schedulers.io()).doOnNext(debugForecast),
            (today, tomorrow) -> "today: " + today + ", tomorrow: " + tomorrow).map(weather -> cityName + "->" + weather)
        );

   
    restCalls.subscribe(debugString);
    */
    
    
    
   
    
    
    try {
      Thread.sleep(1000000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
