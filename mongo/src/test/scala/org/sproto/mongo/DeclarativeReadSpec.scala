package org.sproto.mongo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.sproto._
import org.sproto.mongo.MongoReadProtocol._
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

@RunWith(classOf[JUnitRunner])
class DeclarativeReadSpec extends Spec with ShouldMatchers with Helpers {

  describe("Object") {

    describe("with specialized reading") {

      implicit val canReadAaa: CanRead[ObjSimple, MapReader[MongoReader]] = canReadAsProduct(ObjSimple)("a", "b", "c")

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> "ddd", "b" -> 23, "c" -> true), ObjSimple("ddd", 23, true))

      describe("with subobjects") {

        implicit val canReadBbb: CanRead[ObjWithSub, MapReader[MongoReader]] = canReadAsProduct(ObjWithSub)("x", "y")

        shoudBeReadAndConvertedFromDBObject(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> false)), ObjWithSub(1.0, ObjSimple("ddd", 23, false)))

      }

    }

    describe("with universal reading") {

      implicit def canReadAaa[W](implicit cws: CanRead[String, W], cwi: CanRead[Int, W], cwb: CanRead[Boolean, W]): CanRead[ObjSimple, MapReader[W]] = canReadAsProduct(ObjSimple)("a", "b", "c")

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> "ddd", "b" -> 23, "c" -> false), ObjSimple("ddd", 23, false))

      describe("with subobjects") {

        implicit def canReadBbb[W](implicit cwd: CanRead[Double, W], cwa: CanRead[ObjSimple, W]): CanRead[ObjWithSub, MapReader[W]] = canReadAsProduct(ObjWithSub)("x", "y")

        shoudBeReadAndConvertedFromDBObject(dbObject("x" -> 1.0, "y" -> dbObject("a" -> "ddd", "b" -> 23, "c" -> true)), ObjWithSub(1.0, ObjSimple("ddd", 23, true)))

      }

    }

  }

  describe("Object with lists and sets") {

    describe("with specialized writing") {

      implicit val canReadAaa: CanRead[ObjWithSets, MapReader[MongoReader]] = canReadAsProduct(ObjWithSets)("a", "b")

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)), ObjWithSets(Set("ddd"), List(23, 11)))

    }

    describe("with universal writing") {

      implicit def canReadAaa[W](implicit cws: CanRead[Set[String], W], cwi: CanRead[List[Int], W]): CanRead[ObjWithSets, MapReader[W]] = canReadAsProduct(ObjWithSets)("a", "b")

      shoudBeReadAndConvertedFromDBObject(dbObject("a" -> dbList("ddd"), "b" -> dbList(23, 11)), ObjWithSets(Set("ddd"), List(23, 11)))

    }

  }

}
