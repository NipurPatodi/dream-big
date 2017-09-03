package org.dreambig.aad.xpression.sentiment

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations


/**
  * Created by npatodi on 8/30/17.
  */
object SentimentAnalysor {

  val sentimentMap =Map(0->"upset",1->"sad",2->"neutral",3->"happy",4->"glad")

  def main(args: Array[String]): Unit ={
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
    val pipeline = new StanfordCoreNLP(props)
    var ok  = true
    while (ok) {
      val line = readLine()
      ok = line != null

      if (ok){

        val annotation = pipeline.process(line)
        val ans =annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
        var maxSentence  = 0
        var sentiment  = -1
        for (i <- 0 to ans.size()-1){
          val sentence = ans.get(i)
          val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
          val sc = RNNCoreAnnotations.getPredictedClass(tree)
          if(sentence.toString.length>maxSentence){

            maxSentence=sentence.toString.length
            sentiment = sc
          }
          println(s"===> ${sentence.toString.length}")
          println(sentimentMap(sc))
      }
      println(s"Over all sentiment => ${sentimentMap(sentiment)}")

      }
    }

  }
}
