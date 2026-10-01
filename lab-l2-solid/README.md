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

| Bloque | Qué van a hacer |
| --- | --- |
| 0. Arranque | Preparar el proyecto y conocer el dominio |
| 1. Diagnóstico | Encontrar los problemas y medir el "antes" |
| 2. Refactorización | Corregir el sistema con puntos de control S, O, L, I, D |
| 3. Pruebas unitarias | Probar el diseño nuevo con dobles de prueba |
| 4. Negocio pidió cambios | Implementar requerimientos que no conocían |
| 5. Revisión cruzada | Extender el código de otra pareja |
| 6. Cierre | UML final, comparación y reflexión |

## El contexto

> "Bienvenidos al equipo de banca móvil de Banco Andino. La app lleva dos años en
> producción y los clientes la usan todos los días. El problema es el backend: cada cambio
> que pide el área de negocio tarda semanas, rompe algo que no tenía nada que ver y nadie
> se atreve a tocar la clase `TransaccionService`. El desarrollador que la escribió ya no está.
> Ustedes la heredaron."

### Glosario bancario

| Término | Significado en este laboratorio |
| --- | --- |
| Cuenta de ahorros | Cuenta en la que el cliente deposita, retira y transfiere dinero libremente. |
| CDT | Certificado de Depósito a Término. El cliente deja un dinero "congelado" hasta una fecha de vencimiento a cambio de intereses. No permite retiros antes de esa fecha. |
| Cuota de manejo | Valor mensual que el banco cobra por mantener una cuenta. |
| Transferencia interbancaria | Transferencia hacia una cuenta de otro banco. Tiene comisión. |
| Avance | Retiro de efectivo con cargo al cupo de una tarjeta de crédito. |
| Extracto | Resumen del estado de un producto (saldo, deuda, etc.). |

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
