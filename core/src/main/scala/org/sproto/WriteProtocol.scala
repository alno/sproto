package org.sproto

trait CanWrite[-That, -Writer] {

  def write(that: That, to: Writer)

}

trait CanConvertTo[That, Result] {

  def convertTo(that: That): Result

}

trait WriteProtocol {

  def to[T, R](that: T)(implicit cct: CanConvertTo[T, R]) =
    cct.convertTo(that)

  def write[T, W](value: T, writer: W)(implicit cw: CanWrite[T, W]) =
    cw.write(value, writer)

  def canWrite[T, W](f: (T, W) => Unit) = new CanWrite[T, W] {

    def write(that: T, to: W) = f(that, to)

  }

}
