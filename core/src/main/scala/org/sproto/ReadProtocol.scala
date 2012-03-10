package org.sproto

trait CanRead[+That, -Reader] {

  def read(from: Reader): That

}

trait CanConvertFrom[-Source, That] {

  def convertFrom(src: Source): That

}

trait ReadProtocol {

  def from[T, S](src: S)(implicit ccf: CanConvertFrom[S, T]) =
    ccf.convertFrom(src)

  def read[T, R](reader: R)(implicit cr: CanRead[T, R]) =
    cr.read(reader)

  def canRead[T, R](f: R => T) = new CanRead[T, R] {

    def read(from: R) = f(from)

  }

}
