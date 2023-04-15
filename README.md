# cmpe275GC1

Steps
-- Install GRPC on Mac - brew install grpc

Compile proto file - 

protoc --java_out=. file.proto

compile java  - 

javac -cp ".:protobuf-java-3.17.0.jar" FileServer.java
javac -cp ".:protobuf-java-3.17.0.jar" FileClient.java

run server

java -cp ".:protobuf-java-3.17.0.jar" FileServer


Run Client

java -cp ".:protobuf-java-3.17.0.jar" FileClient
