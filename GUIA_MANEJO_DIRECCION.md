# Guía de Manejo de la Entidad `Dirección` — Patrón PacketWorld API

## 0. Implementación en FitNutrition API (Pacientes y Médicos)

> **Actualización (Commits T404 y T424):** Se ha portado e implementado de forma exitosa el patrón de manejo de direcciones para el dominio específico de FitNutrition, enfocándose en las entidades **Paciente** y **Médico**.

A continuación se detalla la lógica de negocio y arquitectura implementada en los endpoints de registro y edición para estos commits:

### 0.1. Módulo Independiente de Domicilio
- Se implementó `ws.DireccionWS` y `dominio.DireccionImp` exponiendo los endpoints `/api/direccion/crear-direccion`, `/api/direccion/editar` y `/api/direccion/obtener-direccion-id/{idDireccion}`.
- Se configuró el `DireccionMapper.xml` para realizar `INSERT`, `UPDATE` y `DELETE` sobre la tabla `domicilio`. El `INSERT` utiliza `useGeneratedKeys="true"` para devolver el ID autogenerado.
- La clase `dto.Respuesta` fue modificada para incluir un atributo genérico `Object valor`, permitiendo devolver al frontend el `idDireccion` (`id_domicilio`) tras una inserción exitosa.

### 0.2. Actualización de Entidades (POJOs)
Las clases `pojo.Paciente` y `pojo.Medico` fueron extendidas para incluir campos de solo visualización correspondientes al domicilio (no persistidos en la tabla principal de la entidad):
- `calle`, `numero`, `idColonia`, `nombreColonia`, `codigoPostal`, `ciudad`, `estado`, `direccionCompleta`.

### 0.3. Inclusión en Consultas de Base de Datos (Mappers)
Se modificaron los mappers `PacienteMapper.xml` y `MedicoMapper.xml` (en operaciones como `obtenerPorId`, listados, etc.) para incluir múltiples `LEFT JOIN` con las tablas del catálogo geográfico:
```xml
LEFT JOIN domicilio d ON p.id_domicilio = d.id_domicilio
LEFT JOIN colonias c ON d.id_colonia = c.id
LEFT JOIN municipios m ON c.municipio = m.id
LEFT JOIN estados e ON m.estado = e.id
```
Con esto, se ensambla la propiedad `direccionCompleta` al vuelo usando la función `CONCAT_WS()` de MySQL.

### 0.4. Validaciones Estrictas en Endpoints (Web Services)
En los Web Services principales (`PacienteWS` y `MedicoWS`), se agregaron validaciones de negocio en los métodos `POST` (registro) y `PUT` (edición). Es obligatorio que el cliente asigne un `idDomicilio` válido:
```java
if (entidad.getIdDomicilio() == null || entidad.getIdDomicilio() <= 0) {
    throw new BadRequestException("La dirección es obligatoria");
}
```
Esto refuerza el flujo de dos pasos: **1)** Crear el domicilio -> **2)** Registrar o actualizar el Paciente/Médico con el ID previamente obtenido.

---

> **Aviso importante para quien lea este documento:**
> Este archivo documenta el *patrón de negocio* del proyecto **PacketWorld API**. Las tablas, nombres de columnas y entidades aquí mencionadas son **específicas de este proyecto**. Si vas a replicar este patrón en otro proyecto, adapta los nombres a tu propio esquema. Lo que sí aplica directamente es la **lógica de flujo** y la **estrategia de separación de responsabilidades**.

---

## 1. Concepto Central: ¿Por qué la Dirección es una Entidad Independiente?

La dirección **NO se registra junto con la entidad que la posee** (Cliente, Sucursal, etc.). Esta decisión es de **lógica de negocio** y se basa en:

1. **Separación de responsabilidades**: El módulo de dirección gestiona exclusivamente datos de ubicación.
2. **Flujo de dos pasos**: Primero se crea la dirección → se obtiene su ID generado → ese ID se pasa a la entidad principal.
3. **Catálogo geográfico normalizado**: Los datos como Colonia, Ciudad, Municipio y Estado **no se escriben manualmente**. Existe un catálogo (`colonias`, `municipios`, `estados`) del que el usuario selecciona. Solo se almacena `idColonia`; el resto se resuelve con JOINs.

---

## 2. Esquema de Tablas (Este Proyecto — Adaptar al tuyo)

> ⚠️ Las tablas siguientes son de PacketWorld. Adáptalas a tu esquema.

```
DIRECCION
├── idDireccion   (PK, auto-increment)
├── calle         (varchar)
├── numero        (varchar)
└── idColonia     (FK → COLONIAS.id)

COLONIAS
├── id, nombre, codigo_postal
└── municipio     (FK → MUNICIPIOS.id)

MUNICIPIOS
├── id, nombre
└── estado        (FK → ESTADOS.id)

ESTADOS
└── id, nombre

CLIENTE
├── idCliente, nombre, ...
└── idDireccion   (FK → DIRECCION.idDireccion)

SUCURSAL
├── idSucursal, nombre, ...
└── idDireccion   (FK → DIRECCION.idDireccion)
```

**Punto clave**: La tabla `DIRECCION` solo almacena `calle`, `numero` e `idColonia`. Todo lo demás se obtiene dinámicamente con JOINs.

---

## 3. Componentes del Sistema

| Capa | Clase | Responsabilidad |
|---|---|---|
| **POJO** | `pojo.Direccion` | Modelo de datos de la entidad |
| **Web Service** | `ws.DireccionWS` | Endpoints REST (JAX-RS) |
| **Dominio** | `dominio.DireccionImp` | Lógica de negocio + acceso a BD |
| **Mapper XML** | `DireccionMapper.xml` | Consultas SQL (MyBatis) |
| **DTO** | `dto.Respuesta` | Objeto de respuesta estándar `{error, mensaje, valor}` |

Módulos que **consumen** la dirección:
- `ws.ClienteWS` / `dominio.ClienteImp` / `ClienteMapper.xml`
- `ws.SucursalWS` / `dominio.SucursalImp` / `SucursalMapper.xml`

---

## 4. El POJO `Direccion`

```java
public class Direccion {
    Integer idDireccion;   // PK, se popula automáticamente tras INSERT
    String  calle;         // Se persiste en BD
    String  numero;        // Se persiste en BD
    Integer idColonia;     // FK que se persiste en BD
    // --- Campos de solo visualización (no se insertan, vienen de JOINs) ---
    Integer codigoPostal;
    String  colonia;
    String  ciudad;
    String  municipio;
    String  estado;
    // getters y setters...
}
```

**Campos persistidos**: `calle`, `numero`, `idColonia`.  
**Campos de solo lectura** (populados por SELECT con JOINs): `codigoPostal`, `colonia`, `ciudad`, `municipio`, `estado`.

---

## 5. INSERCIÓN de Dirección

### 5.1 Flujo de dos pasos (el frontend maneja la secuencia)

```
1. GET  /direccion/obtener-direccion-codigo-postal/{cp}
         → Devuelve lista de colonias disponibles para ese CP
         → El usuario elige una colonia (obtiene su idColonia)

2. POST /direccion/crear-direccion
         Body: { "calle": "Av. Juárez", "numero": "123", "idColonia": 45 }
         → Respuesta: { "error": false, "mensaje": "Dirección guardada", "valor": "87" }
                                                                           ↑ idDireccion generado

3. POST /cliente/registrar   (o /sucursal/registrar)
         Body: { "nombre": "...", ..., "idDireccion": 87 }
```

### 5.2 Web Service

```java
// ws/DireccionWS.java
@Path("crear-direccion")
@POST
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public Respuesta crearDireccion(String json) {
    Gson gson = new Gson();
    Direccion direccion = gson.fromJson(json, Direccion.class);
    if (Validaciones.esVacio(direccion.getCalle()))
        throw new BadRequestException("La calle es obligatoria");
    if (direccion.getIdColonia() == null)
        throw new BadRequestException("El idColonia es obligatorio");
    return DireccionImp.crearDireccion(direccion);
}
```

### 5.3 Dominio

```java
// dominio/DireccionImp.java
public static Respuesta crearDireccion(Direccion direccion) {
    Respuesta respuesta = new Respuesta();
    respuesta.setError(true);
    SqlSession conexionBD = MyBatisUtil.getSession();
    if (conexionBD != null) {
        try {
            int filasAfectadas = conexionBD.insert("direccion.crear-direccion", direccion);
            conexionBD.commit();
            if (filasAfectadas == 1) {
                respuesta.setError(false);
                respuesta.setMensaje("Dirección guardada");
                // MyBatis inyectó el ID generado en el objeto: direccion.getIdDireccion()
                respuesta.setValor(String.valueOf(direccion.getIdDireccion()));
            }
        } catch (Exception e) {
            respuesta.setMensaje(e.getMessage());
        } finally {
            conexionBD.close();
        }
    }
    return respuesta;
}
```

### 5.4 Mapper XML

```xml
<!-- DireccionMapper.xml -->
<insert id="crear-direccion"
        parameterType="pojo.Direccion"
        useGeneratedKeys="true"
        keyProperty="idDireccion">
    INSERT INTO direccion(calle, numero, idColonia)
    VALUES (#{calle}, #{numero}, #{idColonia})
</insert>
```

> **`useGeneratedKeys="true"` + `keyProperty="idDireccion"`**: MyBatis escribe automáticamente el ID generado por la BD de regreso en el atributo `idDireccion` del objeto POJO. Esto es lo que permite retornar el ID al cliente en `Respuesta.valor`.

---

## 6. ACTUALIZACIÓN de Dirección

La actualización de la dirección es **completamente independiente** de la entidad dueña. Si un cliente cambia su dirección, se llama a `PUT /direccion/editar` por separado. Editar al cliente (nombre, teléfono, etc.) **no toca la dirección**.

### 6.1 Endpoint

```
PUT /api/direccion/editar
Content-Type: application/json

Body: { "idDireccion": 87, "calle": "Calle Reforma", "numero": "500", "idColonia": 62 }

Respuesta: { "error": false, "mensaje": "Información de la dirección actualizada." }
```

### 6.2 Web Service

```java
@Path("editar")
@PUT
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Respuesta editar(String json) {
    Gson gson = new Gson();
    Direccion direccion = gson.fromJson(json, Direccion.class);
    return DireccionImp.editar(direccion);
}
```

### 6.3 Dominio

```java
public static Respuesta editar(Direccion direccion) {
    Respuesta respuesta = new Respuesta();
    respuesta.setError(true);
    SqlSession conexionBD = MyBatisUtil.getSession();
    if (conexionBD != null) {
        try {
            int filasAfectadas = conexionBD.update("direccion.editar", direccion);
            conexionBD.commit();
            if (filasAfectadas > 0) {
                respuesta.setError(false);
                respuesta.setMensaje("Información de la dirección actualizada.");
            } else {
                respuesta.setMensaje("No se pudo actualizar la dirección.");
            }
        } catch (Exception e) {
            respuesta.setMensaje(e.getMessage());
        } finally {
            conexionBD.close();
        }
    }
    return respuesta;
}
```

### 6.4 Mapper XML

```xml
<update id="editar" parameterType="pojo.Direccion">
    UPDATE direccion
    SET calle = #{calle}, numero = #{numero}, idColonia = #{idColonia}
    WHERE idDireccion = #{idDireccion}
</update>
```

---

## 7. ELIMINACIÓN de Dirección

La eliminación **no tiene endpoint REST público**. Existe el método `DireccionImp.eliminar()` para uso interno del sistema, para coordinarse con la eliminación de la entidad dueña si así se requiere.

```java
// dominio/DireccionImp.java
public static boolean eliminar(Integer idDireccion) {
    if (idDireccion == null || idDireccion <= 0) return false;
    SqlSession conexionBD = MyBatisUtil.getSession();
    if (conexionBD != null) {
        try {
            int filas = conexionBD.delete("direccion.eliminar", idDireccion);
            conexionBD.commit();
            return filas > 0;
        } catch (Exception e) {
            conexionBD.rollback(); // rollback explícito
            return false;
        } finally {
            conexionBD.close();
        }
    }
    return false;
}
```

```xml
<delete id="eliminar" parameterType="int">
    DELETE FROM DIRECCION WHERE idDireccion = #{value}
</delete>
```

**Diferencias respecto a crear/editar**:
- Devuelve `boolean`, no `Respuesta` — es un método auxiliar interno.
- Incluye `rollback` explícito en el `catch`.
- Valida el ID **antes** de abrir conexión.

---

## 8. CONSULTA de Dirección

### 8.1 Colonias por Código Postal (catálogo — se usa antes de crear/editar)

```
GET /api/direccion/obtener-direccion-codigo-postal/06600
```

```json
[
  { "idColonia": 45, "colonia": "Juárez", "ciudad": "CdMx", "municipio": "Cuauhtémoc", "estado": "CDMX", "codigoPostal": 6600 },
  { "idColonia": 46, "colonia": "Tabacalera", ... }
]
```

```xml
<select id="obtener-colonias-codigo_postal" resultType="pojo.Direccion" parameterType="int">
    SELECT c.id as idColonia, c.nombre as colonia, c.ciudad,
           m.nombre as municipio, e.nombre as estado, c.codigo_postal as codigoPostal
    FROM colonias c
    INNER JOIN municipios m ON m.id = c.municipio
    INNER JOIN estados e ON e.id = m.estado
    WHERE c.codigo_postal = #{codigoPostal}
</select>
```

### 8.2 Dirección completa por ID

```
GET /api/direccion/obtener-direccion-id/87
```

```xml
<select id="obtener-direccion-id" resultType="pojo.Direccion" parameterType="int">
    SELECT d.idDireccion, d.calle, d.numero,
           c.id AS idColonia, c.nombre AS colonia, c.ciudad,
           m.nombre AS municipio, e.nombre AS estado, c.codigo_postal AS codigoPostal
    FROM DIRECCION d
    INNER JOIN COLONIAS c ON d.idColonia = c.id
    INNER JOIN MUNICIPIOS m ON c.municipio = m.id
    INNER JOIN ESTADOS e ON m.estado = e.id
    WHERE d.idDireccion = #{idDireccion}
</select>
```

---

## 9. Cómo las Entidades Consumen la Dirección

### 9.1 Al REGISTRAR (ejemplo con Cliente)

El `idDireccion` (obtenido en el Paso 2 del flujo) se incluye en el payload del cliente:

```json
{
    "nombre": "Ana García",
    "apellidoPaterno": "García",
    "apellidoMaterno": "López",
    "telefono": "5512345678",
    "correo": "ana@email.com",
    "idDireccion": 87
}
```

**Validación en el WS** — `idDireccion` es obligatorio:
```java
if (cliente.getIdDireccion() == null || cliente.getIdDireccion() <= 0)
    throw new BadRequestException("El idDireccion es obligatorio");
```

**Mapper INSERT del cliente** (solo guarda la FK):
```xml
<insert id="registrar-cliente" parameterType="pojo.Cliente">
    INSERT INTO cliente (nombre, apellidoPaterno, apellidoMaterno, telefono, correo, idDireccion)
    VALUES (#{nombre}, #{apellidoPaterno}, #{apellidoMaterno}, #{telefono}, #{correo}, #{idDireccion})
</insert>
```

### 9.2 Al MOSTRAR (SELECT con JOINs en el mapper del cliente)

El POJO `Cliente` tiene **dos tipos de campos**:

```java
// pojo/Cliente.java
private Integer idDireccion;       // FK guardada en tabla cliente
private Integer idColonia;
// Campos de visualización (no existen en tabla cliente, los llena el SELECT):
private String direccionCompleta;
private String calle;
private String numero;
private String nombreColonia;
private String codigoPostal;
private String ciudad;
private String estado;
```

El SELECT del cliente hace JOINs para reconstruir la dirección completa:

```xml
<select id="obtener-clientes" resultType="pojo.Cliente">
    SELECT cl.idCliente, cl.nombre, cl.apellidoPaterno, cl.apellidoMaterno,
           cl.telefono, cl.correo, cl.idDireccion,
           d.calle, d.numero, d.idColonia,
           col.nombre AS nombreColonia,
           col.codigo_postal AS codigoPostal,
           m.nombre AS ciudad,
           e.nombre AS estado,
           CONCAT_WS(', ',
               CONCAT_WS(' ', d.calle, d.numero),
               NULLIF(col.nombre, ''),
               CONCAT('C.P. ', NULLIF(col.codigo_postal, '')),
               NULLIF(m.nombre, ''),
               NULLIF(e.nombre, '')
           ) AS direccionCompleta
    FROM cliente cl
    INNER JOIN direccion d ON cl.idDireccion = d.idDireccion
    LEFT JOIN colonias col ON d.idColonia = col.id
    LEFT JOIN municipios m ON col.municipio = m.id
    LEFT JOIN estados e ON m.estado = e.id
</select>
```

### 9.3 Al EDITAR entidad dueña — la dirección NO se toca

```xml
<!-- editar-cliente NO modifica idDireccion ni datos de dirección -->
<update id="editar-cliente" parameterType="pojo.Cliente">
    UPDATE cliente SET
        nombre = #{nombre},
        apellidoPaterno = #{apellidoPaterno},
        apellidoMaterno = #{apellidoMaterno},
        telefono = #{telefono},
        correo = #{correo}
    WHERE idCliente = #{idCliente}
</update>
```

---

## 10. Resumen de Endpoints de `DireccionWS`

| Método | URL | Descripción | Cuándo se usa |
|---|---|---|---|
| `GET` | `/direccion/obtener-direccion-codigo-postal/{cp}` | Lista colonias para un CP | Antes de crear/editar dirección |
| `GET` | `/direccion/obtener-direccion-id/{id}` | Datos completos de una dirección | Para mostrar o precargar formulario |
| `GET` | `/direccion/obtener-cp-sucursal/{idSucursal}` | CP de la dirección de una sucursal | Módulo de envíos |
| `POST` | `/direccion/crear-direccion` | Crea nueva dirección | **Siempre antes** de registrar cliente/sucursal |
| `PUT` | `/direccion/editar` | Actualiza calle, número o colonia | Edición independiente de la dirección |
| *(interno)* | — | `DireccionImp.eliminar()` | Solo uso interno del servidor |

---

## 11. Diagrama del Flujo de Registro

```
Frontend                     DireccionWS              ClienteWS
   |                              |                       |
   |-- GET colonias por CP ------>|                       |
   |<-- [ {idColonia, colonia} ]--|                       |
   |  (usuario elige colonia)     |                       |
   |                              |                       |
   |-- POST crear-direccion ----->|                       |
   |   {calle, numero, idColonia} |                       |
   |<-- {error:false, valor:"87"} |                       |
   |        (idDireccion = 87)    |                       |
   |                              |                       |
   |-- POST registrar cliente ---------------------------->|
   |   {nombre,..., idDireccion:87}                       |
   |<-- {error:false, mensaje:"Cliente registrado"} -------|
```

---

## 12. Puntos Clave para Replicar Este Patrón

1. **Crea un módulo de dirección independiente** con su propio WS, Imp y Mapper XML.
2. **La dirección se inserta en un paso previo** a la entidad dueña. El frontend maneja esta secuencia de dos llamadas.
3. **Solo se persisten los campos propios** (`calle`, `numero`) y la FK al catálogo (`idColonia`). Los datos geográficos derivados se resuelven con JOINs.
4. **El POJO tiene dos tipos de campos**: los que se persisten y los de solo visualización. Sepáralos claramente.
5. **Usa `useGeneratedKeys="true"` + `keyProperty`** en el INSERT de MyBatis para que el ID generado se inyecte automáticamente en el POJO y puedas retornarlo al cliente via `Respuesta.valor`.
6. **La actualización de la entidad dueña NO actualiza la dirección**. La edición de dirección siempre es un llamado separado a `PUT /direccion/editar`.
7. **No expongas el DELETE de dirección** como endpoint público para evitar inconsistencias referenciales. Úsalo solo internamente si es necesario.
8. **Al mostrar entidades**, usa JOINs en el mapper de la entidad (Cliente, Sucursal) para reconstruir la dirección completa. No almacenes datos geográficos redundantes en la tabla de la entidad.
9. **`idDireccion` es campo obligatorio** al registrar cualquier entidad que use dirección — valídalo en el WS antes de pasar al dominio.
10. **El catálogo geográfico es de solo lectura** desde la perspectiva del usuario. Los endpoints de dirección nunca modifican las tablas `colonias`, `municipios` o `estados`.

---

## 13. Componentes y Flujo Detallado por Operación

Esta sección es el mapa completo de **qué archivos/clases participan y en qué orden** para cada operación sobre la dirección.

---

### 13.1 Flujo de INSERCIÓN (Registro de Dirección)

**Componentes involucrados:**

| # | Componente | Archivo | Acción |
|---|---|---|---|
| 1 | Frontend | (cliente HTTP) | Envía `POST` con JSON `{calle, numero, idColonia}` |
| 2 | `ws.DireccionWS` | `DireccionWS.java` | Recibe la petición, deserializa JSON → `Direccion`, valida campos obligatorios |
| 3 | `pojo.Direccion` | `Direccion.java` | Es el objeto que transporta los datos entre todas las capas |
| 4 | `dominio.DireccionImp` | `DireccionImp.java` | Abre sesión MyBatis, ejecuta el INSERT, hace commit |
| 5 | `modelo.mybatis.MyBatisUtil` | `MyBatisUtil.java` | Proporciona la `SqlSession` (conexión a BD del pool) |
| 6 | `DireccionMapper.xml` | `DireccionMapper.xml` | Contiene el SQL `INSERT INTO direccion(calle, numero, idColonia)` con `useGeneratedKeys` |
| 7 | **Base de Datos** | tabla `DIRECCION` | Ejecuta el INSERT, genera `idDireccion` automáticamente |
| 8 | `dto.Respuesta` | `Respuesta.java` | Empaqueta el resultado: `{error, mensaje, valor}` donde `valor` = `idDireccion` generado |
| 9 | Frontend | (cliente HTTP) | Recibe la respuesta y **guarda el `valor` (idDireccion)** para el siguiente paso |

**Flujo paso a paso:**

```
Frontend
  │
  ├─1─▶ POST /api/direccion/crear-direccion
  │      Body: { "calle": "Av. Juárez", "numero": "10", "idColonia": 45 }
  │
  ▼
ws.DireccionWS → crearDireccion(String json)
  │
  ├─2─▶ Gson.fromJson(json, Direccion.class)   →  objeto pojo.Direccion populado
  ├─3─▶ Validaciones.esVacio(direccion.getCalle())  — lanza 400 si falla
  ├─4─▶ direccion.getIdColonia() == null            — lanza 400 si falla
  └─5─▶ llama DireccionImp.crearDireccion(direccion)
          │
          ├─6─▶ MyBatisUtil.getSession()  →  SqlSession (conexión del pool)
          ├─7─▶ conexionBD.insert("direccion.crear-direccion", direccion)
          │       │
          │       └─▶ DireccionMapper.xml ejecuta:
          │            INSERT INTO direccion(calle, numero, idColonia)
          │            VALUES ("Av. Juárez", "10", 45)
          │            ← BD genera idDireccion = 87
          │            ← MyBatis inyecta 87 en direccion.idDireccion (useGeneratedKeys)
          │
          ├─8─▶ conexionBD.commit()
          └─9─▶ respuesta.setValor("87")   ←  idDireccion disponible para el frontend
                 return Respuesta{error:false, mensaje:"Dirección guardada", valor:"87"}

Frontend
  └─10─▶ Guarda idDireccion = 87 → lo usa en el POST siguiente (registrar cliente/sucursal)
```

---

### 13.2 Flujo de EDICIÓN (Actualización de Dirección)

**Componentes involucrados:**

| # | Componente | Archivo | Acción |
|---|---|---|---|
| 1 | Frontend | (cliente HTTP) | Envía `PUT` con JSON `{idDireccion, calle, numero, idColonia}` |
| 2 | `ws.DireccionWS` | `DireccionWS.java` | Recibe la petición, deserializa JSON → `Direccion` |
| 3 | `pojo.Direccion` | `Direccion.java` | Transporta todos los campos: `idDireccion` (para el WHERE), `calle`, `numero`, `idColonia` |
| 4 | `dominio.DireccionImp` | `DireccionImp.java` | Abre sesión MyBatis, ejecuta el UPDATE, hace commit |
| 5 | `modelo.mybatis.MyBatisUtil` | `MyBatisUtil.java` | Proporciona la `SqlSession` |
| 6 | `DireccionMapper.xml` | `DireccionMapper.xml` | Contiene el SQL `UPDATE direccion SET ... WHERE idDireccion = ?` |
| 7 | **Base de Datos** | tabla `DIRECCION` | Ejecuta el UPDATE en el registro correspondiente |
| 8 | `dto.Respuesta` | `Respuesta.java` | Empaqueta resultado: `{error:false, mensaje:"Información de la dirección actualizada."}` |

**Flujo paso a paso:**

```
Frontend
  │
  ├─1─▶ PUT /api/direccion/editar
  │      Body: { "idDireccion": 87, "calle": "Calle Nueva", "numero": "500", "idColonia": 62 }
  │
  ▼
ws.DireccionWS → editar(String json)
  │
  ├─2─▶ Gson.fromJson(json, Direccion.class)  →  pojo.Direccion con todos los campos
  └─3─▶ llama DireccionImp.editar(direccion)
          │
          ├─4─▶ MyBatisUtil.getSession()  →  SqlSession
          ├─5─▶ conexionBD.update("direccion.editar", direccion)
          │       │
          │       └─▶ DireccionMapper.xml ejecuta:
          │            UPDATE direccion
          │            SET calle="Calle Nueva", numero="500", idColonia=62
          │            WHERE idDireccion = 87
          │
          ├─6─▶ conexionBD.commit()
          └─7─▶ return Respuesta{error:false, mensaje:"Información de la dirección actualizada."}

Frontend
  └─8─▶ Muestra confirmación al usuario
```

> ⚠️ **Importante**: Si el usuario también quiere editar sus datos personales (nombre, teléfono), eso es **otra llamada separada** a `PUT /cliente/editar`. Los componentes `ClienteWS` → `ClienteImp` → `ClienteMapper.xml` manejan esa operación **sin tocar la tabla `DIRECCION`**.

---

### 13.3 Flujo de ELIMINACIÓN (Dirección — Uso Interno)

**Componentes involucrados:**

| # | Componente | Archivo | Acción |
|---|---|---|---|
| 1 | Código interno del servidor | (llamada Java directa) | Llama a `DireccionImp.eliminar(idDireccion)` — **NO hay endpoint REST** |
| 2 | `dominio.DireccionImp` | `DireccionImp.java` | Valida el ID, abre sesión, ejecuta DELETE, hace commit o rollback |
| 3 | `modelo.mybatis.MyBatisUtil` | `MyBatisUtil.java` | Proporciona la `SqlSession` |
| 4 | `DireccionMapper.xml` | `DireccionMapper.xml` | Contiene el SQL `DELETE FROM DIRECCION WHERE idDireccion = ?` |
| 5 | **Base de Datos** | tabla `DIRECCION` | Elimina el registro si no tiene restricciones de FK activas |

**Flujo paso a paso:**

```
Código interno (ej. lógica de negocio al eliminar una entidad relacionada)
  │
  ├─1─▶ DireccionImp.eliminar(87)
          │
          ├─2─▶ Valida: idDireccion != null && idDireccion > 0
          │      Si no: retorna false inmediatamente (sin abrir BD)
          │
          ├─3─▶ MyBatisUtil.getSession()  →  SqlSession
          ├─4─▶ conexionBD.delete("direccion.eliminar", 87)
          │       │
          │       └─▶ DireccionMapper.xml ejecuta:
          │            DELETE FROM DIRECCION WHERE idDireccion = 87
          │
          ├─5─▶ [Éxito]  conexionBD.commit()  → return true
          │     [Error]   conexionBD.rollback() → return false
          └─6─▶ finally: conexionBD.close()
```

> 🔒 **¿Por qué no hay endpoint público?** Si se expusiera un `DELETE /direccion/eliminar/{id}`, alguien podría eliminar una dirección que todavía está asociada a un cliente o sucursal activo, rompiendo la integridad referencial. Por eso la eliminación se maneja solo desde el backend, coordinada con la eliminación de la entidad dueña.

---

### 13.4 Flujo de VISUALIZACIÓN (Mostrar Dirección)

La visualización ocurre en **dos escenarios**:

#### Escenario A — Ver dirección aislada (endpoint propio de `DireccionWS`)

**Componentes involucrados:**

| # | Componente | Archivo | Acción |
|---|---|---|---|
| 1 | Frontend | (cliente HTTP) | Envía `GET` con `idDireccion` como path param |
| 2 | `ws.DireccionWS` | `DireccionWS.java` | Recibe petición, valida que `idDireccion > 0` |
| 3 | `dominio.DireccionImp` | `DireccionImp.java` | Abre sesión, ejecuta SELECT, cierra sesión |
| 4 | `DireccionMapper.xml` | `DireccionMapper.xml` | SQL con JOINs a `COLONIAS`, `MUNICIPIOS`, `ESTADOS` |
| 5 | `pojo.Direccion` | `Direccion.java` | MyBatis mapea el resultado a este objeto (incluye campos geográficos) |
| 6 | Frontend | (cliente HTTP) | Recibe el objeto `Direccion` completo como JSON |

```
Frontend
  │
  ├─1─▶ GET /api/direccion/obtener-direccion-id/87
  │
  ▼
ws.DireccionWS → obtenerDireccionPorId(Integer idDireccion)
  │
  ├─2─▶ Valida: idDireccion != null && idDireccion > 0
  └─3─▶ llama DireccionImp.obtenerDireccionPorId(87)
          │
          ├─4─▶ MyBatisUtil.getSession()  →  SqlSession
          ├─5─▶ conexionBD.selectOne("direccion.obtener-direccion-id", 87)
          │       │
          │       └─▶ DireccionMapper.xml ejecuta SELECT con JOINs:
          │            DIRECCION ─JOIN─▶ COLONIAS ─JOIN─▶ MUNICIPIOS ─JOIN─▶ ESTADOS
          │            ← MyBatis mapea resultado a pojo.Direccion (todos los campos populados)
          │
          └─6─▶ return pojo.Direccion {
                   idDireccion:87, calle:"Av.Juárez", numero:"10",
                   idColonia:45, colonia:"Juárez", codigoPostal:6600,
                   ciudad:"CdMx", municipio:"Cuauhtémoc", estado:"CDMX"
                 }
```

#### Escenario B — Ver dirección embebida en otra entidad (ej. al listar clientes)

**Componentes involucrados:**

| # | Componente | Archivo | Acción |
|---|---|---|---|
| 1 | Frontend | (cliente HTTP) | Envía `GET /cliente/obtener-todos` |
| 2 | `ws.ClienteWS` | `ClienteWS.java` | Recibe petición, llama a `ClienteImp` |
| 3 | `dominio.ClienteImp` | `ClienteImp.java` | Abre sesión, ejecuta selectList |
| 4 | `ClienteMapper.xml` | `ClienteMapper.xml` | SELECT que hace JOIN con `DIRECCION`, `COLONIAS`, `MUNICIPIOS`, `ESTADOS` |
| 5 | `pojo.Cliente` | `Cliente.java` | MyBatis mapea el resultado completo (campos del cliente + campos de dirección) |
| 6 | Frontend | (cliente HTTP) | Recibe lista de clientes, cada uno con su dirección completa embebida |

> **Nota clave**: En este escenario el módulo `DireccionWS` y `DireccionImp` **no participan**. La dirección se recupera directamente en el mapper de la entidad dueña mediante JOINs. Esto es una optimización: en lugar de hacer dos llamadas (una para el cliente y otra para la dirección), todo se resuelve en un solo SELECT con JOINs.

```
Frontend
  │
  ├─1─▶ GET /api/cliente/obtener-todos
  │
  ▼
ws.ClienteWS → obtenerClientes()
  └─▶ ClienteImp.obtenerClientes()
        └─▶ SqlSession.selectList("cliente.obtener-clientes")
              └─▶ ClienteMapper.xml ejecuta:
                   SELECT cl.*, d.calle, d.numero, col.nombre AS nombreColonia, ...
                   FROM cliente cl
                   INNER JOIN direccion d ON cl.idDireccion = d.idDireccion
                   LEFT JOIN colonias col ON d.idColonia = col.id
                   LEFT JOIN municipios m ON col.municipio = m.id
                   LEFT JOIN estados e ON m.estado = e.id
                   ← MyBatis mapea a List<pojo.Cliente>
                      (cada Cliente tiene calle, colonia, ciudad, estado populados)

Frontend
  └─▶ Recibe JSON con clientes, cada uno trayendo la dirección completa
```

---

### 13.5 Tabla Resumen: Componentes por Operación

| Operación | `DireccionWS` | `DireccionImp` | `DireccionMapper.xml` | `pojo.Direccion` | `dto.Respuesta` | WS/Imp/Mapper de entidad dueña |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| **Insertar dirección** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Insertar entidad dueña** | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| **Editar dirección** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Editar entidad dueña** | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| **Eliminar dirección** | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Ver dirección aislada** | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Ver entidad con dirección** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ (JOINs en su mapper) |

---

## 14. Guía para el Cliente Escritorio — PacketWorldControlEscritorio

> ⚠️ **Aviso importante — Léelo antes de continuar:**
> Las secciones anteriores (1–13) documentan el **PacketWorld API** (servidor). Esta sección documenta cómo el **cliente escritorio JavaFX** (`PacketWorldControlEscritorio`) consume ese API y maneja la dirección desde la interfaz gráfica.
>
> **Si estás construyendo tu propio cliente escritorio**, toma esta sección como **guía de referencia del patrón**, NO como código que puedas copiar directamente. Tu proyecto tendrá sus propias entidades (no Cliente, no Sucursal), sus propios campos y sus propias pantallas. Lo que sí debes replicar es la **estrategia de comunicación con el API**, la **secuencia de dos pasos** para registrar, y la **separación de responsabilidades por capa**.

---

### 14.1 Stack Tecnológico del Cliente Escritorio

Este cliente está construido con **JavaFX** (interfaz gráfica) + **Java SE** (lógica de negocio) + **HTTP puro** (comunicación con el API REST). No usa MyBatis ni accede a la base de datos directamente.

**Principio fundamental**: El cliente escritorio **nunca toca la base de datos**. Toda operación se realiza enviando peticiones HTTP al API REST y procesando la respuesta JSON.

---

### 14.2 Arquitectura de Capas del Cliente Escritorio

```
┌─────────────────────────────────────────────────────────────────────────┐
│  CAPA DE VISTA (JavaFX)                                                 │
│  *.fxml  +  *Controller.java                                            │
│  Formularios, tablas, ComboBox, TextField, botones                      │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │ llama métodos estáticos de...
┌──────────────────────▼──────────────────────────────────────────────────┐
│  CAPA DE DOMINIO (Lógica de negocio + comunicación HTTP)                │
│  dominio/DireccionImp.java   dominio/ClienteImp.java                    │
│  dominio/SucursalImp.java    (y demás entidades)                        │
│  Construye URLs, serializa/deserializa JSON con Gson                    │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │ delega la conexión HTTP a...
┌──────────────────────▼──────────────────────────────────────────────────┐
│  CAPA DE CONEXIÓN                                                       │
│  conexion/ConexionAPI.java                                              │
│  Abre HttpURLConnection, envía petición, lee respuesta                  │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │ HTTP sobre la red...
┌──────────────────────▼──────────────────────────────────────────────────┐
│  PACKETTWORLD API REST (servidor — documentado en secciones 1–13)       │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 14.3 Componentes del Cliente Escritorio Relacionados con Dirección

| Componente | Archivo | Responsabilidad |
|---|---|---|
| **POJO Dirección** | `pojo/Direccion.java` | Mismo modelo que el API: `idDireccion`, `calle`, `numero`, `idColonia`, `colonia`, `ciudad`, `municipio`, `estado`, `codigoPostal` |
| **POJO Cliente** | `pojo/Cliente.java` | Tiene `idDireccion` (FK) más campos de visualización: `calle`, `numero`, `nombreColonia`, `codigoPostal`, `ciudad`, `estado`, `direccionCompleta` |
| **POJO Sucursal** | `pojo/Sucursal.java` | Igual que Cliente: FK + campos de visualización de dirección |
| **POJO RespuestaHTTP** | `pojo/RespuestaHTTP.java` | Encapsula `{codigo: int, contenido: String}` — resultado crudo del HTTP |
| **`dto.Respuesta`** | `dto/Respuesta.java` | Deserialización del `{error, mensaje, valor}` que devuelve el API |
| **`conexion.ConexionAPI`** | `conexion/ConexionAPI.java` | Métodos estáticos: `peticionGET()`, `peticionBody()`, `peticionSinBody()` |
| **`dominio.DireccionImp`** | `dominio/DireccionImp.java` | Llama al API de dirección: obtener colonias, obtener por ID, registrar, editar |
| **`dominio.ClienteImp`** | `dominio/ClienteImp.java` | Llama al API de cliente: registrar, editar, eliminar, buscar, obtener todos |
| **`dominio.SucursalImp`** | `dominio/SucursalImp.java` | Llama al API de sucursal: registrar, editar, eliminar, obtener |
| **`FXMLClientesController`** | `controlpacketworld/FXMLClientesController.java` | Pantalla lista de clientes + botones Registrar/Editar/Eliminar |
| **`FXMLClienteRegistrarController`** | `controlpacketworld/FXMLClienteRegistrarController.java` | Formulario de registro/edición de cliente (incluye campos de dirección) |
| **`FXMLSucursalesController`** | `controlpacketworld/FXMLSucursalesController.java` | Pantalla lista de sucursales |
| **`FXMLSucursalRegistrarController`** | `controlpacketworld/FXMLSucursalRegistrarController.java` | Formulario de registro/edición de sucursal (incluye campos de dirección) |
| **`utilidad.Constantes`** | `utilidad/Constantes.java` | `URL_WS`, claves de HashMap (`KEY_ERROR`, `KEY_LISTA`, `KEY_OBJETO`, `KEY_MENSAJE`), métodos HTTP |
| **`utilidad.GsonUtil`** | `utilidad/GsonUtil.java` | Instancia singleton de `Gson` para serialización/deserialización |
| **`interfaz.INotificador`** | `controlpacketworld/interfaz/INotificador.java` | Permite al formulario notificar al listado cuando una operación terminó |

> **Nota sobre `INotificador`**: Es un patrón de callback. El controlador del listado (`FXMLClientesController`) implementa esta interfaz. Al abrir el formulario modal, se pasa `this` como notificador. Cuando el formulario termina exitosamente, llama `notificador.notificarOperacionExitosa(...)` y el listado se refresca automáticamente. **No tiene nada que ver con dirección** — es un patrón general de comunicación entre ventanas. Se menciona aquí porque aparece en los flujos.

---

### 14.4 La Capa `ConexionAPI` — El Puente HTTP

```java
// Tres métodos principales:

// 1. GET sin body (para consultas)
ConexionAPI.peticionGET(String URL) → RespuestaHTTP

// 2. POST/PUT con body JSON
ConexionAPI.peticionBody(String URL, String metodo, String json, String contentType) → RespuestaHTTP

// 3. DELETE (u otros verbos sin body)
ConexionAPI.peticionSinBody(String URL, String metodo) → RespuestaHTTP
```

`RespuestaHTTP` tiene dos campos: `codigo` (HTTP status code) y `contenido` (String JSON de la respuesta). El dominio siempre verifica `codigo == HttpURLConnection.HTTP_OK` (200) antes de deserializar el contenido.

**Códigos de error propios** (no HTTP estándar):
- `Constantes.ERROR_MALFORMED_URL = 1001` → URL mal formada
- `Constantes.ERROR_PETICION = 1002` → fallo de red/conexión

---

### 14.5 El `DireccionImp` del Cliente — Qué Hace Cada Método

```java
// Obtener colonias por código postal → devuelve HashMap con lista de Direccion
DireccionImp.obtenerDireccion(String codigoPostal)
  → GET http://localhost:8080/PacketWorldAPI/webresources/direccion/obtener-direccion-codigo-postal/{cp}
  → HashMap { "error": false, "lista_valores": List<Direccion> }

// Obtener dirección completa por ID → devuelve HashMap con objeto Direccion
DireccionImp.obtenerDireccionPorId(Integer idDireccion)
  → GET .../direccion/obtener-direccion-id/{id}
  → HashMap { "error": false, "objeto": Direccion }

// Registrar nueva dirección → devuelve HashMap con el idDireccion generado
DireccionImp.registrarDireccion(Direccion direccion)
  → POST .../direccion/crear-direccion  Body: {calle, numero, idColonia}
  → HashMap { "error": false, "mensaje": "...", "valor": "87" }  ← idDireccion como String

// Editar dirección existente → devuelve dto.Respuesta
DireccionImp.editar(Direccion direccion)
  → PUT .../direccion/editar  Body: {idDireccion, calle, numero, idColonia}
  → Respuesta { error: false, mensaje: "Información de la dirección actualizada." }
```

> **Importante**: `registrarDireccion` devuelve `HashMap<String, Object>` y el ID generado llega en la clave `"valor"` como `String`. El controlador debe hacer `Integer.parseInt(idString)` para usarlo. Si el String es nulo o inválido, se muestra una alerta de error crítico.

---

## 15. Flujos Detallados por Operación en el Cliente Escritorio

### 15.1 INSERT — Registrar Entidad con Dirección (Ejemplo: Cliente)

**Pantalla involucrada**: `FXMLClienteRegistrar.fxml` + `FXMLClienteRegistrarController`

**Componentes del formulario relacionados con dirección**:
- `TextField tfCodigoPostal` — el usuario escribe el CP; al perder el foco dispara la carga de colonias
- `ComboBox<Direccion> cbColonia` — lista de colonias del CP; muestra `colonia.getNombre()`
- `TextField tfCalle` — captura calle
- `TextField tfNumero` — captura número
- `TextField tfEstado` — **no editable**, se rellena automáticamente al cargar colonias
- `TextField tfCiudad` — **no editable**, se rellena automáticamente al cargar colonias

**Flujo completo**:

```
Usuario (pantalla FXMLClienteRegistrar)
  │
  ├─1─▶ Escribe código postal en tfCodigoPostal y cambia el foco
  │      ↳ focusedProperty Listener dispara: cargarColonias(tfCodigoPostal.getText())
  │
  ▼
cargarColonias(codigoPostal)
  ├─2─▶ Valida: codigoPostal != null && length == 5
  ├─3─▶ DireccionImp.obtenerDireccion(codigoPostal)
  │       └─▶ ConexionAPI.peticionGET(URL + "direccion/obtener-direccion-codigo-postal/" + cp)
  │            ← API devuelve JSON: [ {idColonia, colonia, ciudad, estado, codigoPostal}, ... ]
  │            ← DireccionImp convierte a List<Direccion>
  ├─4─▶ colonias.addAll(listaReal)          ← ObservableList del ComboBox
  └─5─▶ tfCiudad.setText(listaReal.get(0).getCiudad())
         tfEstado.setText(listaReal.get(0).getEstado())

Usuario
  ├─6─▶ Selecciona una colonia en cbColonia
  ├─7─▶ Llena tfNombre, tfApellidoPaterno, tfTelefono, tfCorreo, tfCalle, tfNumero
  └─8─▶ Clic en btnRegistrar → clicRegistrar(event)

clicRegistrar() → sonCamposValidos() → true → registrarCliente()

registrarCliente()
  ├─9─▶  obtenerDireccionDeInterfaz()
  │        ← new Direccion(calle=tfCalle, numero=tfNumero, idColonia=cbColonia.selected.idColonia)
  │
  ├─10─▶ DireccionImp.registrarDireccion(direccion)
  │         └─▶ ConexionAPI.peticionBody(URL+"direccion/crear-direccion", "POST", json, "application/json")
  │              ← API devuelve: { error:false, mensaje:"Dirección guardada", valor:"87" }
  │         HashMap respuestaDireccion = { "error":false, "mensaje":"...", "valor":"87" }
  │
  ├─11─▶ [Si error==false] idDireccion = Integer.parseInt(respuestaDireccion.get("valor"))
  │
  ├─12─▶ new Cliente(nombre, apellido, ..., idDireccion=87)
  │
  ├─13─▶ ClienteImp.registrarCliente(cliente)
  │         └─▶ ConexionAPI.peticionBody(URL+"cliente/registrar", "POST", json, "application/json")
  │              ← API devuelve: { error:false, mensaje:"Cliente registrado" }
  │
  └─14─▶ [Si error==false] notificador.notificarOperacionExitosa("registrado", nombre)
           └─▶ FXMLClientesController.notificarOperacionExitosa()
                 ├─▶ cargarDatosTabla()   ← refresca la tabla
                 └─▶ Utilidades.mostrarAlertaSimple(...)

[En paralelo] FXMLClienteRegistrarController.cerrarVentana()
```

**Resumen de componentes por paso**:

| Paso | Componente |
|---|---|
| Escucha foco del CP | `focusedProperty` listener en `FXMLClienteRegistrarController` |
| Cargar colonias | `DireccionImp.obtenerDireccion()` → `ConexionAPI.peticionGET()` |
| Llenar ComboBox | `ObservableList<Direccion> colonias` + `StringConverter` en el controller |
| Recoger datos de UI | `obtenerDireccionDeInterfaz()` → construye `pojo.Direccion` |
| Registrar dirección | `DireccionImp.registrarDireccion()` → `ConexionAPI.peticionBody()` |
| Obtener ID generado | `respuestaDireccion.get("valor")` → `Integer.parseInt()` |
| Registrar cliente | `ClienteImp.registrarCliente()` → `ConexionAPI.peticionBody()` |
| Notificar éxito | `INotificador.notificarOperacionExitosa()` → `FXMLClientesController` |

---

### 15.2 SELECT — Mostrar Entidades con Dirección en Tabla

**Pantalla involucrada**: `FXMLClientes.fxml` + `FXMLClientesController`

**Columnas de la tabla relacionadas con dirección**:
- `colCalle` → propiedad `calle` del POJO Cliente
- `colNumero` → propiedad `numero`
- `colColonia` → propiedad `nombreColonia`
- `colCP` → propiedad `codigoPostal`
- `colCiudad` → propiedad `ciudad`
- `colEstado` → propiedad `estado`

**Flujo completo**:

```
FXMLClientesController.initialize()
  └─▶ cargarDatosTabla()

cargarDatosTabla()
  ├─1─▶ ClienteImp.obtenerClientes()
  │       └─▶ ConexionAPI.peticionGET(URL + "cliente/obtener-todos")
  │            ← API ejecuta SELECT con JOINs a DIRECCION, COLONIAS, MUNICIPIOS, ESTADOS
  │            ← JSON: [ { idCliente, nombre, ..., calle, numero, nombreColonia,
  │                         codigoPostal, ciudad, estado, direccionCompleta }, ... ]
  │            ← Gson deserializa a List<Cliente>
  │
  ├─2─▶ listaClientes.addAll(clientesWS)
  └─3─▶ tvClientes.setItems(listaClientes)
         ← PropertyValueFactory mapea cada campo a su columna
         ← Los campos de dirección (calle, nombreColonia, ciudad, etc.)
            vienen ya populados desde el API — NO se hace una llamada extra a DireccionImp

```

> **Punto clave para la IA lectora**: Al listar, el módulo de dirección (`DireccionImp`) **no interviene**. Los campos de dirección llegan directamente en el JSON de la entidad (Cliente/Sucursal) porque el API los incluye vía JOINs en su SELECT. El cliente escritorio solo deserializa ese JSON y lo muestra en la tabla.

**Nota sobre `getCodigoPostal()` en `pojo.Cliente`**: El POJO tiene lógica de fallback. Si el campo `codigoPostal` llegó vacío pero `direccionCompleta` está poblado, extrae el CP parseando el string `"Av. Juárez 10, Juárez, C.P. 06600, ..."`. Esto es una medida defensiva; en condiciones normales, el API ya devuelve `codigoPostal` directo.

---

### 15.3 UPDATE — Editar Entidad con Dirección (Ejemplo: Cliente)

**Pantalla involucrada**: El mismo `FXMLClienteRegistrar.fxml` + `FXMLClienteRegistrarController`, pero con `esEdicion = true`

**Diferencias respecto al registro**:
- El botón muestra `"Actualizar"` en vez de `"Registrar"`
- `inicializarValores(notificador, cliente)` recibe el objeto `Cliente` con todos sus datos ya cargados
- Se llama `cargarDatosEdicion()` que precarga los campos del formulario
- La colonia actual se preselecciona en el ComboBox con `seleccionarColonia(idColonia)`

**Flujo completo**:

```
FXMLClientesController
  ├─1─▶ Usuario selecciona un cliente en la tabla y clic en "Editar"
  └─2─▶ irFormulario(true, clienteSeleccionado)
           └─▶ Carga FXMLClienteRegistrar.fxml como ventana modal
                controlador.inicializarValores(this, clienteSeleccionado)

FXMLClienteRegistrarController.inicializarValores(...)
  ├─3─▶ esEdicion = true
  └─4─▶ cargarDatosEdicion()
           ├─▶ tfNombre.setText(cliente.getNombre())
           ├─▶ tfCalle.setText(cliente.getCalle())
           ├─▶ tfCodigoPostal.setText(cliente.getCodigoPostal())
           ├─▶ cargarColonias(cliente.getCodigoPostal())   ← igual que en registro
           └─▶ seleccionarColonia(cliente.getIdColonia())
                 └─▶ Platform.runLater(() → cbColonia.select(dir donde dir.idColonia == idColonia))

Usuario modifica campos y clic en "Actualizar" → clicRegistrar()
  ├─5─▶ sonCamposValidos() → true
  └─6─▶ [esEdicion == true] construye objetos separados:
           │
           ├─▶ Cliente cliente = new Cliente()
           │     cliente.setIdCliente(clienteEdicion.getIdCliente())
           │     cliente.setIdDireccion(clienteEdicion.getIdDireccion())  ← FK no cambia
           │     cliente.setNombre(tfNombre.getText())
           │     ... (otros campos personales)
           │
           └─▶ Direccion direccionEdicion = new Direccion()
                 direccionEdicion.setIdDireccion(clienteEdicion.getIdDireccion()) ← ID existente
                 direccionEdicion.setCalle(tfCalle.getText())
                 direccionEdicion.setNumero(tfNumero.getText())
                 direccionEdicion.setIdColonia(cbColonia.selected.getIdColonia())

  ├─7─▶ DireccionImp.editar(direccionEdicion)           ← PRIMERO se edita dirección
  │       └─▶ ConexionAPI.peticionBody(URL+"direccion/editar", "PUT", json, "application/json")
  │            ← Respuesta: { error:false, mensaje:"Información de la dirección actualizada." }
  │
  └─8─▶ [Si no hubo error] actualizarCliente(cliente)  ← LUEGO se edita el cliente
           └─▶ ClienteImp.editarCliente(cliente)
                 └─▶ ConexionAPI.peticionBody(URL+"cliente/editar", "PUT", json, "application/json")
                      ← Respuesta: { error:false, mensaje:"..." }
                 └─▶ notificador.notificarOperacionExitosa("actualizado", nombre)
```

> **Regla de negocio crítica**: Si la edición de dirección falla, la edición del cliente **NO se intenta**. Se muestra alerta de error y el usuario debe corregir. La dirección siempre se actualiza primero.

---

### 15.4 DELETE — Eliminar Entidad (Ejemplo: Cliente)

**Pantalla involucrada**: `FXMLClientesController` — no abre formulario modal

**Flujo completo**:

```
FXMLClientesController
  ├─1─▶ Usuario selecciona cliente en tabla y clic en "Eliminar"
  └─2─▶ clicEliminar(event)

clicEliminar()
  ├─3─▶ clienteSeleccionado = tvClientes.getSelectionModel().getSelectedItem()
  ├─4─▶ Utilidades.mostrarAlertaConfirmacion("¿Estás seguro...?")
  │       └─▶ Si usuario cancela → termina aquí
  │
  └─5─▶ [Si confirma] ClienteImp.eliminarCliente(idCliente)
           └─▶ ConexionAPI.peticionSinBody(URL+"cliente/eliminar/"+idCliente, "DELETE")
                ← El API (servidor) maneja la eliminación de la dirección internamente
                   via DireccionImp.eliminar(idDireccion) — el cliente no lo sabe ni lo controla
                ← Respuesta: { error:false, mensaje:"Cliente eliminado" }

  ├─6─▶ [Si error==false] Utilidades.mostrarAlertaSimple("Cliente eliminado", ...)
  └─7─▶ cargarDatosTabla()   ← refresca la tabla
```

> **Punto clave**: El cliente escritorio **nunca llama a `DireccionImp` para eliminar**. Solo llama a `ClienteImp.eliminarCliente()` y el API REST se encarga de eliminar también la dirección asociada en cascada (usando internamente `DireccionImp.eliminar()`). El cliente desktop está completamente desacoplado de esa responsabilidad.

---

### 15.5 Flujo de Sucursal — Diferencias Respecto a Cliente

El formulario de sucursal (`FXMLSucursalRegistrarController`) sigue exactamente el mismo patrón que el de cliente. Las diferencias son:

| Aspecto | Cliente | Sucursal |
|---|---|---|
| Campos propios adicionales | `apellidoPaterno`, `apellidoMaterno`, `telefono`, `correo` | `codigo` (único, no editable en edición), `estatus` |
| Validación extra en registro | No | Verifica duplicado de calle+numero+colonia antes de registrar |
| Eliminar | DELETE real (baja física) vía `DELETE cliente/eliminar/{id}` | Baja lógica (estatus = Inactiva) vía `PUT sucursal/bajar/{id}` |
| Extracción de CP en edición | `cliente.getCodigoPostal()` (campo directo) | `extraerCP(sucursal.getDireccionCompleta())` — parsea el string de dirección completa |

**Validación de duplicado en Sucursal** (lógica específica de este proyecto):
```java
// Antes de registrar, se consulta la lista completa de sucursales y se compara
// calle + numero + idColonia con las existentes
private boolean existeDireccionEnBaseDeDatos() {
    List<Sucursal> listaSucursales = SucursalImp.obtenerSucursales();
    // itera y compara equalsIgnoreCase en calle, numero e idColonia.equals(...)
}
```

---

## 16. Tabla Resumen de Componentes del Cliente Escritorio por Operación

| Operación | Vista (Controller) | DireccionImp | ClienteImp / SucursalImp | ConexionAPI |
|---|:---:|:---:|:---:|:---:|
| **Cargar colonias (CP)** | `FXMLClienteRegistrarController` | ✅ `obtenerDireccion()` | ❌ | ✅ `peticionGET` |
| **INSERT dirección + entidad** | `FXMLClienteRegistrarController` | ✅ `registrarDireccion()` | ✅ `registrarCliente()` | ✅ `peticionBody` x2 |
| **SELECT lista entidades** | `FXMLClientesController` | ❌ | ✅ `obtenerClientes()` | ✅ `peticionGET` |
| **UPDATE dirección + entidad** | `FXMLClienteRegistrarController` | ✅ `editar()` | ✅ `editarCliente()` | ✅ `peticionBody` x2 |
| **DELETE entidad (dirección interna)** | `FXMLClientesController` | ❌ | ✅ `eliminarCliente()` | ✅ `peticionSinBody` |
| **Precargar formulario edición** | `FXMLClienteRegistrarController` | ✅ `obtenerDireccion()` | ❌ | ✅ `peticionGET` |

> **Conclusión para la IA lectora**: En el cliente escritorio, la dirección nunca se maneja sola. Siempre aparece acompañando a una entidad (Cliente, Sucursal). El único momento en que `DireccionImp` es indispensable es al **registrar** (dos pasos obligatorios) y al **editar** (siempre se actualiza la dirección antes que la entidad dueña). En el **SELECT** y en el **DELETE**, `DireccionImp` no participa desde el cliente — esas responsabilidades las asume el API servidor de forma transparente.
