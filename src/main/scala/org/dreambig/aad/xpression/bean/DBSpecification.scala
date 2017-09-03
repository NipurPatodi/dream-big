package org.dreambig.aad.xpression.bean

/**
  * Created by npatodi030716 on 3/7/2017.
  */
case class DBSpecification(url:String,
                           user:String,
                           password:String
                           ){
  val driver:String="org.postgresql.Driver"
}


