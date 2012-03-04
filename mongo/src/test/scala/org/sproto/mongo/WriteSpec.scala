package org.sproto.mongo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.sproto._
import org.sproto.mongo.MongoWriteProtocol._
import org.bson.types.BasicBSONList
import org.bson.BasicBSONObject

@RunWith(classOf[JUnitRunner])
class WriteSpec extends Spec with ShouldMatchers {

  def dbList(elems: Any*) = {
    val l = new BasicBSONList
    elems.foreach(x => l.add(x.asInstanceOf[AnyRef]))
    l
  }

  def dbObject(elems: (String, Any)*) = {
    val o = new BasicBSONObject
    elems.foreach(x => o.put(x._1, x._2.asInstanceOf[AnyRef]))
    o
  }

  describe("String") {
    it("should be writen as is") {
      val w = new MongoWriter

      write("qwerty", w)

      w.result should equal("qwerty")
    }
  }

  describe("Array") {
     it("of strings should be writed to any") {
      val w = new MongoWriter

      write(List("aaa", "bbb"), w)

      w.result should equal(dbList("aaa", "bbb"))
    }
    it("of integers should be writed to any") {
      val w = new MongoWriter

      write(List(1, 2, 3), w)

      w.result should equal(dbList(1, 2, 3))
    }
  }

  describe("Object") {
    val w = new MongoWriter

    case class Aaa(a: String, b: Int)

    describe("with specialized writing") {

	    implicit object canWriteAaa extends CanWrite[Aaa, MongoObjectWriter] {

	      def write(that: Aaa, to: MongoObjectWriter) {
	        writeField("a", that.a, to)
	        writeField("b", that.b, to)
	      }

	    }

	    it("should be writed to any") {
	      write(Aaa("ddd", 23), w)
	    }

    }

    describe("with universal writing") {

	    implicit def canWriteAaa[W](implicit cws: CanWrite[String, W], cwi: CanWrite[Int,W]) = new CanWrite[Aaa, MapWriter[W]] {

	      def write(that: Aaa, to: MapWriter[W]) {
	        writeField("a", that.a, to)
	        writeField("b", that.b, to)
	      }

	    }

	    it("should be writed to any") {
	      write(Aaa("ddd", 23), w)
	    }

    }

  }

  describe("Object with lists and sets") {
    val w = new MongoWriter

    case class Aaa(a: Set[String], b: List[Int])

    describe("with specialized writing") {

	    implicit object canWriteAaa extends CanWrite[Aaa, MongoObjectWriter] {

	      def write(that: Aaa, to: MongoObjectWriter) {
	        writeField("a", that.a, to)
	        writeField("b", that.b, to)
	      }

	    }

	    it("should be writed to any") {
	      write(Aaa(Set("ddd"), List(23,11)), w)
	    }

    }

    describe("with universal writing") {

	    implicit def canWriteAaa[W](implicit cws: CanWrite[String, W], cwi: CanWrite[Int,W], lc: W => SeqWriter[W]) = new CanWrite[Aaa, MapWriter[W]] {

	      def write(that: Aaa, to: MapWriter[W]) {
	        writeField("a", that.a, to)
	        writeField("b", that.b, to)
	      }

	    }

	    it("should be writed to any") {
	      write(Aaa(Set("ddd"), List(23,11)), w)
	    }

    }

  }

}
