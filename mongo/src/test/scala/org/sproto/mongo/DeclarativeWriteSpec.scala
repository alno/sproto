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
class DeclarativeWriteSpec extends Spec with ShouldMatchers with Helpers {

  describe("Object") {

    describe("with specialized writing") {

      implicit val canWriteAaa = canWriteAsProduct((p: ObjSimple) => ObjSimple.unapply(p).get)("a", "b", "c")

      shoudBeWritedAndConvertedToDBObject(ObjSimple("ddd", 23, true), dbObject("a" -> "ddd", "b" -> 23, "c" -> true))

      describe("with subobjects") {

        implicit val canWriteBbb = canWriteAsProduct((p: ObjWithSub) => ObjWithSub.unapply(p).get)("x", "y")

        shoudBeWritedAndConvertedToDBObject(ObjWithSub(1.0, ObjSimple("ddd", 23, false)), dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> false)))

      }

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[String, W], cwi: CanWrite[Int, W], cwb: CanWrite[Boolean, W]): CanWrite[ObjSimple, MapWriter[W]] = canWriteAsProduct((p: ObjSimple) => ObjSimple.unapply(p).get)("a", "b", "c")

      shoudBeWritedAndConvertedToDBObject(ObjSimple("ddd", 23, false), dbObject("a" -> "ddd", "b" -> 23, "c" -> false))

      describe("with subobjects") {

        implicit def canWriteBbb[W](implicit cwd: CanWrite[Double, W], cwa: CanWrite[ObjSimple, W]): CanWrite[ObjWithSub, MapWriter[W]] = canWriteAsProduct((p: ObjWithSub) => ObjWithSub.unapply(p).get)("x", "y")

        shoudBeWritedAndConvertedToDBObject(ObjWithSub(1.0, ObjSimple("ddd", 23, true)), dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> true)))

      }

    }

  }

  describe("Object with lists and sets") {

    describe("with specialized writing") {

      implicit val canWriteAaa: CanWrite[ObjWithSets, MapWriter[MongoWriter]] = canWriteAsProduct((p: ObjWithSets) => ObjWithSets.unapply(p).get)("a", "b")

      shoudBeWritedAndConvertedToDBObject(ObjWithSets(Set("ddd"), List(23, 11)), dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))

    }

    describe("with universal writing") {

      implicit def canWriteAaa[W](implicit cws: CanWrite[Set[String], W], cwi: CanWrite[List[Int], W]): CanWrite[ObjWithSets, MapWriter[W]] = canWriteAsProduct((p: ObjWithSets) => ObjWithSets.unapply(p).get)("a", "b")

      shoudBeWritedAndConvertedToDBObject(ObjWithSets(Set("ddd"), List(23, 11)), dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)))

    }

  }

  describe("Recursive object") {
    val w = new MongoWriter

    case class Aaa(a: Option[Aaa])

    describe("with specialized writing") {

      implicit object canWriteAaa extends CanWrite[Aaa, MapWriter[MongoWriter]] {

        def write(that: Aaa, to: MapWriter[MongoWriter]) {
          that.a.foreach(writeField("a", _, to))
        }

      }

      it("should be writed to any") {
        write(Aaa(Some(Aaa(None))), w)
      }

      it("should be converted to BasicDBObject") {
        (to(Aaa(Some(Aaa(None)))): BasicDBObject) should equal(dbObject("a" -> dbObject()))
      }

    }

  }

}
