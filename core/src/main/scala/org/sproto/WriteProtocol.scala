package org.sproto

trait CanWrite[-That, -Writer] {

  def write(that: That, to: Writer)

}

trait CanConvertTo[That, Result] {

  def convert(that: That): Result

}

trait SeqWriter[Writer] {

  def writeElement[T](value: T)(implicit cw: CanWrite[T, Writer])

}

trait MapWriter[Writer] {

  def writeField[T](name: String, value: T)(implicit cw: CanWrite[T, Writer])

}

trait WriteProtocolLow {

  implicit def canWriteAsSeq[T, W](implicit conv: W => SeqWriter[W], cw: CanWrite[T, SeqWriter[W]]) = new CanWrite[T, W] {

    def write(that: T, writer: W) =
      cw.write(that, conv(writer))

  }

  implicit def canWriteAsMap[T, W](implicit conv: W => MapWriter[W], cw: CanWrite[T, MapWriter[W]]) = new CanWrite[T, W] {

    def write(that: T, writer: W) =
      cw.write(that, conv(writer))

  }

}

trait WriteProtocol extends WriteProtocolLow {

  def to[T, R](that: T)(implicit cct: CanConvertTo[T, R]) =
    cct.convert(that)

  def write[T, W](value: T, writer: W)(implicit cw: CanWrite[T, W]) =
    cw.write(value, writer)

  def writeElement[T, W](that: T, to: SeqWriter[W])(implicit cw: CanWrite[T, W]) =
    to.writeElement(that)

  def writeField[T, W](name: String, that: T, to: MapWriter[W])(implicit cw: CanWrite[T, W]) =
    to.writeField(name, that)

  implicit def canWriteTraversable[T, W](implicit cw: CanWrite[T, W]) = new CanWrite[Traversable[T], SeqWriter[W]] {

    def write(that: Traversable[T], writer: SeqWriter[W]) {
      that.foreach(writer.writeElement(_))
    }

  }

  def canWrite[T, W](f: (T, W) => Unit) = new CanWrite[T, W] {

    def write(that: T, to: W) = f(that, to)

  }

}
