package it.cosenonjaviste.rxjava.secondpost;

import rx.Observable;

import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.ClientResponse.Status;

public class RestCallAsObservable {

  private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";

  public static Observable<String> getCurrentWeather(final String cityName) {

    Observable.OnSubscribe<String> onSubscribe = subscriber -> {

      try {
        String url = BASE_URL + cityName;

        Client client = Client.create();
        WebResource webResource = client.resource(url);

        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

        if (response.getStatus() != Status.OK.getStatusCode()) {
          throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        String output = response.getEntity(String.class);

        JsonElement json = new JsonParser().parse(output);

        String weather = json.getAsJsonObject().get("weather")
                             .getAsJsonArray().get(0)
                             .getAsJsonObject().get("description")
                             .getAsString();

        subscriber.onNext(weather);
        subscriber.onCompleted();
      } catch (Exception e) {
        subscriber.onError(e);
      }
    };

    return Observable.create(onSubscribe);
  }

  public static void main(String[] args) {
    // a little test to check getWeather Observable
    getCurrentWeather("London").subscribe(System.out::println);

    Observable<String> allCities = Observable.just("Zurich", "London", "Paris", "Berlin", "Roma", "Madrid", "Wien");

    // first attempt, you can see weather, but no city names
    Observable<String> allWeathers = allCities.flatMap(city -> getCurrentWeather(city));
    
    allWeathers.subscribe(System.out::println);
    
    // concat city and weather using a lambda inside another.
    allCities.flatMap(city -> getCurrentWeather(city).map(weather -> city + "->" + weather))
             .subscribe(System.out::println);

  
  }

}
