package de.tub.grpc;

import clientapi.BookRequest;
import clientapi.BookInfoResponse;
import clientapi.ClientBookServiceGrpc;

import de.tub.BookInfoRequest;
import de.tub.BookRatingGrpc;

import de.tub.grpc.auth.AuthEncoder;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimpleServer extends ClientBookServiceGrpc.ClientBookServiceImplBase {

    private static final String isisEmail = "taras.levankou@campus.tu-berlin.de";
    private static final String isisMatrikelNr = "493936";
    private static final String authString = AuthEncoder.generateAuthString(isisEmail, isisMatrikelNr);
    private static final String TUB_SERVER_ADDRESS = "141.23.71.222:60002";
    @Override
    public void getBookInfo(BookRequest request, StreamObserver<BookInfoResponse> responseObserver) {
        ManagedChannel tubChannel = Grpc.newChannelBuilder(TUB_SERVER_ADDRESS, InsecureChannelCredentials.create())
                .build();

        BookRatingGrpc.BookRatingBlockingStub tubStub = BookRatingGrpc.newBlockingStub(tubChannel);

        BookInfoRequest tubRequest = BookInfoRequest.newBuilder()
                .setAuth(authString)
                .setBookTitle(request.getTitle())
                .build();
        try {
            de.tub.BookInfoResponse tubResponse = tubStub.getBookInfo(tubRequest);

            BookInfoResponse clientResponse = BookInfoResponse.newBuilder()
                    .setTitle(tubResponse.getBookTitle())
                    .setAuthor(tubResponse.getAuthor())
                    .setYear(tubResponse.getYear())
                    .setPages(tubResponse.getPages())
                    .setAverageRating(tubResponse.getAverageRating())
                    .build();

            responseObserver.onNext(clientResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        } finally {
            tubChannel.shutdown();
        }
    }

    @Override
    public void getAllBooks(clientapi.AllBooksRequest request,
                            StreamObserver<clientapi.AllBooksResponse> responseObserver) {

        ManagedChannel tubChannel = Grpc.newChannelBuilder(TUB_SERVER_ADDRESS, InsecureChannelCredentials.create())
                .build();

        BookRatingGrpc.BookRatingBlockingStub tubStub = BookRatingGrpc.newBlockingStub(tubChannel);

        de.tub.AllBooksRequest tubRequest = de.tub.AllBooksRequest.newBuilder()
                .setAuth(authString)
                .build();

        try {
            de.tub.AllBooksResponse tubResponse = tubStub.getAllBooks(tubRequest);

            clientapi.AllBooksResponse.Builder responseBuilder = clientapi.AllBooksResponse.newBuilder();
            responseBuilder.addAllBookTitles(tubResponse.getBookTitlesList());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        } finally {
            tubChannel.shutdown();
        }
    }

    @Override
    public void getTopBooks(clientapi.TopBooksRequest request,
                            StreamObserver<clientapi.TopBooksResponse> responseObserver) {

        ManagedChannel tubChannel = Grpc.newChannelBuilder(TUB_SERVER_ADDRESS, InsecureChannelCredentials.create())
                .build();

        BookRatingGrpc.BookRatingBlockingStub tubStub = BookRatingGrpc.newBlockingStub(tubChannel);

        try {
            de.tub.AllBooksResponse allBooksResp = tubStub.getAllBooks(
                    de.tub.AllBooksRequest.newBuilder().setAuth(authString).build()
            );
            List<String> allTitles = allBooksResp.getBookTitlesList();

            List<BookInfoResponse> allBooks = new ArrayList<>();

            for (String title : allTitles) {
                de.tub.BookInfoRequest infoReq = de.tub.BookInfoRequest.newBuilder()
                        .setAuth(authString)
                        .setBookTitle(title)
                        .build();

                de.tub.BookInfoResponse tubInfo = tubStub.getBookInfo(infoReq);

                clientapi.BookInfoResponse book = clientapi.BookInfoResponse.newBuilder()
                        .setTitle(tubInfo.getBookTitle())
                        .setAuthor(tubInfo.getAuthor())
                        .setYear(tubInfo.getYear())
                        .setPages(tubInfo.getPages())
                        .setAverageRating(tubInfo.getAverageRating())
                        .build();

                allBooks.add(book);
            }

            int count = request.getCount();

            List<clientapi.BookInfoResponse> topN = allBooks.stream()
                    .sorted(Comparator.comparing(clientapi.BookInfoResponse::getAverageRating).reversed())
                    .limit(count)
                    .toList();

            clientapi.TopBooksResponse response = clientapi.TopBooksResponse.newBuilder()
                    .addAllBooks(topN)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        } finally {
            tubChannel.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(12345)
                .addService(new SimpleServer())
                .build();

        System.out.println("gRPC-Server läuft auf Port 12345...");
        server.start();
        server.awaitTermination();
    }
}