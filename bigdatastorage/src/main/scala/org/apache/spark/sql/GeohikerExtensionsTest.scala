package org.apache.spark.sql

import io.dtonic.geohiker.spark.GeohikerSparkExtensions

object GeohikerExtensionsTest {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("GeohikerExtensionsTest")
      .config("spark.sql.extensions", classOf[GeohikerSparkExtensions].getName)
      .getOrCreate()

    spark.sql("SELECT ST_POINT(10.0, 20.0) AS PT").show()
  }
}
