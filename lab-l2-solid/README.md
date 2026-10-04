# LABORATORIO L2: SOLID

## Taller integrador: el backend de Banco Andino

**Ingeniería de Software II**

Universidad Nacional de Colombia · Sede Bogotá · 2026

- **Modalidad:** parejas
- **Lenguaje:** libre (el código base se presenta en Java)
- **Prerrequisito:** Laboratorio L1 (SOLID letra por letra)

> **¿Qué cambia respecto al L1?** En el L1 cada ejercicio les decía qué principio aplicar
> y el código estaba aislado. En este laboratorio reciben un solo sistema, con clases que
> dependen unas de otras, y nadie les dice dónde están los problemas. Ustedes los
> diagnostican, los corrigen y, al final, el negocio les pedirá cambios que pondrán a
> prueba si su diseño realmente aguanta.



## Recorrido del laboratorio


| Bloque                   | Qué van a hacer                                         |
| ------------------------ | ------------------------------------------------------- |
| 0. Arranque              | Preparar el proyecto y conocer el dominio               |
| 1. Diagnóstico           | Encontrar los problemas y medir el "antes"              |
| 2. Refactorización       | Corregir el sistema con puntos de control S, O, L, I, D |
| 3. Pruebas unitarias     | Probar el diseño nuevo con dobles de prueba             |
| 4. Negocio pidió cambios | Implementar requerimientos que no conocían              |
| 5. Revisión cruzada      | Extender el código de otra pareja                       |
| 6. Cierre                | UML final, comparación y reflexión                      |




## El contexto

> "Bienvenidos al equipo de banca móvil de Banco Andino. La app lleva dos años en
> producción y los clientes la usan todos los días. El problema es el backend: cada cambio
> que pide el área de negocio tarda semanas, rompe algo que no tenía nada que ver y nadie
> se atreve a tocar la clase `TransaccionService`. El desarrollador que la escribió ya no está.
> Ustedes la heredaron."



### Glosario bancario


| Término                     | Significado en este laboratorio                                                                                                                                       |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Cuenta de ahorros           | Cuenta en la que el cliente deposita, retira y transfiere dinero libremente.                                                                                          |
| CDT                         | Certificado de Depósito a Término. El cliente deja un dinero "congelado" hasta una fecha de vencimiento a cambio de intereses. No permite retiros antes de esa fecha. |
| Cuota de manejo             | Valor mensual que el banco cobra por mantener una cuenta.                                                                                                             |
| Transferencia interbancaria | Transferencia hacia una cuenta de otro banco. Tiene comisión.                                                                                                         |
| Avance                      | Retiro de efectivo con cargo al cupo de una tarjeta de crédito.                                                                                                       |
| Extracto                    | Resumen del estado de un producto (saldo, deuda, etc.).                                                                                                               |




### Qué hace el sistema

- Transfiere dinero entre cuentas, cobra la comisión según el tipo de transferencia, guarda la
transacción en Oracle, imprime el comprobante, avisa al cliente por SMS y deja un registro
de auditoría.
- Cobra la cuota de manejo mensual a una lista de cuentas.
- Genera extractos de los productos de crédito (tarjeta y crédito de vivienda).

La conexión a Oracle y el envío de SMS están simulados con mensajes en consola, pero
imaginen que son reales: cada vez que se ejecutan, el sistema se conecta a la base de datos de
producción y le llega un mensaje de texto al cliente.

## Bloque 0 — Arranque

Preparar el proyecto y entender qué hace el sistema antes de juzgarlo.

1. Creen el proyecto en el lenguaje elegido y copien (o traduzcan) el código base.
2. Ejecuten el programa principal y guarden su salida en un archivo `salida_original.txt`.
  La usarán en el bloque 2 para comprobar que no cambiaron el comportamiento.
3. Lean el código completo una vez, sin tomar notas, solo para entender el flujo de una
  transferencia.



## Bloque 1 — Diagnóstico



### 1.1 Tabla de hallazgos

En el código hay al menos un problema por cada letra de SOLID, y algunas clases
tienen más de uno. Encuéntrenlos y regístrenlos en una tabla como esta en su README:


| Clase / método                                                             | Letra | Evidencia en el código                                                                                                                                                                                                                                                                                                                                                                                                  | Consecuencia para el banco o el cliente                                                                                                                                                                                                                                  |
| -------------------------------------------------------------------------- | ----- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `TransaccionService.transferir()`                                          | S     | Un solo método hace siete cosas: valida, calcula la comisión, mueve el dinero, guarda en Oracle, imprime el comprobante, envía el SMS y registra la auditoría. Los comentarios numerados del 1 al 7 lo muestran.                                                                                                                                                                                                        | Cualquier cambio, aunque sea solo de formato del comprobante o de proveedor de SMS, obliga a tocar el código que mueve el dinero, con riesgo de romper las transferencias. Por eso los cambios tardan semanas y nadie se atreve a modificar la clase.                    |
| `TransaccionService` (atributos `repositorio` y `sms`)                     | D     | Depende directamente de las clases concretas `OracleRepositorio` y `SmsGateway`, y las crea ella misma con `new`. No hay interfaces ni constructor que permita pasarle otra implementación.                                                                                                                                                                                                                             | Cambiar de base de datos o de proveedor de SMS obliga a modificar la clase que mueve el dinero. Además, no se puede probar una transferencia sin escribir en la base de datos de producción y sin enviarle un SMS real al cliente.                                       |
| `TransaccionService.transferir()` (`switch` de la comisión)                | O     | La comisión se calcula con un `switch` sobre el `String tipo`, con un `case` por cada tipo de transferencia (`MISMO_BANCO`, `OTRO_BANCO`, `INTERNACIONAL`). Para agregar un tipo nuevo hay que editar ese `switch`.                                                                                                                                                                                                     | Cada vez que el banco lanza un tipo de transferencia nuevo o cambia una tarifa, hay que modificar y volver a probar la clase que mueve el dinero, con riesgo de afectar las transferencias que ya funcionan.                                                             |
| `ProductoBancario` (implementada por `TarjetaCredito` y `CreditoVivienda`) | I     | La interfaz obliga a implementar cinco métodos (`depositar`, `retirar`, `calcularIntereses`, `pagarCuota`, `generarExtracto`). `TarjetaCredito.depositar()` y `CreditoVivienda.depositar()`/`retirar()` quedan vacíos con el comentario `// no aplica`. Además, `Main` solo usa `generarExtracto()`.                                                                                                                    | Las operaciones que "no aplican" se pueden llamar y no hacen nada ni avisan: si la app le ofrece al cliente depositar o retirar sobre su crédito de vivienda, la operación parece exitosa pero el saldo no cambia. Cada producto nuevo arrastra métodos que no necesita. |
| `CDT.retirar()` (hereda de `Cuenta`)                                       | L     | `Cuenta.retirar()` permite retirar siempre que haya saldo. `CDT` lo sobrescribe y lanza `UnsupportedOperationException` si la fecha actual es anterior al vencimiento: agrega una condición que el padre no tiene y una excepción que quien usa una `Cuenta` no espera. `CobroCuotaManejo.cobrarMensual(List<Cuenta>)` y `TransaccionService.transferir(Cuenta, ...)` aceptan un `CDT` sin que el compilador lo impida. | Si un CDT entra en la lista del cobro de la cuota de manejo, el proceso se cae a mitad de camino y las cuentas que siguen en la lista quedan sin cobrar. Si se usa como origen de una transferencia, el cliente recibe un error inesperado en lugar de un mensaje claro. |




### 1.2 Dos experimentos

1. **El CDT.** Modifiquen temporalmente el programa principal para que el cobro de la cuota
  de manejo incluya el CDT de Ana. ¿Qué pasa? ¿Qué pasaría en producción si el proceso de
   cobro corre de noche para un millón de cuentas y la cuenta número 500 000 es un CDT?
2. **La prueba imposible.** Intenten escribir una prueba unitaria que verifique que una
  transferencia a otro banco cobra $7.500 de comisión, con una condición: la prueba no puede
   conectarse a Oracle ni enviar un SMS. ¿Lo lograron? ¿Qué les impide hacerlo?



#### Experimento 1: El CDT

Se cambió temporalmente la línea del cobro en `Main` por
`new CobroCuotaManejo().cobrarMensual(List.of(cdtAna, ana, luis));` y se ejecutó el programa:

```text
[ORACLE] Conectando a jdbc:oracle:thin:@prod-db:1521/BANCO...
[ORACLE] INSERT INTO transacciones VALUES ('001-1', '001-2', 150000.0, 7500.0)
===== BANCO ANDINO - COMPROBANTE =====
Origen: 001-1
Destino: 001-2
Monto: $150000.0
Comisión: $7500.0
======================================
[SMS] Conectando al proveedor de mensajería...
[SMS] Para Ana: Transferiste $150000.0 a la cuenta 001-2
[AUDITORIA] 2026-10-01T12:55:52.933907064 OTRO_BANCO 001-1 -> 001-2 $150000.0
Exception in thread "main" java.lang.UnsupportedOperationException: Un CDT no permite retiros antes del vencimiento
        at CDT.retirar(CDT.java:14)
        at CobroCuotaManejo.cobrarMensual(CobroCuotaManejo.java:8)
        at Main.main(Main.java:13)
```

**¿Qué pasa?** Un CDT es una `Cuenta` que bloquea el dinero hasta su fecha de vencimiento.
Al sobrescribir `retirar()` para lanzar `UnsupportedOperationException` antes de esa fecha, rompe el principio de sustitución de Liskov: `CobroCuotaManejo` trata a todas las cuentas por igual, pero el CDT se comporta de una forma que la clase padre no anuncia. La excepción no se maneja en ningún lado y detiene el programa completo: como el CDT estaba primero en la lista, no se le cobró la cuota ni a Ana ni a Luis, y tampoco se imprimieron los extractos de la tarjeta y del crédito de vivienda, que venían después en `Main`.

**¿Qué pasaría en producción?** Si el cobro corre de noche para un millón de cuentas y la número 500 000 es un CDT, el proceso se cae en esa cuenta y las cerca de 500 000 cuentas restantes quedan sin cobrar. Además, las 499 999 anteriores sí quedaron cobradas y no hay registro de dónde se detuvo el proceso: si alguien lo vuelve a correr desde el principio a la mañana siguiente, esos clientes pagan la cuota dos veces. El banco pierde ingresos de la mitad de sus clientes y le cobra doble a la otra mitad.

**Observación adicional:** el mismo bloqueo ocurre si una cuenta no tiene saldo, porque `Cuenta.retirar()` lanza `IllegalStateException` y `cobrarMensual()` tampoco la maneja. La diferencia es que esa excepción sí hace parte del contrato de `Cuenta`, así que el problema ahí es que el proceso de cobro no maneja errores, no un problema de Liskov. El CDT es más grave: aunque se corrigiera el cobro para atrapar `IllegalStateException`, que es la excepción que anuncia la clase padre, el CDT lanza un tipo de excepción distinto y el proceso se seguiría cayendo.

#### Experimento 2: La prueba imposible

Se intentó escribir la prueba con el código tal como está:

```java
public class PruebaComisionOtroBanco {
    public static void main(String[] args) {
        Cuenta origen = new CuentaAhorros("T-1", "Prueba", 1_000_000);
        Cuenta destino = new CuentaAhorros("T-2", "Prueba", 0);

        new TransaccionService().transferir(origen, destino, 100_000, "OTRO_BANCO");

        double comisionCobrada = 1_000_000 - 100_000 - origen.getSaldo();
        System.out.println(comisionCobrada == 7_500 ? "PASA: comisión = 7500" : "FALLA: " + comisionCobrada);
    }
}
```

Resultado:

```text
[ORACLE] Conectando a jdbc:oracle:thin:@prod-db:1521/BANCO...
[ORACLE] INSERT INTO transacciones VALUES ('T-1', 'T-2', 100000.0, 7500.0)
===== BANCO ANDINO - COMPROBANTE =====
...
[SMS] Conectando al proveedor de mensajería...
[SMS] Para Prueba: Transferiste $100000.0 a la cuenta T-2
[AUDITORIA] 2026-10-01T13:07:09.406860763 OTRO_BANCO T-1 -> T-2 $100000.0
PASA: comisión = 7500
```

**¿Lo lograron?** No. La comprobación de la comisión pasa, pero incumple la condición del experimento: para llegar a ella, la prueba se conectó a la base de datos de producción (`prod-db`), insertó una transacción falsa y le envió un SMS al "cliente". Con el código actual no es posible escribir una prueba unitaria de la comisión; cualquier prueba de `transferir()` termina siendo una prueba de integración, que es un tipo de prueba distinto y que además aquí ensucia producción.

**¿Qué les impide hacerlo?** Dos de los problemas de la tabla de hallazgos:

- **S (responsabilidad única):** el cálculo de la comisión no existe como una unidad que se pueda probar por separado. Es una variable local dentro de `transferir()`, mezclada con la persistencia y la notificación. No hay forma de pedir solo la comisión: hay que ejecutar la transferencia completa e inferirla de forma indirecta, restando saldos.
- **D (inversión de dependencias):** este es el impedimento directo. `TransaccionService` crea `OracleRepositorio` y `SmsGateway` con `new` dentro de la propia clase, y depende de clases concretas en lugar de interfaces. Por eso la prueba no puede reemplazarlas por dobles de prueba (por ejemplo, un repositorio en memoria y un notificador falso que no envíe nada). Aunque la comisión estuviera separada, `transferir()` seguiría yendo a Oracle y al SMS.



### 1.3 Medición "antes"


| Métrica                                                                     | Antes                                                                                                |
| --------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| Líneas del método `transferir`                                              | 23 (líneas de código, sin contar líneas en blanco ni comentarios)                                    |
| Número de razones distintas por las que `TransaccionService` podría cambiar | 7 (validación, comisión, movimiento del dinero, persistencia, comprobante, notificación y auditoría) |
| Clases concretas que `TransaccionService` crea con `new`                    | 2 (`OracleRepositorio` y `SmsGateway`)                                                               |
| Métodos vacíos o que lanzan excepción por "no aplica"                       | 3 (`TarjetaCredito.depositar()`, `CreditoVivienda.depositar()` y `CreditoVivienda.retirar()`)        |
| ¿Se puede probar `transferir` sin Oracle ni SMS? (Sí/No)                    | No (ver Experimento 2)                                                                               |




### 1.4 Diagrama de clases del código original

Dibujen el diagrama de clases UML del código base: clases, interfaces, herencia, implementación y dependencias (`new`). Puede ser a mano (foto) o con cualquier herramienta (draw.io, PlantUML, Mermaid, etc.). Marquen en rojo las dependencias o herencias que consideren problemáticas.

![Diagrama de clases del código original](media/clases.png)

## Bloque 2 — Refactorización

Corregir el sistema completo, un principio a la vez, sin cambiar su comportamiento.

Trabajen en el orden de los puntos de control. Al terminar cada uno: (1) ejecuten el programa y comparen la salida con `salida_original.txt`, (2) respondan la pregunta de control en su README y (3) hagan el commit.

> **Tip: compara la salida automáticamente.** En Linux o macOS: `diff salida_original.txt salida_nueva.txt`. En Windows (PowerShell): `Compare-Object (gc salida_original.txt) (gc salida_nueva.txt)`. Solo deberían cambiar la fecha y la hora de la auditoría. A esta técnica se le llama **prueba de caracterización**: antes de refactorizar código sin pruebas, se "congela" lo que hace hoy para detectar cualquier cambio accidental.



### Punto de control S

Separen las responsabilidades que hoy están mezcladas en `TransaccionService.transferir`.

**Pregunta de control:** Después del cambio, describan en una frase qué hace `TransaccionService`. ¿Aparece la palabra "y"? Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?

**Respuesta:**

- **¿Qué hace** `TransaccionService`**?** Orquesta la transferencia de una cuenta origen a una cuenta destino.
- **¿Aparece la palabra "y"?** No. La clase ya no valida, calcula, guarda, imprime y audita por su cuenta: solo coordina el orden de los pasos y delega cada uno en una clase con una única responsabilidad (`ValidadorTransaccion`, `CalculadoraComision`, `OracleRepositorio`, `ImpresoraComprobante`, `SmsGateway` y `Auditoria`).
- **Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?** Solo `ImpresoraComprobante.java`. `TransaccionService` y el código que mueve el dinero no se modifican.



### Punto de control O

Hoy, agregar un tipo de transferencia obliga a editar el `switch`. Cámbienlo para que un tipo nuevo se agregue creando código, no editando el existente.

**Pregunta de control:** Si mañana llega un tipo de transferencia nuevo, ¿qué archivos existentes tendrían que modificar? Enumérenlos. Lo ideal es que solo aparezca el punto donde se arma el sistema (el programa principal).

**Respuesta:** De los archivos existentes, solo se modifica `Main.java`, para crear la instancia del nuevo tipo y pasarla a `transferir()`. El tipo se define en una clase nueva que implementa la interfaz `TipoTransaccion` y sus dos métodos: `getNombre()`, que devuelve el nombre del tipo que se registra en la auditoría, y `calcularComision(double monto)`, que contiene la regla de comisión propia de ese tipo. Como `TransaccionService` trabaja con la interfaz y no con tipos concretos, ni ella ni los tipos ya existentes (`MismoBanco`, `OtroBanco` e `Internacional`) necesitan cambios.

### Punto de control L

Corrijan la jerarquía de cuentas para que el cobro de cuota de manejo nunca pueda explotar por culpa de un CDT.

**Pregunta de control:** ¿Su solución detecta el error al compilar (o con el verificador de tipos de su lenguaje) o al ejecutar? ¿Por qué es mejor lo primero? Si alguien propone "envolver el retiro en un `try`/`catch` e ignorar los CDT", ¿por qué eso no resuelve el problema de diseño?

**Respuesta:**

- **¿Al compilar o al ejecutar?** Al compilar. `CobroCuotaManejo.cobrarMensual()` ahora recibe una `List<CuentaTransaccional>`, y `CDT` hereda de `Cuenta` pero no de `CuentaTransaccional`. Si alguien intenta incluir un CDT en el cobro, el compilador rechaza el código con un error de tipos incompatibles y el programa ni siquiera llega a ejecutarse.
- **¿Por qué es mejor?** Porque el error aparece mientras se desarrolla, antes de que el código llegue a producción. Un error en tiempo de ejecución, en cambio, solo se manifiesta cuando se da el caso concreto (por ejemplo, un CDT en la posición 500 000 del cobro nocturno): puede pasar desapercibido en las pruebas y terminar afectando a los clientes con cobros incompletos o fallas en tiempo real.
- **¿Por qué un** `try`**/**`catch` **que ignore los CDT no resuelve el problema?** Porque solo oculta el síntoma. El diseño seguiría diciendo que un CDT es una cuenta que permite retiros, cuando en realidad no lo es. Cada clase que use cuentas tendría que recordar esa excepción y protegerse por su cuenta, y bastaría con que una sola lo olvidara para que el error volviera. Corregir la jerarquía, en cambio, deja explícito en los tipos qué cuentas permiten retiros y cuáles no, y el compilador se encarga de hacerlo cumplir.



### Punto de control I

Corrijan `ProductoBancario` para que ningún producto tenga que implementar métodos que no le aplican.

**Pregunta de control:** ¿Pudieron lograr que un mismo generador de extractos funcione para cuentas, tarjetas y créditos a la vez? ¿Qué interfaz necesitó para eso, y por qué no necesitó conocer los demás métodos de cada producto?

**Respuesta:**

- **¿Funciona para cuentas, tarjetas y créditos a la vez?** Sí. Un mismo recorrido sobre una `List<GeneradorExtracto>` genera el extracto de cualquier cuenta (incluido el CDT, que la hereda de `Cuenta`), de la tarjeta de crédito y del crédito de vivienda.
- **¿Qué interfaz necesitó?** La interfaz `GeneradorExtracto`, con un único método: `generarExtracto()`. La implementan `Cuenta`, `TarjetaCredito` y `CreditoVivienda`, cada una con su propio formato.
- **¿Por qué no necesitó conocer los demás métodos?** Porque es una interfaz segregada: contiene solo lo que todos los productos tienen en común, que es poder generar su extracto. Quien recorre los extractos depende únicamente de ese método, sin saber si el producto maneja saldo, deuda o intereses. Las operaciones propias de cada producto quedaron en otros tipos (`ProductoCredito` para `calcularIntereses()` y `pagarCuota()`, y `Cuenta`/`CuentaTransaccional` para `depositar()` y `retirar()`), así que ningún producto tiene que implementar métodos que no le aplican.



### Punto de control D

Hagan que `TransaccionService` deje de crear sus dependencias con `new` y que dependa de abstracciones. Todo el "armado" del sistema debe quedar en un solo lugar (el programa principal).

**Pregunta de control:** ¿Cuántas clases concretas conoce ahora `TransaccionService`? ¿Quién decide si se usa Oracle o si se notifica por SMS? Vuelvan al experimento 2 del bloque 1: ¿ya es posible esa prueba?

**Respuesta:**

- **¿Cuántas clases concretas conoce ahora** `TransaccionService`**?** Cuatro: `ValidadorTransaccion`, `CalculadoraComision`, `ImpresoraComprobante` y `Auditoria`. Ya no conoce las dos de infraestructura, `OracleRepositorio` y `SmsGateway`, que eran las que impedían probar la clase y cambiar de proveedor. Se invirtieron las dependencias volátiles, es decir, las que pueden tener varias implementaciones (la base de datos y el canal de notificación), mediante las interfaces `RepositorioTransacciones` y `Notificador`. Las otras cuatro son lógica interna estable con una sola implementación prevista, y crearles interfaces agregaría complejidad sin beneficio.
- **¿Quién decide si se usa Oracle o si se notifica por SMS?** `Main`, al construir el `TransaccionService`: `new TransaccionService(new OracleRepositorio(), new SmsGateway())`.
- **¿Ya es posible la prueba del experimento 2?** Sí. Como el repositorio y el notificador llegan por el constructor, la prueba puede pasarle a `TransaccionService` dobles de prueba que no se conectan a Oracle ni envían SMS:

```java
import java.util.ArrayList;
import java.util.List;

public class PruebaComisionOtroBanco {
    static class RepositorioFalso implements RepositorioTransacciones {
        final List<Double> comisiones = new ArrayList<>();

        @Override
        public void guardarTransaccion(String origen, String destino, double monto, double comision) {
            comisiones.add(comision);
        }
    }

    static class NotificadorFalso implements Notificador {
        int enviados = 0;

        @Override
        public void enviar(String destinatario, String mensaje) {
            enviados++;
        }
    }

    public static void main(String[] args) {
        RepositorioFalso repositorio = new RepositorioFalso();
        NotificadorFalso notificador = new NotificadorFalso();
        CuentaTransaccional origen = new CuentaAhorros("T-1", "Prueba", 1_000_000);
        Cuenta destino = new CuentaAhorros("T-2", "Prueba", 0);

        new TransaccionService(repositorio, notificador).transferir(origen, destino, 100_000, new OtroBanco());

        System.out.println(repositorio.comisiones.equals(List.of(7_500.0)) ? "PASA: comisión guardada = 7500" : "FALLA: " + repositorio.comisiones);
        System.out.println(origen.getSaldo() == 892_500 ? "PASA: saldo origen = 892500" : "FALLA: " + origen.getSaldo());
        System.out.println("SMS reales enviados: 0 (notificador falso recibió " + notificador.enviados + ")");
    }
}
```

Resultado:

```text
===== BANCO ANDINO - COMPROBANTE =====
Origen: T-1
Destino: T-2
Monto: $100000.0
Comisión: $7500.0
======================================
[AUDITORIA] 2026-10-02T22:24:32.701676169 OTRO_BANCO T-1 -> T-2 $100000.0
PASA: comisión guardada = 7500
PASA: saldo origen = 892500
SMS reales enviados: 0 (notificador falso recibió 1)
```

A diferencia del intento del bloque 1, ya no aparece ninguna línea `[ORACLE]` ni `[SMS]`: la prueba verifica la comisión de $7.500 sin conectarse a la base de datos de producción ni enviarle un mensaje al cliente.

## Bloque 3 — Pruebas unitarias

Demostrar que el diseño nuevo se puede probar sin base de datos, sin SMS y en milisegundos.

> **Dobles de prueba.** Un doble de prueba es una implementación falsa de una abstracción, hecha solo para las pruebas. Por ejemplo, un repositorio que guarda las transacciones en una lista en memoria en vez de en Oracle, o un notificador que anota los mensajes en vez de enviarlos. Solo es posible usarlos si la clase que se prueba depende de abstracciones (punto de control D). Si en su lenguaje usan un framework de mocks (Mockito, unittest.mock, Moq, Jest, etc.), también es válido.

Usen el framework de pruebas de su lenguaje (JUnit, pytest, xUnit, Jest, Vitest, flutter_test, XCTest, go test, etc.) y escriban como mínimo estas pruebas:

1. Una transferencia al mismo banco no cobra comisión y mueve exactamente el monto entre las dos cuentas.
2. Una transferencia a otro banco cobra $7.500 de comisión y descuenta monto + comisión de la cuenta de origen.
3. Si el saldo es insuficiente, la transferencia se rechaza y no se guarda nada ni se notifica al cliente.
4. Cada transferencia exitosa se guarda una sola vez y genera una sola notificación.
5. Un tipo de transferencia desconocido se rechaza y el saldo de la cuenta de origen no cambia.

**Pregunta de control:** ¿Cuánto tardan en ejecutarse todas sus pruebas? ¿Cuántas líneas de `TransaccionService` tuvieron que cambiar para poder probarla? ¿Qué habría pasado si intentaran estas mismas pruebas en el bloque 1?

**Respuesta:**

- **¿Cuánto tardan?** Las cinco pruebas de `TransaccionServiceTest` se ejecutan en unos 200 ms (entre 180 y 210 ms según la ejecución), sin conectarse a ninguna base de datos ni enviar mensajes.
- **¿Cuántas líneas de** `TransaccionService` **cambiaron?** Ninguna para poder probarla. La clase ya era testeable desde el punto de control D: como recibe el `RepositorioTransacciones` y el `Notificador` por el constructor, bastó con crear dos dobles de prueba (`RepositorioEnMemoria`, que guarda las transacciones en una lista, y `NotificadorFalso`, que anota los mensajes en vez de enviarlos) y pasárselos al construirla. La única línea que se tocó fue la llamada al validador, para que también reciba el tipo (ver la nota sobre la prueba 5); fue una corrección de comportamiento, no un requisito para poder probar la clase.
- **¿Qué habría pasado en el bloque 1?** No se habrían podido hacer como pruebas unitarias. `TransaccionService` creaba internamente `OracleRepositorio` y `SmsGateway` con `new`, así que la única opción era ejecutarlas con los servicios reales: cada prueba escribiría en la base de datos de producción y le enviaría un SMS al cliente, sería lenta y dependería de que esos servicios estuvieran disponibles. Además, no habría forma de verificar qué se guardó ni cuántas notificaciones se enviaron. La alternativa sería crear un duplicado completo de la clase con servicios falsos, pero entonces las pruebas verificarían esa copia y no el código real.
- **Nota sobre la prueba 5:** desde el punto de control O, el tipo de transferencia es un objeto que implementa `TipoTransaccion`, así que un tipo mal escrito o inexistente ya no compila. El único "tipo desconocido" que puede llegar en ejecución es `null`. Para rechazarlo de forma explícita, `ValidadorTransaccion.validar(monto, tipo)` lanza `IllegalArgumentException("Tipo de transferencia desconocido")`, el mismo mensaje que daba el `switch` original, antes de calcular la comisión o mover dinero. La prueba verifica ese rechazo y que los saldos no cambian, que no se guarda nada y que no se notifica al cliente.



## Bloque 4 — "Negocio pidió cambios"

Poner a prueba el diseño con requerimientos que no conocían.

> "Buenas tardes, equipo. Estos son los cambios priorizados para el próximo lanzamiento de la app. Gracias por su apoyo." — Gerencia de Canales Digitales

Para cada requerimiento:

1. Antes de programar, miren el código original (está en el commit `bloque-0-codigo-base`) y estimen cuántos archivos existentes habría que modificar allí.
2. Implementen el requerimiento sobre su código refactorizado.
3. Registren cuántos archivos existentes modificaron realmente y cuántos archivos nuevos crearon.
4. Ejecuten las pruebas del bloque 3: deben seguir pasando.



### Requerimientos


| Req. | Nombre                   | Descripción                                                                                                                                                                                                                               | Criterio de aceptación                                                                                                                                                           |
| ---- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| R1   | Transferencias por llave | Los clientes podrán transferir usando una llave (su número de celular o su cédula) en lugar del número de cuenta. Estas transferencias son inmediatas y no tienen comisión.                                                               | Una transferencia de tipo `LLAVE` por $50.000 descuenta exactamente $50.000 de la cuenta de origen. (No es necesario implementar la búsqueda de la cuenta a partir de la llave.) |
| R2   | Cuenta infantil          | Nuevo producto para menores de edad. Recibe depósitos sin límite, pero sus retiros no pueden superar $200.000 en un mismo día. Se debe poder usar como origen de transferencias y se le cobra la cuota de manejo como a cualquier cuenta. | Si la cuenta ya retiró $150.000 hoy, un retiro de $60.000 se rechaza y el saldo no cambia.                                                                                       |
| R3   | Notificaciones push      | Además del SMS, el cliente debe recibir una notificación push en la app por cada transferencia.                                                                                                                                           | Por cada transferencia exitosa aparecen en consola un mensaje `[SMS]` y un mensaje `[PUSH]`.                                                                                     |
| R4   | Sistema antifraude       | Por regulación, cada transacción exitosa debe enviarse al sistema antifraude del banco (simulado con un mensaje en consola que empiece con `[ANTIFRAUDE]`). La auditoría actual se mantiene.                                              | Por cada transferencia exitosa aparecen un mensaje `[AUDITORIA]` y uno `[ANTIFRAUDE]`. Una transferencia rechazada no genera ninguno.                                            |
| R5   | Migración a PostgreSQL   | El banco dejará de pagar la licencia de Oracle. Las transacciones se guardarán en PostgreSQL (simulado con `[POSTGRES]`). La clase de Oracle no se borra: se conserva por si hay que devolverse durante la migración.                     | El programa guarda en PostgreSQL y las pruebas unitarias no cambian.                                                                                                             |




### Registro de cambios


| Req. | Archivos a modificar en el código original (estimado)                                                                                  | Archivos existentes modificados (real)                    | Archivos nuevos                                                                        | ¿Se rompió alguna prueba? |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- | -------------------------------------------------------------------------------------- | ------------------------- |
| R1   | 1 (`TransaccionService.java`, para agregar un `case "LLAVE"` al `switch` de la comisión)                                               | 0                                                         | 2 (`Llave.java` y `test/LlaveTest.java`)                                               | No                        |
| R2   | 0 (bastaba con crear `CuentaInfantil.java` heredando de `Cuenta` y sobrescribiendo `retirar()`) aunque con su mala seguridad anterior. | 0                                                         | 2 (`CuentaInfantil.java` y `test/CuentaInfantilTest.java`)                             | No                        |
| R3   | 1 (`TransaccionService.java`, para crear un `PushGateway` con `new` y agregar la llamada después del SMS)                              | 1 (`Main.java`, para armar el notificador con SMS y push) | 3 (`PushGateway.java`, `NotificadorCompuesto.java` y `test/NotificacionPushTest.java`) | No                        |
| R4   |                                                                                                                                        |                                                           |                                                                                        |                           |
| R5   |                                                                                                                                        |                                                           |                                                                                        |                           |


**Para cada requerimiento se creo un archivo test para su criterio de aceptación y algunos extra que consideramos**

**Commits:** uno por requerimiento: `req-1`, `req-2`, `req-3`, `req-4` y `req-5`.