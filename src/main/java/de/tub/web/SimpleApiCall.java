package de.tub.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Scanner;

public class SimpleApiCall {

	private static final HttpClient client = HttpClient.newHttpClient();

	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.print("Geben Sie ein Land ein (oder 'exit'): ");
			String country = scanner.nextLine().trim();

			if (country.equalsIgnoreCase("exit")) {
				System.out.println("Programm beendet.");
				break;
			}

			String url = "https://restcountries.com/v3.1/name/" + country;
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				System.out.println("Land nicht gefunden.");
				continue;
			}

			String body = response.body();

			String capital = null;
			int capStart = body.indexOf("\"capital\":[\"");
			if (capStart != -1) {
				int capEnd = body.indexOf("\"]", capStart);
				if (capEnd != -1) {
					capital = body.substring(capStart + 12, capEnd);
				}
			}

			Double lat = null, lon = null;
			int latlngStart = body.indexOf("\"capitalInfo\":{\"latlng\":[");
			if (latlngStart != -1) {
				int latStart = latlngStart + "\"capitalInfo\":{\"latlng\":[".length();
				int latEnd = body.indexOf(",", latStart);
				if (latEnd != -1) {
					int lonEnd = body.indexOf("]", latEnd);
					if (lonEnd != -1) {
						try {
							lat = Double.parseDouble(body.substring(latStart, latEnd).trim());
							lon = Double.parseDouble(body.substring(latEnd + 1, lonEnd).trim());
						} catch (NumberFormatException e) {
							lat = null;
							lon = null;
						}
					}
				}
			}

			if (capital == null || lat == null || lon == null) {
				System.out.println("Hauptstadt oder Koordinaten nicht gefunden.");
				continue;
			}

			System.out.println("Hauptstadt: " + capital);
			System.out.println("Koordinaten: " + lat + ", " + lon);

			String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
					"&longitude=" + lon + "&current_weather=true";

			HttpRequest weatherRequest = HttpRequest.newBuilder()
					.uri(URI.create(weatherUrl))
					.GET()
					.build();

			HttpResponse<String> weatherResponse = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());
			if (weatherResponse.statusCode() != 200) {
				System.out.println("Fehler beim Abrufen der Wetterdaten.");
				continue;
			}

			String weatherBody = weatherResponse.body();

			String temperature = null;
			String windspeed = null;

			int currentStart = weatherBody.indexOf("\"current_weather\":{");
			if (currentStart != -1) {
				int tempStart = weatherBody.indexOf("\"temperature\":", currentStart);
				int windStart = weatherBody.indexOf("\"windspeed\":", currentStart);
				if (tempStart != -1) {
					int tempEnd = weatherBody.indexOf(",", tempStart);
					if (tempEnd != -1) {
						temperature = weatherBody.substring(tempStart + 14, tempEnd).trim();
					}
				}
				if (windStart != -1) {
					int windEnd = weatherBody.indexOf(",", windStart);
					if (windEnd == -1) {
						windEnd = weatherBody.indexOf("}", windStart);
					}
					if (windEnd != -1) {
						windspeed = weatherBody.substring(windStart + 12, windEnd).trim();
					}
				}
			}

			if (temperature != null && windspeed != null) {
				System.out.println("Temperatur in " + capital + ": " + temperature + " °C");
				System.out.println("Windgeschwindigkeit: " + windspeed + " km/h");
			} else {
				System.out.println("Wetterdaten unvollständig.");
			}

			System.out.println("------------");
		}
	}}
