package org.sproto.mongo

import org.sproto._

import org.bson.types.BasicBSONList
import org.bson.BasicBSONObject

class MongoWriter {

  var result: Any = null

}

class MongoObjectWriter extends MapWriter[MongoWriter] {

  val result = new BasicBSONObject

  def writeField[T](name: String, value: T)(implicit canWrite: CanWrite[T, MongoWriter]) {
    val anyWriter = new MongoWriter // TODO Reusing?
    canWrite.write(value, anyWriter)
    result.put(name, anyWriter.result.asInstanceOf[AnyRef])
  }

}

class MongoListWriter extends SeqWriter[MongoWriter] {

  val result = new BasicBSONList

  def writeElement[T](value: T)(implicit canWrite: CanWrite[T, MongoWriter]) {
    val anyWriter = new MongoWriter // TODO Reusing?
    canWrite.write(value, anyWriter)
    result.add(anyWriter.result.asInstanceOf[AnyRef])
  }

}

trait MongoWriteProtocol extends WriteProtocol {

  implicit def toObjectWriter(w: MongoWriter) = {
    val r = new MongoObjectWriter
    w.result = r.result
    r
  }

  implicit def toListWriter(w: MongoWriter) = {
    val r = new MongoListWriter
    w.result = r.result
    r
  }

  def canWriteDirect[T] = new CanWrite[T,MongoWriter] {
    def write(that: T, to: MongoWriter) {
      to.result = that
    }
  }

  implicit val canWriteString = canWriteDirect[String]
  implicit val canWriteInt = canWriteDirect[Int]
  implicit val canWriteDouble = canWriteDirect[Double]
  implicit val canWriteNumber = canWriteDirect[Number]

}

object MongoWriteProtocol extends MongoWriteProtocol
