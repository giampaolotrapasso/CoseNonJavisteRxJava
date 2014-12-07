package it.cosenonjaviste.rxjava.secondpost;

import rx.Observable;

import com.google.gson.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.ClientResponse.Status;

public class RestCallAsObserver {

  private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";

  public static void main(String[] args) {

    Observable<String> allCities = Observable.just("Zurich", "London", "Paris", "Berlin", "Roma", "Madrid", "Wien");

    allCities.subscribe(cityName -> {
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

      System.out.println(cityName + "-->" + weather);
    });

  }

}
