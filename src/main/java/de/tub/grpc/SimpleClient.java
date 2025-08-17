package de.tub.grpc;

import clientapi.AllBooksRequest;
import clientapi.AllBooksResponse;
import clientapi.BookRequest;
import clientapi.BookInfoResponse;
import clientapi.ClientBookServiceGrpc;
import clientapi.TopBooksRequest;
import clientapi.TopBooksResponse;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.List;
import java.util.Scanner;

public class SimpleClient {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		ManagedChannel channel = Grpc.newChannelBuilder("localhost:12345", InsecureChannelCredentials.create())
				.build();

		ClientBookServiceGrpc.ClientBookServiceBlockingStub stub =
				ClientBookServiceGrpc.newBlockingStub(channel);

		boolean running = true;

		while (running) {
			System.out.println("Bitte wählen Sie eine Option:");
			System.out.println("1 - Alle verfügbaren Bücher anzeigen");
			System.out.println("2 - Informationen über ein bestimmtes Buch anzeigen");
			System.out.println("3 - Information über Top-Bücher anzeigen");
			System.out.println("4 - Beenden");
			System.out.print("Ihre Auswahl: ");

			String input = scanner.nextLine();

			switch (input) {
				case "1":

					try {
						AllBooksResponse response = stub.getAllBooks(AllBooksRequest.newBuilder().build());
						List<String> bookTitles = response.getBookTitlesList();

						if (bookTitles.isEmpty()) {
							System.out.println(" Keine Bücher gefunden.");
						} else {
							System.out.println(" Verfügbare Bücher:");
							for (String title : bookTitles) {
								System.out.println(" - " + title);
							}
						}
					} catch (Exception e) {
						System.err.println("Fehler beim Abrufen der Bücher: " + e.getMessage());
					}
					break;

				case "2":

					System.out.print("Geben Sie den Buchtitel ein: ");
					String title = scanner.nextLine();

					try {
						BookRequest request = BookRequest.newBuilder().setTitle(title).build();
						BookInfoResponse response = stub.getBookInfo(request);

						System.out.println("Buchinformationen:");
						System.out.println("Titel: " + response.getTitle());
						System.out.println("Autor: " + response.getAuthor());
						System.out.println("Jahr: " + response.getYear());
						System.out.println("Seiten: " + response.getPages());
						System.out.println("Durchschnittliche Bewertung: " + response.getAverageRating());
					} catch (Exception e) {
						System.err.println("Fehler beim Abrufen der Buchinformation: " + e.getMessage());
					}
					break;

				case "4":
					running = false;
					System.out.println("Client wird beendet.");
					break;

				case "3":
					try {
						System.out.print("Wie viele Top-Bücher möchten Sie sehen? (z.B 3): ");
						String countStr = scanner.nextLine();
						int count = Integer.parseInt(countStr);

						TopBooksRequest topReq = TopBooksRequest.newBuilder()
								.setCount(count)
								.build();

						TopBooksResponse topResp = stub.getTopBooks(topReq);

						List<BookInfoResponse> topBooks = topResp.getBooksList();

						if (topBooks.isEmpty()) {
							System.out.println("Keine bewerteten Bücher gefunden.");
						} else {
							System.out.println("Top " + count + " Bücher nach Bewertung:");
							for (int i = 0; i < topBooks.size(); i++) {
								BookInfoResponse b = topBooks.get(i);
								System.out.printf("%d. %s von %s (%.2f Sterne)\n",
										i + 1,
										b.getTitle(),
										b.getAuthor(),
										b.getAverageRating());
							}
						}
					} catch (Exception e) {
						System.err.println("Fehler beim Abrufen der Top-Bücher: " + e.getMessage());
					}
					break;

				default:
					System.out.println("Ungültige Auswahl. Bitte erneut versuchen.");
			}
		}

		channel.shutdown();
	}
}