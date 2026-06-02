# Entregables del Proyecto FitNutrition

Este documento resume la evidencia técnica de tres entregables del proyecto FitNutrition:

- Desarrollo UI escritorio JavaFX.
- Integración UI con API REST.
- Módulo de seguridad y login.

La entrega se respalda con los repositorios del sistema:

- Aplicación de escritorio JavaFX: `C:\Users\benit\NetBeansProjects\FXFitNutrition`
- API REST: `C:\Users\benit\NetBeansProjects\APIFitNutrition\api-rest-fitnutrition`

---

## 1. Desarrollo UI Escritorio JavaFX

### Descripción

Se desarrolló una aplicación de escritorio en JavaFX para la gestión de los módulos principales del sistema FitNutrition. La interfaz permite al usuario navegar entre módulos, consultar información, registrar datos, modificar registros y ejecutar acciones propias de cada flujo funcional.

### Alcance Implementado

La aplicación de escritorio contempla las siguientes vistas y módulos:

- Inicio de sesión.
- Menú principal con navegación por rol.
- Gestión de pacientes.
- Gestión de citas.
- Gestión de consultas médicas.
- Historial médico por paciente.
- Gestión de dietas.
- Gestión de alimentos.
- Gestión de médicos para usuarios administradores.

### Componentes JavaFX Utilizados

La interfaz utiliza componentes propios de JavaFX, entre ellos:

- `FXML`
- `AnchorPane`
- `VBox`
- `HBox`
- `GridPane`
- `TableView`
- `TableColumn`
- `ComboBox`
- `TextField`
- `TextArea`
- `DatePicker`
- `Spinner`
- `Button`
- `Label`
- `Alert`
- Ventanas modales con `Stage`

### Evidencias Funcionales

- Las pantallas principales se encuentran organizadas por módulo en el proyecto JavaFX.
- El menú principal permite acceder a los módulos disponibles según el rol del usuario.
- Las tablas muestran información proveniente de la API REST.
- Los formularios cuentan con validaciones locales.
- Las acciones de registrar, modificar, cancelar, eliminar y consultar están conectadas con la lógica del sistema.
- Se aplicó una hoja de estilos para mantener una identidad visual consistente.

### Criterios de Aceptación Cubiertos

- [x] La aplicación de escritorio ejecuta las vistas principales mediante JavaFX.
- [x] Los formularios permiten capturar y validar información.
- [x] Las tablas muestran datos de los módulos principales.
- [x] La navegación entre módulos funciona desde el menú principal.
- [x] La interfaz utiliza mensajes visuales mediante `Alert` para errores, confirmaciones y validaciones.
- [x] El código se ejecuta en el entorno local mediante NetBeans/Ant.

---

## 2. Integración UI con API REST

### Descripción

La aplicación de escritorio JavaFX fue integrada con la API REST de FitNutrition para consumir y persistir información real del sistema. La comunicación entre la interfaz y el backend permite que los módulos operen sobre datos almacenados en la base de datos.

### Alcance de la Integración

La integración contempla operaciones sobre los siguientes módulos:

- Login y sesión de usuario.
- Pacientes.
- Citas.
- Consultas médicas.
- Dietas.
- Alimentos.
- Médicos.

### Comunicación Cliente-Servidor

La aplicación de escritorio consume servicios HTTP expuestos por la API REST. Las clases de implementación del cliente realizan peticiones a endpoints del backend para consultar, registrar, modificar, cancelar o eliminar información.

Ejemplos de operaciones integradas:

- Buscar citas por criterio, médico y estatus.
- Registrar, modificar, cancelar y reagendar citas.
- Registrar y consultar consultas médicas.
- Consultar historial médico de pacientes.
- Registrar, modificar y cancelar consultas.
- Crear, consultar, modificar y eliminar dietas.
- Registrar, buscar y editar alimentos.
- Autenticar usuarios por rol.

### Evidencias Técnicas

- La aplicación JavaFX obtiene datos desde la API REST.
- Los formularios envían información al backend mediante peticiones HTTP.
- Las tablas se refrescan después de registrar, modificar o cancelar información.
- El backend valida reglas de negocio complementarias a las validaciones de la interfaz.
- La API REST responde con objetos o mensajes controlados para que la interfaz muestre alertas al usuario.

### Criterios de Aceptación Cubiertos

- [x] La UI consume endpoints reales de la API REST.
- [x] Las operaciones CRUD principales están conectadas con backend.
- [x] Los cambios realizados desde la interfaz se reflejan en la información consultada posteriormente.
- [x] La aplicación muestra mensajes de error cuando la API no responde o rechaza una operación.
- [x] La integración permite trabajar con datos reales del sistema.
- [x] El código se ejecuta sin errores en el entorno local.

---

## 3. Módulo de Seguridad y Login

### Descripción

Se implementó el módulo de autenticación para controlar el acceso al sistema FitNutrition. El inicio de sesión permite distinguir entre usuarios con rol de administrador y usuarios con rol de médico, aplicando reglas de visibilidad y filtrado de información según corresponda.

### Roles Contemplados

El sistema contempla los siguientes roles:

- Administrador.
- Médico.

### Reglas por Rol

Administrador:

- Puede acceder a todos los módulos disponibles.
- Puede visualizar información general de pacientes, citas, consultas, dietas, alimentos y médicos.
- No se aplican filtros por médico en los módulos clínicos.

Médico:

- Puede acceder a los módulos operativos del sistema.
- En pacientes, citas y consultas, visualiza únicamente la información relacionada con su usuario médico.
- En dietas y alimentos, puede visualizar la información general del catálogo.
- No tiene acceso al módulo de gestión de médicos.

### Flujo de Login

1. El usuario ingresa sus credenciales en la pantalla de inicio de sesión.
2. La aplicación envía la solicitud de autenticación a la API REST.
3. La API valida las credenciales.
4. Si las credenciales son correctas, la aplicación guarda la sesión del usuario.
5. El menú principal se configura de acuerdo con el rol autenticado.
6. Si las credenciales son incorrectas o el servicio falla, se muestra una alerta al usuario.

### Evidencias Técnicas

- Existe una pantalla de inicio de sesión en la aplicación JavaFX.
- La autenticación se realiza contra la API REST.
- La sesión del usuario se conserva durante la navegación.
- El menú principal cambia según el rol.
- El módulo de médicos se oculta para usuarios médicos.
- Los módulos de pacientes, citas y consultas aplican filtros cuando el usuario autenticado es médico.

### Criterios de Aceptación Cubiertos

- [x] El sistema permite iniciar sesión con credenciales válidas.
- [x] El sistema rechaza credenciales inválidas mediante una alerta visual.
- [x] El sistema identifica si el usuario autenticado es administrador o médico.
- [x] El menú principal se adapta al rol del usuario.
- [x] El usuario médico solo visualiza información clínica asociada a su cuenta.
- [x] El administrador visualiza la información general del sistema.
- [x] El cierre de sesión limpia la sesión actual y regresa al inicio de sesión.

---

## Forma de Entrega Recomendada

Para estos tres entregables, se recomienda entregar:

- Repositorio de la aplicación JavaFX.
- Repositorio de la API REST.
- Este documento como evidencia técnica.
- Capturas de pantalla opcionales de los módulos principales.
- Instrucciones básicas de ejecución en caso de que el docente desee probar el sistema localmente.

Los puntos de desarrollo UI, integración API REST y seguridad/login se consideran cubiertos dentro del código fuente del repositorio, pero este documento permite identificar claramente qué parte del sistema corresponde a cada entregable.

