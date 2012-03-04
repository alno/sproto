package org.sproto

trait CanWrite[-That, -Writer] {

  def write(that: That, to: Writer)

}

trait SeqWriter[Writer] {

  def writeElement[T](value: T)(implicit cw: CanWrite[T, Writer])

}

trait MapWriter[Writer] {

  def writeField[T](name: String, value: T)(implicit cw: CanWrite[T, Writer])

}

trait WriteProtocol {

  def write[T, W, RW](value: T, writer: W)(implicit canWrite: CanWrite[T, RW], conv: W => RW) =
    canWrite.write(value, conv(writer))

  def writeElement[T, W](that: T, to: SeqWriter[W])(implicit cw: CanWrite[T, W]) =
    to.writeElement(that)

  def writeField[T, W](name: String, that: T, to: MapWriter[W])(implicit cw: CanWrite[T, W]) =
    to.writeField(name, that)

  implicit def canWriteTraversable[T, W](implicit cw: CanWrite[T, W], wl: W => SeqWriter[W]) = new CanWrite[Traversable[T], W] {

    def write(that: Traversable[T], writer: W) {
      val lw = wl(writer)

      that.foreach(lw.writeElement(_))
    }

  }

  def canWrite[T, W](f: (T, W) => Unit) = new CanWrite[T, W] {

    def write(that: T, to: W) = f(that, to)

  }

}
