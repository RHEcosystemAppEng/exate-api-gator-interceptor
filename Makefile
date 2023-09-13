.PHONY: push/gator-api-interceptor
push/gator-api-interceptor: ## Push quay.io/ecosystem-appeng/gator-api-interceptor
	./mvnw  -Dquarkus.container-image.build=true \
	-Dquarkus.container-image.push=true \
	-Dquarkus.container-image.group=ecosystem-appeng \
	-Dquarkus.container-image.name=gator-api-interceptor \
	-Dquarkus.container-image.registry=quay.io \
	install -f pom.xml
