cp ../target/pingerdaemon-rabbitmq-client-0.0.1-SNAPSHOT.jar ./
docker build -t my-pingerdaemon-rabbitmq-client .
docker run -d --name pingerdaemon-rabbitmq-client --network fedsky pingerdaemon-rabbitmq-client
