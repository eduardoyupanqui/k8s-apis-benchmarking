# k8s-apis-benchmarking
Definir entorno para pruebas de apis en k8s bare metal
gin vs aspnetcore vs springboot vs quarkus

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

## Aprovisionar dependencias
1. **Ejecutar recetas de terraform.**

    Esto aprovisionara las dependencias en el cluster k8s.
    ```bash
   cd terraform
   terraform init
   terraform apply
    ```