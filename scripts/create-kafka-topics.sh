KAFKA_BOOTSTRAP_SERVER=${KAFKA_BOOTSTRAP_SERVER:-localhost:29092}

echo "Creating Kafka topics..."
echo "Bootstrap server: $KAFKA_BOOTSTRAP_SERVER"

docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic auth_notifications \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic auth_metrics \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

echo "Topics created successfully!"
echo ""
echo "Listing all topics:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
