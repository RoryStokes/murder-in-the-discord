package es.rorystok.mitd

import diode.{Circuit, ModelR}

object Util {
  def subscribeZipped[M <: AnyRef, A, B](circuit: Circuit[M], a: ModelR[M,A], b: ModelR[M,B])(handler: (A, B) => Unit): Unit = {
    def handle(in: Any): Unit = handler(a.value, b.value)
    circuit.subscribe(a)(handle)
    circuit.subscribe(b)(handle)
  }
}
