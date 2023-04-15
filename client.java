import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileClient {
  private final ManagedChannel channel;
  private final FileServiceGrpc.FileServiceStub stub;

  public FileClient(String host, int port) {
    channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
    stub = FileServiceGrpc.newStub(channel);
  }

  public void sendFile(String fileName) throws IOException {
    StreamObserver<FileChunk> requestObserver = stub.sendFile(new StreamObserver<Empty>() {
      @Override
      public void onNext(Empty empty) {
        System.out.println("File sent successfully.");
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }

      @Override
      public void onCompleted() {
        System.out.println("File transfer completed.");
      }
    });

    FileInputStream fileInputStream = new FileInputStream(fileName);
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

    List<String> lines = new ArrayList<>();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      lines.add(line);
      if (lines.size() >= 5000) {
        sendChunk(lines, requestObserver);
        lines = new ArrayList<>();
      }
    }

    if (lines.size() > 0) {
      sendChunk(lines, requestObserver);
    }

    requestObserver.onCompleted();
  }

  private void sendChunk(List<String> lines, StreamObserver<FileChunk> requestObserver) {
    String chunkString = String.join("\n", lines);
    FileChunk chunk = FileChunk.newBuilder()
        .setChunkData(chunkString)
        .build();
    requestObserver.onNext(chunk);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    FileClient client = new FileClient("localhost", 50051);
    client.sendFile("input.csv");
    client.shutdown();
  }
}
