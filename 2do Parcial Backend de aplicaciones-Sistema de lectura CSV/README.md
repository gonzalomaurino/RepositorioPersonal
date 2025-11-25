# README – Solución del Simulacro (Enunciado Final)

## Propósito

Este README guía paso a paso la **solución completa** del simulacro final. La idea es que sirva como estrategia de ataque para el parcial: reproducir el proceso que seguimos para resolverlo, de punta a punta, **sin tests** (main lineal), usando **H2 en memoria**, **JPA (EntityManager manual)**, **OpenCSV** y un servicio de importación central (`ImportService`).

---

## 0) Requisitos previos

* JDK 21 (o compatible con tu entorno).
* Maven 3.8+.
* IDE a elección (VS Code/IntelliJ) con **annotation processing** habilitado (Lombok).
* Archivo `games_data.csv` (UTF-8, separador `;`).

---

## 1) Estructura del proyecto

Proyecto **Maven de un solo módulo**, app de consola:

```bash
src/
  main/java/utnfc/isi/back/sim/
    app/MainApp.java                 # flujo principal lineal
    infra/DatabaseInitializer.java   # ejecuta DDL en H2
    infra/LocalEntityManagerProvider.java  # EMF recreable
    csv/                             # OpenCSV → CsvGameRow
      CsvLoader.java
      CsvGameRow.java
      (converters mínimos: fecha, rating, K)
    service/                         # lógica de importación
      ImportService.java
    domain/                          # entidades + enum
      Desarrollador.java
      Genero.java
      Plataforma.java
      Juego.java                     # ESRB como String + helpers Enum
      ClasificacionEsrb.java
  main/resources/
    META-INF/persistence.xml
    sql/ddl.sql                      # Estructura H2 del pre-enunciado
```

**Dependencias:** `jakarta.persistence-api`, `hibernate-core`, `h2`, `opencsv`, `lombok`.

---

## 2) Paso 1 – Inicializar la BD (DDL en memoria)

1. `DatabaseInitializer.recreateSchemaFromDdl()` lee `sql/ddl.sql` y ejecuta todos los statements sobre `jdbc:h2:mem:backdb;DB_CLOSE_DELAY=-1;MODE=LEGACY`.
2. `LocalEntityManagerProvider` invoca al inicializador y crea el `EntityManagerFactory` **solo si está cerrado** (patrón *ensureOpen()*), de modo que las corridas sean determinísticas y no dependan del orden.

> Resultado: un esquema limpio con tablas **JUEGOS**, **DESARROLLADORES**, **PLATAFORMAS**, **GENEROS** y sus **secuencias**.

---

## 3) Paso 2 – Lectura del CSV con OpenCSV

* `CsvGameRow` mapea columnas por nombre (header binding) y aplica **conversores mínimos**:

  * `Release_Date` → **epoch millis** (`Integer`, null si `""` o `TBD`).
  * `Rating` → `Double` (null si `N/A`).
  * `Plays` / `Playing` → `Integer` soportando sufijo `K`.
  * `Developers` / `Platforms` / `Genres` se leen como **cadenas** y luego se aplanan con un helper (`"['Nintendo']" → "Nintendo"`; `[]`/vacío → null).
* El parser se configura **sin quote char** para evitar cuelgues por `"`/`'` mezcladas.

```java
var rows = CsvLoader.read(pathCsv);
```

---

## 4) Paso 3 – Servicio de importación (`ImportService`)

El `ImportService` encapsula toda la lógica de carga desde el CSV hacia la base de datos.
Su función es **aislar el dominio y la persistencia del detalle de parsing**, manteniendo el código organizado y reutilizable.

### Estructura interna:

* **Acumulación en memoria:**

  * `Map<String, Desarrollador> devs`
  * `Map<String, Genero> gens`
  * `Map<String, Plataforma> plats`
  * `List<PendingGame> juegosPendientes`

* **Filtrado y normalización:**

  * Se descartan filas sin desarrollador, plataforma o género.
  * Se normaliza `clasificacionEsrb` a partir del código textual.
  * Se limpian resúmenes y campos vacíos.
  
* **Persistencia diferida:**

  * Una vez finalizada la lectura, se abre un `EntityManager`.
  * Se ejecuta **una única transacción**: primero las entidades maestras (Maps) y luego los juegos.
  * Se genera un `ImportResult` con las cantidades insertadas y validadas.

Ejemplo de uso desde el `MainApp`:

```java
var rows = CsvLoader.read(pathCsv);
var service = new ImportService();
var result = service.importar(rows);
System.out.printf("Importación completada: %d filas válidas, %d juegos cargados\n",
    result.getFilasValidas(), result.getJuegosInsertados());
```

---

## 5) Paso 4 – Armado en memoria (performance primero)

* En **memoria** se acumulan, sin tocar la BD:

  * `Map<String, Desarrollador> devs`  (clave: nombre)
  * `Map<String, Genero> gens`
  * `Map<String, Plataforma> plats`
  * `List<PendingGame> juegos` (datos completos del juego listos para persistir)
* Se **filtran** filas que no traen *Desarrollador*, *Plataforma* o *Género*.
* Se normaliza **ESRB**: si vacío → `UR` (Unrated) o se deja `null` según lo acordado.

---

## 6) Paso 5 – Persistencia en bloque (una sola transacción)

1. Se abre un `EntityManager` y se inicia **una transacción**.
2. Se persisten (en cualquier orden estable) las **tablas maestras**: `DESARROLLADORES`, `GENEROS`, `PLATAFORMAS` a partir de los `Map` (IDs autogenerados por secuencias).
3. Luego se crean y `persist` todas las filas de **JUEGOS**, resolviendo las relaciones por **referencia** a los objetos ya persistidos de los mapas.
4. `commit()`. Si algo falla → `rollback()`.

Ventajas: **0 roundtrips** durante el parseo, y **1 commit** grande contra H2.

---

## 7) Paso 6 – Cálculos solicitados en el enunciado

Desde el `MainApp` (o un pequeño `ReportService`), una vez poblada la BD:

1. **Top-5 Géneros por `jugando`**

   * `SELECT g.nombre, SUM(COALESCE(j.jugando,0)) ... GROUP BY g.nombre ORDER BY SUM(...) DESC LIMIT 5`
   * Mostrar los 5 mayores.

2. **Desarrolladores con más de 30 juegos**

   * `SELECT d.nombre, COUNT(j) ... GROUP BY d.nombre HAVING COUNT(j) > 30 ORDER BY COUNT(j) DESC`.

3. **Plataforma mejor rankeada** (rating promedio)

   * `SELECT p.nombre, AVG(j.rating) ... WHERE j.juegos_finalizados > 999 GROUP BY p.nombre ORDER BY AVG(j.rating) DESC FETCH FIRST 1 ROW ONLY`.

> Las consultas pueden resolverse con JPQL y `TypedQuery<Object[]>` o con SQL nativo contra H2. Mantener mensajes claros por consola.

---

## 8) Paso 7 – Ejecución (main lineal)

Ejecutar indicando la ruta al CSV:

```bash
mvn -q exec:java \
  -Dexec.mainClass=utnfc.isi.back.sim.app.MainApp \
  -Dexec.args="/ruta/al/games_data.csv"
```

**Flujo esperado por consola** (orientativo):

```bash
[OK] Esquema inicializado (H2 en memoria)
[OK] CSV leído: 16473 filas (válidas: XXXX)
[OK] Persistidos: devs=AAA, géneros=BBB, plataformas=CCC, juegos=DDD

Top 5 Géneros por jugando:
  1) Adventure  →  123456
  2) Shooter    →  ...
...

Desarrolladores con > 30 juegos:
  Nintendo →  321
  ...

Plataforma mejor rankeada (finalizados > 999):
  Nintendo Switch →  4.28
```

---

## 9) Consideraciones y decisiones de diseño

* **ESRB**: se guarda como `VARCHAR(4)` en `JUEGOS.CLASIFICACION_ESRB` y se expone como `Enum` mediante getters/setters **transitorios** (sin `@Converter`).
* **Logging**: se apagan logs de Hibernate/JUL al inicio para una salida limpia.
* **CSV robusto**: parser sin quote char; saneo de `/n` → `\n` en resúmenes.
* **Idempotencia**: cada corrida reinicia la BD (H2 en memoria + DDL).
* **ImportService**: componente clave que orquesta lectura, acumulación y persistencia, encapsulando la lógica de negocio de la carga.

---

## 10) Errores comunes y cómo evitarlos

* *“EntityManagerFactory is closed”*: usar `LocalEntityManagerProvider` recreable; no cerrar el EMF en cada test/ejecución.
* *Comillas en CSV* (`"` y `'`): configurar `CsvLoader` con `withQuoteChar(NULL)` e `ignoreQuotations(true)`.
* *Campos `[]`*: recordar aplanar `Developers/Platforms/Genres` a **valor único** o `null`.
* *Fecha `TBD`/vacía*: dejar `fechaLanzamiento = null`.

---

## 11) Checklist antes de entregar

* [ ] El `ddl.sql` es exactamente el del pre-enunciado y se ejecuta al inicio.
* [ ] El `MainApp` recibe la ruta al CSV por argumento y ejecuta todo el flujo.
* [ ] Se informa por consola: filas leídas, válidas, insertados por tabla y resultados de los 3 reportes.
* [ ] El código compila con `mvn clean package` y corre con `mvn exec:java`.

---

**UTN FRC — Backend de Aplicaciones (3K2)**
*Solución del Simulacro (Enunciado Final)*
