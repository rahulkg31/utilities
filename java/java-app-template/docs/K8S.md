# Kubernetes - Demo App

The application supports three deployment methods:

- **Plain Kubernetes:** `k8s/manifests/`
- **Kustomize:** `k8s/kustomize/`
- **Helm:** `k8s/helm/`

## 1. Prerequisites

```
kubectl get nodes
```

Build the application image:

```
minikube image build -t demo:1.0.0 .
```

Create namespaces if needed:

```
kubectl create namespace demo
kubectl create namespace demo-dev
kubectl create namespace demo-prod
```

## 2. Deploy the Application

### Plain Kubernetes

```
kubectl apply -f k8s/manifests/ -n demo
```

### Kustomize

```
kubectl apply -k k8s/kustomize/overlays/dev
kubectl apply -k k8s/kustomize/overlays/prod
```

### Helm

```
helm install my-demo ./k8s/helm/ -n demo
```

For subsequent Helm updates:

```
helm upgrade my-demo ./k8s/helm/ -n demo
```

## 3. Verify Deployment

```
kubectl get pods -n demo
kubectl get deployments -n demo
kubectl get services -n demo
kubectl rollout status deployment/demo-app -n demo
```

For Kustomize, use `demo-dev` or `demo-prod` as appropriate. For Helm, check the actual Deployment name:

```
kubectl get deployments -n demo
```

## 4. Apply Changes

After changing application code, rebuild the image and update the image tag in the relevant manifest, overlay, or Helm values.

```
minikube image build -t demo:1.0.1 .
```

Apply using the same method used for deployment:

```
kubectl apply -f k8s/manifests/ -n demo
```

Or:

```
kubectl apply -k k8s/kustomize/overlays/dev
```

Or:

```
helm upgrade my-demo ./k8s/helm/ -n demo
```

Check the rollout:

```
kubectl rollout status deployment/<deployment-name> -n demo
```

## 5. Test the Application

Forward port `8080`:

```
kubectl port-forward -n demo deployment/demo-app 8080:8080
```

In another terminal, test health:

```
curl -i http://localhost:8080/actuator/health
```

If Swagger UI is enabled, open:

http://localhost:8080/swagger-ui/index.html

## 6. Troubleshooting

```
kubectl get pods -n demo
kubectl describe pod <pod-name> -n demo
kubectl logs <pod-name> -n demo
```

## 7. Stop or Remove

Temporarily stop the application:

```
kubectl scale deployment/demo-app --replicas=0 -n demo
```

Start it again:

```
kubectl scale deployment/demo-app --replicas=1 -n demo
```

Delete the plain Deployment:

```
kubectl delete deployment demo-app -n demo
```

Uninstall Helm:

```
helm uninstall my-demo -n demo
```

Remove the development Kustomize resources:

```
kubectl delete -k k8s/kustomize/overlays/dev
```

**Note:** Use one deployment method per application per namespace to avoid resource ownership conflicts.
