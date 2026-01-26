#!/bin/bash

KAFKA_BOOTSTRAP_SERVER=${KAFKA_BOOTSTRAP_SERVER:-localhost:29092}

echo "Deleting Kafka topics..."
echo "Bootstrap server: $KAFKA_BOOTSTRAP_SERVER"
echo ""

# List topics before deletion
echo "Current topics:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
echo ""

# Delete auth_notifications topic if it exists
echo "Deleting topic: auth_notifications"
docker exec kafka kafka-topics --delete \
  --bootstrap-server localhost:9092 \
  --topic auth_notifications \
  2>/dev/null && echo "  ✓ auth_notifications deleted" || echo "  ✗ auth_notifications not found or already deleted"

# Delete auth_metrics topic if it exists
echo "Deleting topic: auth_metrics"
docker exec kafka kafka-topics --delete \
  --bootstrap-server localhost:9092 \
  --topic auth_metrics \
  2>/dev/null && echo "  ✓ auth_metrics deleted" || echo "  ✗ auth_metrics not found or already deleted"

echo ""
echo "Topics deleted successfully!"
echo ""
echo "Remaining topics:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
