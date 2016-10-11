/**
  * Created by subhra on 7/10/16.
  */
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkContext
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.kafka.common.TopicPartition
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

object KafkaStreaming {

  def main(args: Array[String]): Unit = {

    val sc = SparkContext.getOrCreate
    val ssc = new StreamingContext(sc, Seconds(5))

    val preferredHosts = LocationStrategies.PreferConsistent
    val topics = List("basic1", "basic2", "basic3")
    import org.apache.kafka.common.serialization.StringDeserializer
    val kafkaParams = Map(
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "example",
      "auto.offset.reset" -> "earliest"
    )

    val offsets = Map(new TopicPartition("basic3", 0) -> 2L)

    val dstream = KafkaUtils.createDirectStream[String, String](
      ssc,
      preferredHosts,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams, offsets))

    val tf = dstream.transform { rdd => rdd.map(r => (r.key, r.value))
    }

    tf.foreachRDD { rdd =>
      // Get the offset ranges in the RDD
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      for (o <- offsetRanges) {
        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
      }
    }

    def createRDD[K, V](
                         sc: SparkContext,
                         kafkaParams: java.util.Map[String, Object],
                         offsetRanges: Array[OffsetRange],
                         locationStrategy: LocationStrategy): RDD[ConsumerRecord[K, V]]

    ssc.start

    // ...

    ssc.stop(stopSparkContext = false)


  }



}


