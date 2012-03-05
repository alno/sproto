package org.sproto

trait SeqReader[Reader] {

  def hasElement: Boolean

  def readElement[T](implicit cr: CanRead[T, Reader]): T

}

trait ReadSeqSupportLow {

  implicit def canReadAsSeq[T, R](implicit conv: R => SeqReader[R], cr: CanRead[T, SeqReader[R]]) = new CanRead[T, R] {

    def read(from: R) =
      cr.read(conv(from))

  }

}

trait ReadSeqSupport extends ReadSeqSupportLow {

  def readElement[T, R](from: SeqReader[R])(implicit cr: CanRead[T, R]) =
    from.readElement(cr)

  implicit def canReadList[T, R](implicit cr: CanRead[T, R]) = new CanRead[List[T], SeqReader[R]] {

    def read(from: SeqReader[R]) = {
      val b = List.newBuilder[T]

      while (from.hasElement)
        b += from.readElement

      b.result
    }

  }

  implicit def canReadSet[T, R](implicit cr: CanRead[T, R]) = new CanRead[Set[T], SeqReader[R]] {

    def read(from: SeqReader[R]) = {
      val b = Set.newBuilder[T]

      while (from.hasElement)
        b += from.readElement

      b.result
    }

  }

}
