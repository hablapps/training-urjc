package com.hablapps.cirbe.test

import org.scalatest._

import scalaz._, Scalaz._

import com.hablapps.cirbe.{ dominio, negocio, proceso }
import dominio._, dominio.{ Crgopes, Id }
import negocio.Validaciones._
import proceso.validacion._, proceso.crgopes._, proceso.crgopes.Crgopes._
import StateInterpreter._

class CirbeSpec extends FlatSpec with Matchers {

  // Este formato de test es bastante habitual. En primer lugar, se crea el
  // estado `inicial` manualmente. Después se declara el `programa` que se desea
  // probar. Finalmente, se obtiene el resultado de interpretar éste último y se
  // procede a realizar las comprobaciones pertinentes, aquellas donde el
  // programador espera que se hayan producido cambios. Este estilo tan habitual
  // cuenta con varias limitaciones que a continuación describiremos.
  //
  // 1) En este caso, el estado inicial es bastante sencillo, pero su
  // elaboración puede complicarse sobremanera si queremos partir de estados más
  // ricos, algo bastante habitual en este tipo de aplicaciones. Además, ante
  // tal situación y como consecuencia de la complejidad atribuida, es muy
  // posible que el programador declare un estado inicial inconsistente o
  // erróneo. ¿Sería posible generar automáticamente estados iniciales que
  // cumplan las precondiciones necesarias para poder lanzar nuestos escenarios?
  //
  // 2) Si por norma general crear un estado inicial manualmente es costoso, lo
  // habitual es que generar un estado final lo sea aún más. Es por eso por lo
  // que en este ejemplo se adopta la estrategia de probar sólo lo más
  // importante, es decir aquellos elementos que el programador sabe que deben
  // haber cambiado. ¿Cuál es el problema de esta aproximación? El principal
  // problema es que junto con los cambios esperados puedan haberse lanzado
  // otros con los que no contábamos y que por tanto sean erróneos (o no).
  "Cirbe" should "comparar contra valores puntuales del estado" in {

    val envio = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(envio)),
      procesos = Map(envio -> Proceso(envio)),
      crgopes  = Map(envio -> Crgopes(
        db010s = List("DB010_1234_ABCD"),
        db020s = List("DB020_ABCD"))),
      db010s = Map("DB010_1234_ABCD" -> DB010(Relacion("1234", "ABCD"))),
      db020s = Map("DB020_ABCD" -> DB020(Operacion("ABCD", V40, ZZZ))))

    val programa = for {
      _ <- validar("DB010_1234_ABCD": Id[DB010])
      _ <- validar("DB020_ABCD": Id[DB020])
    } yield ()

    val resultado = programa.foldMap(toState).exec(inicial)

    resultado.db010s("DB010_1234_ABCD").errores shouldBe List()
    resultado.db020s("DB020_ABCD").errores shouldBe List(R2008)
  }

  // Esta versión cambio respecto la anterior en cuanto a la forma de contrastar
  // el resultado final. En esta ocasión tenemos la garantía de que el resultado
  // final es correcto. Para ello hemos tenido que definir manualmente el estado
  // final esperado. En esta ocasión el cambio es mínimo, por lo que no resulta
  // complejo calcular nuestro estado esperado en función del estado inicial,
  // pero podría no ser el caso. Con una solución inmutable, rápidamente
  // tendremos que recurrir a librerías tipo `Lenses` para actualizar los
  // valores.
  it should "comparar contra estado completo" in {

    val envio = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(envio)),
      procesos = Map(envio -> Proceso(envio)),
      crgopes  = Map(envio -> Crgopes(
        db010s = List("DB010_1234_ABCD"),
        db020s = List("DB020_ABCD"))),
      db010s = Map("DB010_1234_ABCD" -> DB010(Relacion("1234", "ABCD"))),
      db020s = Map("DB020_ABCD" -> DB020(Operacion("ABCD", V40, ZZZ))))

    val programa = for {
      _     <- validar("DB010_1234_ABCD": Id[DB010])
      _     <- validar("DB020_ABCD": Id[DB020])
    } yield ()

    val resultado = programa.foldMap(toState).exec(inicial)

    val esperado = inicial.copy(
      db020s = Map("DB020_ABCD" -> DB020(
        Operacion("ABCD", V40, ZZZ), List(R2008))))

    resultado shouldBe esperado
  }

  // Esta versión utiliza la misma aproximación que la versión inicial, que
  // comparaba valores puntuales extraídos de un estado final. El enfoque, sin
  // embargo, es diferente, ya que la comprobación de que la lógica es correcta
  // se hace desde el interior de la for comprehension, mediante la instrucción
  // "aseverar" (~ assert). Si la condición no se cumple, se emitirá un mensaje
  // de error. ¿Qué ganamos con esta alternativa? Pues bien, seguimos con los
  // mismos problemas que ya quedaron descritos en la versión inicial, pero
  // obtenemos una ganancia en el hecho de que no tenemos por qué conocer el
  // estado interno para extraer los valores puntuales. Es una solución más
  // integrada, a costa de acoplar una instrucción (a mi juicio) artificial
  // entre nuestro repertorio de primitivas.
  it should "hacer checks contra valores puntuales de forma nativa" in {

    val envio = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(envio)),
      procesos = Map(envio -> Proceso(envio)),
      crgopes  = Map(envio -> Crgopes(
        db010s = List("DB010_1234_ABCD"),
        db020s = List("DB020_ABCD"))),
      db010s = Map("DB010_1234_ABCD" -> DB010(Relacion("1234", "ABCD"))),
      db020s = Map("DB020_ABCD" -> DB020(Operacion("ABCD", V40, ZZZ))))

    val id1: Id[DB010] = "DB010_1234_ABCD"
    val id2: Id[DB020] = "DB020_ABCD"

    val programa = for {
      _  <- validar(id1)
      _  <- validar(id2)
      r1 <- getRegistro(id1)
      r2 <- getRegistro(id2)
      _  <- aseverar(r1.errores == List.empty)
      _  <- aseverar(r2.errores == List(R2008), "Se debería generar 'R2008'")
    } yield ()

    programa.foldMap(toState).exec(inicial)
  }

  it should "permitir finalizar un proceso activo" in {

    val envio = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(envio)),
      procesos = Map(envio -> Proceso(envio)),
      crgopes  = Map(envio -> Crgopes(Activo)))

    val programa = for {
      _       <- declarar(Finalizar(), envio)
      crgopes <- getCrgopes(envio)
      _       <- aseverar(crgopes.get.estado == Finalizado)
    } yield ()

    programa.foldMap(toState).exec(inicial)
  }

  it should "fallar con declaración finalizada" in {

    val envio = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(envio)),
      procesos = Map(envio -> Proceso(envio)),
      crgopes  = Map(envio -> Crgopes(Finalizado)))

    val programa = declarar(DB020(Operacion("ABCD", V40, ZZZ)), envio)

    intercept[Error] {
      programa.foldMap(toState).exec(inicial)
    }
  }
}
