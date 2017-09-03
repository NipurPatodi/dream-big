package org.dreambig.aad.xpression

import java.sql.DriverManager

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.dreambig.aad.xpression.bean.{SentimentAggUtil, TagMessage}
import org.dreambig.aad.xpression.extractor.utility.SentimentAnalyser
import org.dreambig.aad.xpression.extractor.utility.logging.InHouseLogging

/**
  * Created by npatodi on 9/3/17.
  */
object Main extends InHouseLogging{

  def main(args: Array[String]): Unit = {
    val config = new SparkConf().setAppName("Sentiment Analyser")
    val ssc = new StreamingContext(config,Seconds(2))

    val appConfig  = ConfigFactory.load()
    val topic = appConfig.getString("config.kafka.topic")
    val partition = appConfig.getString("config.kafka.partition").toInt
    val topicsPartitionMap =List((topic, partition)).toMap
    val group = appConfig.getString("config.kafka.group")
    val zkQuorum =appConfig.getString("config.kafka.zkQuorum")

    val url  =appConfig.getString("config.sql.url")
    val user =appConfig.getString("config.sql.user")
    val pswd =appConfig.getString("config.sql.pswrd")
    val insertInSentiments =appConfig.getString("config.sql.insertInSentiment")
    val insertInXpression =appConfig.getString("config.sql.insertInXpression")

    Class.forName(appConfig.getString("config.sql.driver"))

    //Converting inputstream to TagMessage
    val inputMsgStream =KafkaUtils.createStream(ssc,zkQuorum, group, topicsPartitionMap).
      map(kv => TagMessage(kv._1, kv._2))

    //Stream of sentiments
    val sentimentStream =inputMsgStream.transform(x=>SentimentAnalyser.analyze(x))

    //Aggregating on every minute level
    val aggregatedSentiments= sentimentStream.reduceByKey((x,y)=> SentimentAggUtil(x,y))

    sentimentStream.foreachRDD(rdd=> {
      rdd.foreachPartition( partitionRecords=>{
        val con = DriverManager.getConnection(url,user,pswd)
        val psXpression = con.prepareStatement(insertInXpression)

        partitionRecords.foreach(x=> {
          psXpression.setLong(1, x._1.toLong)
          psXpression.setString(2, x._2.xpression)
          psXpression.setInt(3, x._2.rate)
          psXpression.execute()
        })

        con.close()
      })
    })
  }

}
