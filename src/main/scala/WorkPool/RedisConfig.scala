package WorkPool

/**
  * Created by earvinkayonga on 16/06/2016.
  */
case class  RedisConfig(
  port: Int,
  host: String,
  processingQueueName: String,
  jobQueueName: String,
  errorQueueName: String,
  numberOfWorkers: Int) {


}
