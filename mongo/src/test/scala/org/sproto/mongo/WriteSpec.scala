package org.sproto.mongo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.sproto._
import org.sproto.mongo.MongoWriteProtocol._
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

@RunWith(classOf[JUnitRunner])
class WriteSpec extends Spec with ShouldMatchers {

  def dbList(elems: Any*) = {
    val l = new BasicDBList
    elems.foreach(x => l.add(x.asInstanceOf[AnyRef]))
    l
  }

  def dbObject(elems: (String, Any)*) = {
    val o = new BasicDBObject
    elems.foreach(x => o.put(x._1, x._2.asInstanceOf[AnyRef]))
    o
  }

  describe("String") {
    it("should be writen as is") {
      val w = new MongoWriter

      write("qwerty", w)

      w.result should equal("qwerty")
    }
    it("should be converted as is") {
      to("qwerty") should equal("qwerty")
    }
  }

  describe("Array") {
    it("of strings should be writed to any") {
      val w = new MongoWriter

      write(List("aaa", "bbb"), w)

      w.result should equal(dbList("aaa", "bbb"))
    }
    it("of strings should be converted to any") {
      (to(List("aaa", "bbb")): BasicDBList) should equal(dbList("aaa", "bbb"))
    }
    it("of integers should be writed to any") {
      val w = new MongoWriter

      write(List(1, 2, 3), w)

      w.result should equal(dbList(1, 2, 3))
    }
    it("of integers should be converted to any") {
      (to(List("aaa", "bbb")): BasicDBList) should equal(dbList("aaa", "bbb"))
    }
  }

  describe("Object") {
    val w = new MongoWriter

    case class Aaa(a: String, b: Int)

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[Aaa, MapWriter[MongoWriter]] {

        def write(that: Aaa, to: MapWriter[MongoWriter]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
        }

      }

      it("should be writed to any") {
        write(Aaa("ddd", 23), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa("ddd", 23)): BasicDBObject) should equal(dbObject("a" -> "ddd", "b" -> 23))
      }

      describe("with subobjects") {

        case class Bbb(x: Double, y: Aaa)

        implicit object canWriteBbb extends CanWrite[Bbb, MapWriter[MongoWriter]] {

          def write(that: Bbb, to: MapWriter[MongoWriter]) {
            writeField("x", that.x, to)
            writeField("y", that.y, to)
          }

        }

        it("should be writed to any") {
          write(Bbb(1.0, Aaa("ddd", 23)), w)
        }

        it("should be converted to BasicDBObject") {
          (to(Bbb(1.0, Aaa("ddd", 23))): BasicDBObject) should equal(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23)))
        }

      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[String, W], cwi: CanWrite[Int, W]) = new CanWrite[Aaa, MapWriter[W]] {

        def write(that: Aaa, to: MapWriter[W]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
        }

      }

      it("should be writed to any") {
        write(Aaa("ddd", 23), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa("ddd", 23)): BasicDBObject) should equal(dbObject("a" -> "ddd", "b" -> 23))
      }

      describe("with subobjects") {

        case class Bbb(x: Double, y: Aaa)

        implicit def canWriteBbb[W](implicit cwd: CanWrite[Double, W], cwa: CanWrite[Aaa, W]) = new CanWrite[Bbb, MapWriter[W]] {

          def write(that: Bbb, to: MapWriter[W]) {
            writeField("x", that.x, to)
            writeField("y", that.y, to)
          }

        }

        it("should be writed to any") {
          write(Bbb(1.0, Aaa("ddd", 23)), w)
        }

        it("should be converted to BasicDBObject") {
          (to(Bbb(1.0, Aaa("ddd", 23))): BasicDBObject) should equal(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23)))
        }

      }

    }

  }

  describe("Object with lists and sets") {
    val w = new MongoWriter

    case class Aaa(a: Set[String], b: List[Int])

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[Aaa, MapWriter[MongoWriter]] {

        def write(that: Aaa, to: MapWriter[MongoWriter]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
        }

      }

      it("should be writed to any") {
        write(Aaa(Set("ddd"), List(23, 11)), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa(Set("ddd"), List(23, 11))): BasicDBObject) should equal(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))
      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[Set[String], W], cwi: CanWrite[List[Int], W]) = new CanWrite[Aaa, MapWriter[W]] {

        def write(that: Aaa, to: MapWriter[W]) {
          writeField("a", that.a, to)
          writeField("b", that.b, to)
        }

      }

      it("should be writed to any") {
        write(Aaa(Set("ddd"), List(23, 11)), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa(Set("ddd"), List(23, 11))): BasicDBObject) should equal(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))
      }

    }

  }

}
