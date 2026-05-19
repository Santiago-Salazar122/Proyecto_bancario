# Proyecto Bancario - Sistema de Gestión Bancaria

Sistema bancario desarrollado con Spring Boot, MySQL (XAMPP) y MongoDB.

---

## 🔧 Requisitos previos

| Herramienta | Versión | Descarga |
|---|---|---|
| Java JDK | 17 o superior | https://adoptium.net |
| XAMPP | Cualquier versión reciente | https://www.apachefriends.org |
| MongoDB Community | 6.x o 7.x | https://www.mongodb.com/try/download/community |
| Maven (opcional) | 3.9.x | https://maven.apache.org/download.cgi |

---

## ▶️ Cómo correr el proyecto

### Opción 1 — Con Maven instalado (recomendado)
```bash
# Desde la carpeta raíz del proyecto (donde está el pom.xml):
mvn spring-boot:run
```

### Opción 2 — Con Maven Wrapper en Windows (sin instalación)
```cmd
# Doble clic en mvnw.cmd  O desde la terminal:
.\mvnw.cmd spring-boot:run
```

### Opción 3 — Con Maven Wrapper en Mac/Linux
```bash
./mvnw spring-boot:run
```

### Opción 4 — Desde IntelliJ IDEA o VS Code
1. Abrir la carpeta del proyecto
2. Buscar `BankApplication.java` en `src/main/java/com/bank/`
3. Clic derecho → Run 'BankApplication'

### Opción 5 — Desde Eclipse / Spring Tool Suite (STS)
1. File → Import → Existing Maven Projects
2. Seleccionar la carpeta raíz del proyecto
3. Clic derecho en el proyecto → Run As → Spring Boot App

---

## 🗃️ Configuración de bases de datos

### MySQL (XAMPP)
1. Abrir XAMPP Control Panel
2. Iniciar el servicio **MySQL**
3. La base de datos `banco` se crea automáticamente al arrancar la app

**Credenciales por defecto** (archivo `application.properties`):
```
URL:      localhost:3306/banco
Usuario:  root
Password: (vacío)
```
> Si tu MySQL tiene contraseña, edita `src/main/resources/application.properties`

### MongoDB (Bitácora de Operaciones)
1. Instalar MongoDB Community desde https://www.mongodb.com/try/download/community
2. Iniciar el servicio:
   - **Windows**: Se inicia automáticamente como servicio de Windows
   - **Manual**: correr `mongod` en la terminal
3. La base de datos `banco_audit` se crea sola al primer insert

---

## 🧪 Verificar que funciona

Una vez corriendo, abrir en el navegador o Postman:

```
GET http://localhost:8080/api/v1/users
```
Debe responder: `[]` (lista vacía, eso es correcto)

---

## 📡 Endpoints disponibles

### Usuarios
| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/users` | Crear usuario |
| GET | `/api/v1/users` | Listar todos |
| GET | `/api/v1/users/{id}` | Buscar por ID |
| GET | `/api/v1/users/identification/{id}` | Buscar por CC/NIT |
| PATCH | `/api/v1/users/{id}/status?status=BLOCKED` | Cambiar estado |

### Cuentas bancarias
| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/accounts` | Abrir cuenta |
| GET | `/api/v1/accounts/{accountNumber}` | Consultar cuenta |
| POST | `/api/v1/accounts/{accountNumber}/deposit` | Depositar |
| POST | `/api/v1/accounts/{accountNumber}/withdraw` | Retirar |
| PATCH | `/api/v1/accounts/{accountNumber}/block` | Bloquear |

### Préstamos
| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/loans` | Solicitar préstamo |
| PATCH | `/api/v1/loans/{id}/approve` | Aprobar |
| PATCH | `/api/v1/loans/{id}/reject?analystUserId=X` | Rechazar |
| PATCH | `/api/v1/loans/{id}/disburse` | Desembolsar |

### Transferencias
| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/transfers` | Crear transferencia |
| PATCH | `/api/v1/transfers/{id}/approve?approverUserId=X` | Aprobar |
| PATCH | `/api/v1/transfers/{id}/reject?approverUserId=X` | Rechazar |
| GET | `/api/v1/transfers/pending` | Ver pendientes |

### Bitácora (MongoDB)
| Método | URL | Descripción |
|---|---|---|
| GET | `/api/v1/audit` | Todo el historial |
| GET | `/api/v1/audit/product/{id}` | Historial de un producto |
| GET | `/api/v1/audit/type/{type}` | Filtrar por tipo |

---

## 🏗️ Arquitectura del proyecto

```
com/bank/
 ├── aplicacion/          ← Servicios de aplicación (@Service)
 ├── domain/
 │    ├── enums/          ← Estados y roles del sistema
 │    ├── model/          ← Entidades: User, BankAccount, Loan, Transfer
 │    ├── ports/          ← Interfaces de repositorio (contratos)
 │    ├── service/        ← Lógica de negocio (1 servicio por caso de uso)
 │    └── valueobject/    ← Money, Email, PhoneNumber, Address
 └── infrastructure/
      ├── api/v1/         ← Controllers REST
      └── repositorios/
           ├── jpa/       ← Repositorios MySQL (JpaRepository)
           ├── mongodb/   ← Repositorio MongoDB (MongoRepository)
           └── adapter/   ← Conectan puertos con repositorios
```
