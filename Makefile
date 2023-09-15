IMAGE_GROUP ?= ecosystem-appeng
IMAGE_NAME ?= gator-api-interceptor
IMAGE_REGISTRY ?= quay.io

.PHONY: push/gator-api-interceptor
push/gator-api-interceptor:
	./mvnw install \
	-Dquarkus.container-image.build=true \
	-Dquarkus.container-image.push=true \
	-Dquarkus.container-image.group=$(IMAGE_GROUP) \
	-Dquarkus.container-image.name=$(IMAGE_NAME) \
	-Dquarkus.container-image.registry=$(IMAGE_REGISTRY) \
	-f pom.xml
