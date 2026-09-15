# Product Catalogue

## Project overview

This Spring Boot REST API manages a product catalogue and demonstrates versioned delivery, local Docker images, Kubernetes deployment, autoscaling, NGINX Ingress, Git releases, and CI validation.

Docker images are built and used locally for this implementation. No registry or image push is required.

## Features

- v1.0 health and product-list endpoints
- v1.1 keyword search
- v2 paginated search with validation
- Self-contained H2 database with sample products
- Multi-stage, non-root Docker image
- Three Kubernetes namespaces, Deployments, Services, HPAs, and Ingress routes
- GitHub Actions test, build, and local image smoke test

## Technology stack and requirements

Java 17+, Spring Boot 2.7.18, Spring Web, Spring Data JPA, H2, Maven, Docker Desktop, kubectl, and Minikube.

## Running locally

```powershell
mvn spring-boot:run
Invoke-WebRequest http://localhost:8080/v1/health
```

The default port is `8080`; set `SERVER_PORT` to change it.

## API endpoints

| Version | Endpoint | Purpose |
| --- | --- | --- |
| v1.0 | `GET /v1/health` | Health and version response |
| v1.0 | `GET /v1/products` | List products |
| v1.1 | `GET /v1.1/health` | Health response |
| v1.1 | `GET /v1.1/products` | List products |
| v1.1 | `GET /v1.1/products/search?keyword=laptop` | Keyword search |
| v2.0 | `GET /v2/health` | Health response |
| v2.0 | `GET /v2/products` | List products |
| v2.0 | `GET /v2/products/search?keyword=laptop&page=0&size=10` | Paginated search |

For v2, `keyword` is required, `page` must be zero or greater, and `size` must be from 1 through 100. Invalid values return HTTP 400.

## Testing

```powershell
mvn test
```

The tests cover startup, health, products, v1.1 search, v2 pagination, and v2 validation.

## Docker

The Dockerfile uses Maven only in the build stage and Eclipse Temurin JRE Alpine in the runtime stage. The runtime uses a non-root user and includes a container health check.

```powershell
docker build -t product-catalogue:local .
docker run --rm --name product-catalogue --publish 8080:8080 product-catalogue:local
docker images product-catalogue
docker ps
```

Docker images are kept on the local Docker engine and are never pushed.

Build the release images from their release tags:

```powershell
git checkout v1.0.0
docker build -t product-catalogue:v1.0.0 .
git checkout v1.1.0
docker build -t product-catalogue:v1.1.0 .
git checkout v2.0.0
docker build -t product-catalogue:v2.0.0 .
git checkout main
```

## Minikube deployment

```powershell
docker version
kubectl version --client
minikube version
minikube start
kubectl get nodes
minikube addons enable ingress
minikube addons enable metrics-server
```

Load the local images into Minikube:

```powershell
minikube image load product-catalogue:v1.0.0
minikube image load product-catalogue:v1.1.0
minikube image load product-catalogue:v2.0.0
minikube image ls
```

Apply all resources:

```powershell
kubectl apply -f k8s/namespaces/namespaces.yaml
kubectl apply -f k8s/v1
kubectl apply -f k8s/v1.1
kubectl apply -f k8s/v2
kubectl apply -f k8s/ingress/ingress.yaml
kubectl get pods -A
kubectl get services -A
kubectl get hpa -A
kubectl get ingress -A
```

Each Deployment uses `imagePullPolicy: IfNotPresent`, two initial replicas, CPU and memory requests/limits, and readiness/liveness probes. Requests are 100m CPU and 256Mi memory; limits are 500m CPU and 512Mi memory, which are suitable for a small Minikube node.

The Ingress file contains one Ingress per namespace because Kubernetes Ingress objects cannot directly reference Services in another namespace. Each backend remains namespace-local while NGINX merges the hostless path rules.

Test through NGINX:

```powershell
$minikubeIp = minikube ip
Invoke-WebRequest "http://$minikubeIp/v1/health"
Invoke-WebRequest "http://$minikubeIp/v1/products"
Invoke-WebRequest "http://$minikubeIp/v1.1/health"
Invoke-WebRequest "http://$minikubeIp/v1.1/products/search?keyword=laptop"
Invoke-WebRequest "http://$minikubeIp/v2/health"
Invoke-WebRequest "http://$minikubeIp/v2/products/search?keyword=laptop&page=0&size=10"
```

## Namespaces and HPA

- `product-v1`: v1.0 Deployment, Service, and HPA
- `product-v1-1`: v1.1 Deployment, Service, and HPA
- `product-v2`: v2.0 Deployment, Service, and HPA

Each HPA targets 70% average CPU utilization and scales from two to five replicas. Metrics Server is required:

```powershell
kubectl get hpa -A
kubectl describe hpa -n product-v2 product-v2
```

## Git workflow and releases

The release history is intentionally linear: initial setup, v1.0 implementation, v1.0.0, v1.1 search, v1.1.0, v2 pagination, v2.0.0, then DevOps infrastructure.

Use `develop` for integration and short-lived branches such as `feature/product-search` and `feature/v2-search`. Merge features into `develop`, validate, then merge `develop` into `main` for release.

```powershell
git log --oneline --graph --decorate --all
git branch -a
git tag
git show v1.0.0
git show v1.1.0
git show v2.0.0
```

## CI/CD

`.github/workflows/ci-cd.yml` checks out the code, installs Java 17, runs Maven verification, builds a local Docker image on the GitHub-hosted runner, and smoke-tests the container. It has no registry login, credentials, secret, or push step.

GitHub-hosted runners cannot access Minikube running on this Windows machine. Local Kubernetes deployment is therefore performed manually with the commands above.

## Troubleshooting and final verification

- Build failure: verify Java 17+ and Maven are on `PATH`.
- Image missing in Minikube: rerun `minikube image load` and check `minikube image ls`.
- HPA metrics unavailable: enable Metrics Server and wait for collection.
- Ingress 404: verify the NGINX addon and run `kubectl describe ingress -A`.

```powershell
kubectl get namespaces
kubectl get deployments -A
kubectl get pods -A
kubectl get services -A
kubectl get hpa -A
kubectl get ingress -A
```