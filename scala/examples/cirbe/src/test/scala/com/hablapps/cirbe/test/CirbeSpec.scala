package com.hablapps.cirbe.test

import org.scalatest._

import scalaz._, Scalaz._

import com.hablapps.cirbe.{ dominio, negocio, proceso }
import dominio._, dominio.{ Crgopes, Id }
import negocio.Validaciones._
import proceso.validacion._, proceso.crgopes._, proceso.crgopes.Crgopes._
import proceso.crgopes.Crgopes.test
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

    val proceso = "09-2015"
    val crgopes = "09-2015"

    val id1: Id[DB010] = "DB010_1234_ABCD"
    val id2: Id[DB020] = "DB020_ABCD"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(proceso)),
      procesos = Map(proceso -> Proceso(proceso, crgopes)),
      crgopes  = Map(crgopes -> Crgopes(
        nombre = crgopes,
        db010s = List(id1),
        db020s = List(id2))),
      db010s = Map(id1 -> DB010(Relacion("1234", "ABCD"))),
      db020s = Map(id2 -> DB020(Operacion("ABCD", V40, ZZZ))))

    val programa = for {
      _ <- validar(id1: Id[DB010])
      _ <- validar(id2: Id[DB020])
    } yield ()

    val resultado = (programa.foldMap(toState).exec(inicial)
      | fail("No se esperaba ningún error en la ejecución de este programa"))

    resultado.db010s(id1).errores shouldBe List()
    resultado.db020s(id2).errores shouldBe List(R2008)
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

    val proceso = "09-2015"
    val crgopes = "09-2015"

    val id1: Id[DB010] = "DB010_1234_ABCD"
    val id2: Id[DB020] = "DB020_ABCD"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(proceso)),
      procesos = Map(proceso -> Proceso(proceso, crgopes)),
      crgopes  = Map(crgopes -> Crgopes(
        nombre = crgopes,
        db010s = List(id1),
        db020s = List(id2))),
      db010s = Map(id1 -> DB010(Relacion("1234", "ABCD"))),
      db020s = Map(id2 -> DB020(Operacion("ABCD", V40, ZZZ))))

    val programa = for {
      _     <- validar(id1: Id[DB010])
      _     <- validar(id2: Id[DB020])
    } yield ()

    val resultado = (programa.foldMap(toState).exec(inicial)
      | fail("No se esperaba ningún error en la ejecución de este programa"))

    val esperado = inicial.copy(
      db020s = Map("DB020_ABCD" -> DB020(
        Operacion("ABCD", V40, ZZZ), List(R2008))))

    resultado shouldBe esperado
  }

  // Esta versión utiliza la misma aproximación que la versión inicial, que
  // comparaba valores puntuales extraídos de un estado final. El enfoque, sin
  // embargo, es diferente, ya que la comprobación de que la lógica es correcta
  // se hace desde el interior de la for comprehension, mediante la instrucción
  // "test". Si la condición no se cumple, se emitirá un mensaje de error.
  // ¿Qué ganamos con esta alternativa? Pues bien, seguimos con los mismos
  // problemas que ya quedaron descritos en la versión inicial, pero obtenemos
  // una ganancia en el hecho de que no tenemos por qué conocer el estado
  // interno para extraer los valores puntuales. Es una solución más integrada,
  // a costa de acoplar una instrucción (a mi juicio) artificial entre nuestro
  // repertorio de primitivas.
  it should "hacer checks contra valores puntuales de forma nativa" in {

    val proceso = "09-2015"
    val crgopes = "09-2015"

    val id1: Id[DB010] = "DB010_1234_ABCD"
    val id2: Id[DB020] = "DB020_ABCD"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(proceso)),
      procesos = Map(proceso -> Proceso(proceso, crgopes)),
      crgopes  = Map(crgopes -> Crgopes(
        nombre = crgopes,
        db010s = List(id1),
        db020s = List(id2))),
      db010s = Map(id1 -> DB010(Relacion("1234", "ABCD"))),
      db020s = Map(id2 -> DB020(Operacion("ABCD", V40, ZZZ))))

    val programa = for {
      _  <- validar(id1)
      _  <- validar(id2)
      r1 <- getRegistro(id1)
      r2 <- getRegistro(id2)
      _  <- test(r1.errores shouldBe List.empty)
      _  <- test(r2.errores should equal (List(R2008)))
    } yield ()

    programa.foldMap(toState).exec(inicial)
  }

  it should "permitir finalizar un proceso activo" in {

    val envio = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(envio)),
      procesos = Map(envio -> Proceso(envio, envio)),
      crgopes  = Map(envio -> Crgopes(envio, Activo)))

    val programa = for {
      _       <- finalizar(envio)
      crgopes <- getCrgopes(envio)
      _       <- test(crgopes.get.estado should === (Finalizado))
    } yield ()

    programa.foldMap(toState).exec(inicial)
  }

  it should "fallar con declaración finalizada" in {

    val proceso = "09-2015"
    val crgopes = "09-2015"

    val inicial = Estado(
      cirbe    = Cirbe(procesos = List(proceso)),
      procesos = Map(proceso -> Proceso(proceso, crgopes)),
      crgopes  = Map(crgopes -> Crgopes(crgopes, Finalizado)))

    val programa = declarar(DB020(Operacion("ABCD", V40, ZZZ)), crgopes)

    (programa.foldMap(toState).exec(inicial).swap
      | fail("Se esperaba que la ejecución del programa generase un error"))
  }

  it should "fallar con declaración finalizada (auto)" in {

    import Precond._

    val precond: PrecondDSL[Id[Proceso]] = for {
      id <- requiereProceso(Option("201510"), _.crgopes == "201510")
    } yield id

    pending

    // TODO: forall(precond.toGen) { inicial => ... }

    val inicial = ???

    val programa = declarar(DB020(Operacion("ABCD", V40, ZZZ)), "201510")

    (programa.foldMap(toState).exec(inicial).swap
      | fail("Se esperaba que la ejecución del programa generase un error"))
  }
}
