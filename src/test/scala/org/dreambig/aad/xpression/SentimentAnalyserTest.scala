package org.dreambig.aad.xpression

import com.holdenkarau.spark.testing.SharedSparkContext
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.dreambig.aad.xpression.bean.TagMessage
import org.dreambig.aad.xpression.extractor.utility.SentimentAnalyser
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by npatodi030716 on 9/27/2016.
  */
class SentimentAnalyserTest  extends FunSuite with BeforeAndAfter with SharedSparkContext {

  var sqlContext:SQLContext =_



  test("Sentiment"){
    sqlContext = SQLContext.getOrCreate(sc)

    val rdd = sc.parallelize(Seq(TagMessage("1","It is a great university."),
      TagMessage("2","India is Good. Great Place to Live in."),TagMessage("3","Wasting time is very bad!.")))

    val result =SentimentAnalyser.analyze(rdd,"message")
    result.collect().foreach(x=> if (x._1==1)assert(x._2.rate==4) else if(x._1==2)assert(x._2.rate==3) else if(x._1==3)assert(x._2.rate==1))



  }

  test("Config"){
    println(ConfigFactory.load().getString("config.kafka.topic"))
  }


  test("Sentiment2"){
    sqlContext = SQLContext.getOrCreate(sc)

    val rdd = sc.parallelize(Seq(TagMessage("1","It is a great university."),
      TagMessage("1","India is Good. Great Place to Live in."),TagMessage("2","Wasting time is very bad!.")))
    val result =SentimentAnalyser.analyze(rdd)
    result.collect().foreach(x=> if (x._1==1)assert(x._2.rate==4) else if(x._1==2)assert(x._2.rate==3) else if(x._1==3)assert(x._2.rate==1))



  }

}
