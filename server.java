import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileServer {
  private final int port;
  private Server server;

  public FileServer(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    server = ServerBuilder.forPort(port)
        .addService(new FileServiceImpl())
        .build()
        .start();
    System.out.println("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      FileServer.this.stop();
      System.err.println("*** server shut down");
    }));
  }

  public void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  private static class FileServiceImpl extends FileServiceGrpc.FileServiceImplBase {
    @Override
    public StreamObserver<FileChunk> sendFile(StreamObserver<Empty> responseObserver) {
      return new StreamObserver<FileChunk>() {
        FileOutputStream outputStream = null;

        @Override
        public void onNext(FileChunk chunk) {
          try {
            if (outputStream == null) {
              outputStream = new FileOutputStream("output.csv");
            }
            outputStream.write(chunk.getChunk().toByteArray());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onError(Throwable throwable) {
          throwable.printStackTrace();
        }

        @Override
        public void onCompleted() {
          try {
            if (outputStream != null) {
              outputStream.close();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          responseObserver.onNext(Empty.getDefaultInstance());
          responseObserver.onCompleted();
        }
      };
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    FileServer server = new FileServer(50051);
    server.start();
    server.blockUntilShutdown();
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
}
