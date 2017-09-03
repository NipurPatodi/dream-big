
package org.dreambig.aad.xpression.extractor.utility

import java.util.Properties

import com.databricks.spark.corenlp.functions._
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.dreambig.aad.xpression.bean.{SentimentIdx, TagMessage}


/**
  * Created by npatodi030716 on 3/2/2017.
  */
object SentimentAnalyser {

  private val SentimentMap = Map(0 -> "upset", 1 -> "sad", 2 -> "neutral", 3 -> "happy", 4 -> "glad")
  private var pipeline: StanfordCoreNLP = null;

  private def getPipeLine: StanfordCoreNLP = {
    if (null == pipeline) {
      val props = new Properties()
      props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
      pipeline = new StanfordCoreNLP(props)
    }
    pipeline
  }

  private def getSentiment( msg: String): Int = {
    val pipe= getPipeLine
    val annotation = pipe.process(msg)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    var maxSentSize = 0
    var sentiment = -1
    for (i <- 0 to sentences.size() - 1) {
      val sentence = sentences.get(i)
      val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      val sc = RNNCoreAnnotations.getPredictedClass(tree)
      if (sentence.toString.length > maxSentSize) {
        maxSentSize = sentence.toString.length
        sentiment = sc
      }
    }
    sentiment
  }


  def analyze(rdd: RDD[TagMessage], onColumn: String): RDD[(String, SentimentIdx)] = {
    val sqlContext = SQLContext.getOrCreate(rdd.sparkContext)
    val df = sqlContext.createDataFrame(rdd)
    df.select(col("tagId"), col(onColumn), sentiment(col(onColumn)).as('sentiment)).
      rdd.map(x => (x.getString(0), SentimentIdx(x.getString(1), x.getInt(2))))
  }


  def analyze(rdd: RDD[TagMessage]): RDD[(String, SentimentIdx)] = {

    rdd.map(x => (x.tagId, SentimentIdx(x.message, getSentiment( x.message))))

  }


}

