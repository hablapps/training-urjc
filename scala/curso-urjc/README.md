# Programación Funcional en Scala

* **Descripción:** Este curso pretende que el alumno adquiera las
  competencias básicas necesarias para afrontar el desarrollo de un
  producto software funcionalmente puro. Esta pureza, que se
  manifiesta en la ausencia de efectos de lado, permite el desarrollo
  de software componible, reutilizable y fácil de se probado. Se
  utilizará Scala como vehículo, un lenguaje relativamente nuevo, que
  combina la orientación a objetos con la programación funcional, y
  que está adquiriendo una notable relevancia en los últimos
  años. Esta popularidad se debe en gran parte al éxito de algunos de
  sus productos. Entre ellos destaca Play!, un framework web que será
  utilizado a lo largo del curso. Aunque las primeras semanas tendrán
  una componente de teoría bastante considerable, se hará un enorme
  empeño en abordar los problemas desde un punto de vista
  eminentemente práctico.
* **Duración:** 30 horas
* **Perfil:** el perfil recomendable para acceder a este curso es el
  de estudiante de informática en últimos años de carrera con
  conocimientos en un lenguaje orientado a objetos, preferiblemente
  Java.
* **Requisitos:** es recomendable que el alumno disponga de un
  ordenador portátil.

## Temario

### Introducción a Scala

En esta primera sesión se pretende, en primer lugar, introducir al
alumno en el ecosistema de Scala. Para ello, será necesario realizar
la instalación de la herramienta de construcción `sbt` que permitirá
la descarga de dependencias, la ejecución de un programa, la ejecución
de las pruebas e incluso el acceso a la REPL, herramienta
indispensable para trabajar con este lenguaje. También se procurará
que el alumno adquiera las nociones básicas sobre la sintaxis de
Scala.

### Introducción a la Programación Funcional

El segundo módulo adentra al alumno en el paradigma funcional. Se
comenzará con la premisa más básica, la función pura, pero también se
atacarán otros conceptos indirectos como la inmutabilidad, la
transparencia referencial o los tipos algebraicos de datos. Esta
semana tiene el objetivo de que el asistente sea consciente de la
importancia de la abstracción, la composición y la reusabilidad y cómo
la programación funcional despliega los mecanismos pertinentes para
poder lidiar con ellos.

### Constructores de Tipos, Type Classes y Funtores

En este módulo el alumno se acostumbrará a pensar en términos de
*kinds* trabajando con constructores de tipos y *type
classes*. Después se abordará una de las abstracciones de mayor
relevancia en el paradigma funcional: los *funtores*.

### ¡No tengas miedo de las mónadas!

La mónada se ha convertido en una abstracción extremadamente popular,
por su elegancia para introducir "efectos" de una forma segura en el
paradigma funcional. No obstante, también se ha convertido en un
concepto muy temido entre los programadores noveles debido a su
complejidad, que muchas veces viene motivada por la existencia de
tutoriales de dudosa calidad. En este curso, se pretende introducir el
concepto a base de varios ejemplos que parten de una motivación, y
sólo después encontrar el patrón común que les une a todos ellos.

### Efectos, EDSLs e Interpretación

Uno de los puntos fuertes de la programación funcional es el de la
separación de conceptos. Veremos cómo la distinción entre
*descripción* e *interpretación* nos ofrece un mecanismo
verdaderamente potente para afrontar diversas situaciones. De forma
indirecta, el alumno aprenderá los fundamentos de los *Embedded Domain
Specific Language (EDSLs)*, técnica muy popular que se logra gracias a
la flexibilidad que ofrece la sintaxis de un lenguaje como Scala.

### Introducción a Play

Play es uno de los proyectos más potentes del ecosistema Scala y ha
sido gran responsable del auge que este lenguaje ha adquirido en los
últimos años. El alumno obtendrá unas nociones básicas sobre Play,
Futures e Iteratees que establecerán la base que le permita adentrarse
en las entrañas de este framework.

### Desplegando Servicios en Play

Unas jornadas eminentemente prácticas para poner en la sartén todos
los conceptos que se han aprendido a lo largo del curso. El resultado
será una pequeña aplicación web en la que se hará un fuerte empeño en
mantener la pureza. En vistas a probar nuestra aplicación, se
trabajará con la herramienta ScalaTest, perfectamente integrada en
SBT.

### Scala @ Real Life

En esta última jornada se pretende traer a profesionales del mundo
Scala para que hablen de sus experiencias con este lenguaje en
particular y con la programación funcional en general. El objetivo
principal de esta sesión es que el alumno conozca el estado del
mercado laboral sobre esta tecnología en nuestro país y que le resulte
más sencillo orientarse profesionalmente hacia este sector.
