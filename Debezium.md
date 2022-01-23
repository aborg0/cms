# Using Debezium

Based on: https://github.com/debezium/debezium-examples/tree/main/tutorial#using-postgres

the topology defined in https://debezium.io/documentation/reference/stable/tutorial.html

It is also worth checking:
 - https://www.infoq.com/presentations/data-streaming-kafka-debezium/
 - https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html

Start the containers:

    docker-compose -f docker-compose-postgres.yaml up

## Start Postgres connector

    curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-postgres.json

## Consume messages from a Debezium topic

    docker-compose -f docker-compose-postgres.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic dbserver1.inventory.customers

## Modify records in the database via Postgres client

    docker-compose -f docker-compose-postgres.yaml exec postgres env PGOPTIONS="--search_path=inventory" bash -c 'psql -U $POSTGRES_USER postgres'

## Shut down the cluster

    docker-compose -f docker-compose-postgres.yaml down