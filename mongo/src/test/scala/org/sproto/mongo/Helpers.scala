package org.sproto.mongo

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

trait Helpers {

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

}