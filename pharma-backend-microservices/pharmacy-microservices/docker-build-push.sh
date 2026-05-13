#!/bin/bash
# Build all Docker images and push to Docker Hub
DOCKER_USER="jahnavilalitha"
VERSION="1.0.0"

SERVICES=(
  "eureka-server"
  "gateway-service"
  "auth-service"
  "catalog-service"
  "order-service"
  "admin-service"
  "payment-service"
  "notification-service"
)

echo "==> Logging in to Docker Hub as $DOCKER_USER..."
docker login -u "$DOCKER_USER"

if [ $? -ne 0 ]; then
  echo "Docker login failed. Aborting."
  exit 1
fi

echo ""
for SERVICE in "${SERVICES[@]}"; do
  IMAGE="$DOCKER_USER/$SERVICE:$VERSION"
  LATEST="$DOCKER_USER/$SERVICE:latest"

  echo "--- Building $IMAGE ---"
  docker build -t "$IMAGE" -t "$LATEST" "./$SERVICE"
  if [ $? -ne 0 ]; then echo "Build failed for $SERVICE. Aborting."; exit 1; fi

  echo "--- Pushing $IMAGE ---"
  docker push "$IMAGE"
  docker push "$LATEST"
  if [ $? -ne 0 ]; then echo "Push failed for $SERVICE. Aborting."; exit 1; fi

  echo ""
done

echo "All images pushed to docker.io/$DOCKER_USER"
