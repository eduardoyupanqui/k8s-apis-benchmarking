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

## Aprovisionar dependencias
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
4. **Comandos para ejecutar las apps.**
   ```bash
    ```
5. **Comandos para crear las imágenes de las apps.**
   ```bash
    ```
