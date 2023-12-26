# k8s-apis-benchmarking
Definir entorno para pruebas de apis en k8s bare metal
<table border="0">
   <tr>
      <td align="center" valign="middle">
         <code><img height="30" src="https://avatars.githubusercontent.com/u/7894478">Gin</code>
      </td>
      <td align="center" valign="middle">
         <h3>VS</h3>
      </td>
      <td align="center" valign="middle">
         <code><img height="30" src="https://raw.githubusercontent.com/github/explore/a92591a79a4ce31660058d7ccc66c79266931f61/topics/dotnet/dotnet.png">ASP.NET_Core</code>
      </td>
      <td align="center" valign="middle">
         <h3>VS</h3>
      </td>
      <td align="center" valign="middle">
         <code><img height="30" src="https://raw.githubusercontent.com/github/explore/80688e429a7d4ef2fca1e82350fe8e3517d3494d/topics/spring-boot/spring-boot.png">Spring</code>
      </td>
      <td align="center" valign="middle">
         <h3>VS</h3>
      </td>
      <td align="center" valign="middle">
         <code><img height="30" src="https://avatars.githubusercontent.com/u/47638783">Quarkus</code>
      </td>
   </tr>
</table>

Proposito:
    ![Diagram](docs/k8s-apis-benchmarking-dark.png#gh-dark-mode-only)
    ![Diagram](docs/k8s-apis-benchmarking-white.png#gh-light-mode-only)

## Ejecutar apps desde codigo fuente
1. **Comandos para ejecutar las apps.**
   ```bash
   cat run.sh
   # cd src/dotnet-app
   #     > dotnet restore
   #     > dotnet build
   #     > dotnet run

   # cd src/sprint-app 
   #     > mvn package
   #     > mvn install / mvn clean install
   #     > mvn spring-boot:run
      
   #     > mvn clean package spring-boot:repackage
   #     > java -jar ./target/spring-app-0.0.1-SNAPSHOT.jar

   # cd src/go-app
   #     > go get -d .
   #     > go build
   #     > go run .
    ```
## Contruccion de imagenes de los apps

1. **Comandos para crear y subir las imágenes de las apps a docker hub.**
   ```bash
   #Crear imagenes por cada app
   chmod +x build.sh
   export USERNAMR=eduyupanqui VER=v1 APP_DIR=dotnet-app && ./build.sh
   export USERNAMR=eduyupanqui VER=v1 APP_DIR=go-app && ./build.sh
   export USERNAMR=eduyupanqui VER=v1 APP_DIR=spring-app && ./build.sh

   # test client
   export USERNAMR=eduyupanqui VER=v1 APP_DIR=client-app && ./build.sh
    ```
2. **Validar contenedores per app en local.**
   ```bash
   #Levanta las dependencias de las aplicaciones: minio, postgres, prometheus, jaeger
   docker compose up -d

   # Run dotnet-app
   docker run --rm -it --network=local-network -p 8000:8000 -p 8081:8081 -v ./deploy/local/config.yaml:/app/config.yaml eduyupanqui/dotnet-app:v1
   # Run go-app
   docker run --rm -it --network=local-network -p 8000:8000 -p 8081:8081 -v ./deploy/local/config.yaml:/config.yaml eduyupanqui/go-app:v1
   # Run spring-app
   docker run --rm -it --network=local-network -p 8000:8000 -p 8081:8081 -v ./deploy/local/config.yaml:/config/application.yml eduyupanqui/spring-app:v1
    ```

## Aprovisionar dependencias en kubernetes
1. **Ejecutar recetas de terraform.**

    Esto aprovisionara las dependencias en el cluster k8s.
    ```bash
   cd terraform
   # 0-providers.tf
   # 1-prometheus-operator-crds.tf
   # 2-prometheus.tf
   # 3-grafana.tf
   # 4-tempo.tf
   # 5-minio.tf
   # 6-postgres.tf
   terraform init
   terraform apply
    ```

2. **Subir imagen de prueba a minio.**
   ```bash
   chmod +x minio-upload.sh
   ./minio-upload images ./minio/images/thumbnail.png
    ```
3. **Ejecutar script en postgres.**
   ```bash
   kubectl exec -it [pod-name] --  psql -h localhost -U admin --password -p 5432 postgresdb
   ```
   ```sql
   CREATE DATABASE mydb;
   \c mydb
   CREATE TABLE public.go_image (
      id uuid NOT NULL,
      lastmodified date NOT NULL
   );
    ```


## Desplegar servicios a k8s
1. **Definir node affinity para las apps.**
   ```bash
   kubectl label nodes kub-2 service=golang
   kubectl label nodes kub-3 service=dotnet
   kubectl label nodes kub-4 service=spring
    ```
2. **Creacion de namespaces para las apps.**
   ```bash
   kubectl create namespace golang
   kubectl create namespace dotnet
   kubectl create namespace spring
    ```
3. **Desplegar las apps a k8s.**
   ```bash
   cd deploy/k8s/go-app
   kubectl apply -f '*.yaml'
   cd ....
   cd deploy/k8s/dotnet-app
   kubectl apply -f '*.yaml'
   cd ....
   cd deploy/k8s/spring-app
   kubectl apply -f '*.yaml'
   ```
4. **Test las apis con K6.**
    ```bash
   cd tests/k6
   k6 run script_dotnet.js
   k6 run script_go.js
   k6 run script_spring.js
   ```
## Desplegar clients para testing a k8s
1. **Definir node affinity para los client-test.**
   ```bash
   kubectl label nodes kub-2 job=spring-client
   kubectl label nodes kub-4 job=golang-client
   kubectl label nodes kub-5 job=dotnet-client
    ```
2. **Desplegar los client tests a k8s.**
   
   <table border="0">
      <tr>
         <td align="center" valign="middle">
            <code><img height="30" src="https://raw.githubusercontent.com/github/explore/a92591a79a4ce31660058d7ccc66c79266931f61/topics/dotnet/dotnet.png">ASP.NET_Core</code>
         </td>
         <td align="center" valign="middle">
            <h5>VS</h5>
         </td>
         <td align="center" valign="middle">
            <code><img height="30" src="https://avatars.githubusercontent.com/u/7894478">Gin</code>
         </td>
      </tr>
   </table>

   ```bash
   # test-client-1
   # dotnet vs go 
   # endpoint: api/devices
   cd tests/k8s/1-test
   kubectl apply -f '*.yaml'
   ```
   <table border="0">
      <tr>
         <td align="center" valign="middle">
            <code><img height="30" src="https://raw.githubusercontent.com/github/explore/a92591a79a4ce31660058d7ccc66c79266931f61/topics/dotnet/dotnet.png">ASP.NET_Core</code>
         </td>
         <td align="center" valign="middle">
            <h5>VS</h5>
         </td>
         <td align="center" valign="middle">
            <code><img height="30" src="https://raw.githubusercontent.com/github/explore/80688e429a7d4ef2fca1e82350fe8e3517d3494d/topics/spring-boot/spring-boot.png">Spring</code>
         </td>
      </tr>
   </table>

   ```bash
   # test-client-1 
   # dotnet vs spring 
   # endpoint: api/devices
   cd tests/k8s/2-test
   kubectl apply -f '*.yaml'
   ```
   <table border="0">
      <tr>
         <td align="center" valign="middle">
            <code><img height="30" src="https://raw.githubusercontent.com/github/explore/a92591a79a4ce31660058d7ccc66c79266931f61/topics/dotnet/dotnet.png">ASP.NET_Core</code>
         </td>
         <td align="center" valign="middle">
            <h5>VS</h5>
         </td>
         <td align="center" valign="middle">
            <code><img height="30" src="https://avatars.githubusercontent.com/u/7894478">Gin</code>
         </td>
      </tr>
   </table>

   ```bash
   # test-client-2 
   # dotnet vs go 
   # endpoint: api/images
   cd tests/k8s/3-test
   kubectl apply -f '*.yaml'
   ```
   <table border="0">
      <tr>
         <td align="center" valign="middle">
            <code><img height="30" src="https://raw.githubusercontent.com/github/explore/a92591a79a4ce31660058d7ccc66c79266931f61/topics/dotnet/dotnet.png">ASP.NET_Core</code>
         </td>
         <td align="center" valign="middle">
            <h5>VS</h5>
         </td>
         <td align="center" valign="middle">
            <code><img height="30" src="https://raw.githubusercontent.com/github/explore/80688e429a7d4ef2fca1e82350fe8e3517d3494d/topics/spring-boot/spring-boot.png">Spring</code>
         </td>
      </tr>
   </table>

   ```bash
   # test-client-2 
   # dotnet vs spring 
   # endpoint: api/images
   cd tests/k8s/4-test
   kubectl apply -f '*.yaml'
   ```
