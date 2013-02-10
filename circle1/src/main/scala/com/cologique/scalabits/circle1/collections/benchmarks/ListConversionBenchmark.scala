package com.cologique.scalabits.circle1.collections.benchmarks

import com.google.caliper.Runner
import com.google.caliper.SimpleBenchmark
import com.google.caliper.Param

/**
 * [info]   1000      JavaArrayListConversion   28.4 =
 * [info]   1000       ListConversionUsingMap   22.1 =
 * [info]   1000     VectorConversionUsingMap   12.7 =
 * [info]   1000 ListBufferConversionUsingMap   22.5 =
 * [info]  10000      JavaArrayListConversion  277.2 =
 * [info]  10000       ListConversionUsingMap  244.5 =
 * [info]  10000     VectorConversionUsingMap  133.6 =
 * [info]  10000 ListBufferConversionUsingMap  245.8 =
 * [info] 100000      JavaArrayListConversion 4130.9 =================
 * [info] 100000       ListConversionUsingMap 7092.5 =============================
 * [info] 100000     VectorConversionUsingMap 2215.0 =========
 * [info] 100000 ListBufferConversionUsingMap 7121.2 ==============================
 */
object ListConversionBenchmark extends App {
  Runner.main(classOf[ListConversionBenchmark], args);
}

class ListConversionBenchmark extends SimpleBenchmark {
  @Param(Array("1000", "10000", "100000")) var size: Int = 10000; // set automatically by framework
  var range: Range = null
  var immutableList: List[Int] = null
  var immutableVector: Vector[Int] = null
  var javaList: java.util.ArrayList[Int] = null
  var mutableList: scala.collection.mutable.ListBuffer[Int] = null

  override protected def setUp() {
    range = 0 until size
    immutableList = List(range: _*)
    immutableVector = Vector(range: _*)
    javaList = {
      val list = new java.util.ArrayList[Int]()
      range.foreach { i =>
        list.add(i)
      }
      list
    }
    mutableList = {
      val list = scala.collection.mutable.ListBuffer[Int]()
      range.foreach(list += _)
      list
    }
  }

  def timeJavaArrayListConversion(reps: Int) {
    for (i <- 1 to reps) {
      val list = new java.util.ArrayList[Int]()
      for (i <- 0 until javaList.size()) {
        list.add(javaList.get(i) * 2);
      }
    }
  }

  def timeListConversionUsingMap(reps: Int) {
    for (i <- 1 to reps) {
      val list = immutableList.map(_ * 2)
    }
  }

  def timeVectorConversionUsingMap(reps: Int) {
    for (i <- 1 to reps) {
      val list = immutableVector.map(_ * 2)
    }
  }

  def timeListBufferConversionUsingMap(reps: Int) {
    for (i <- 1 to reps) {
      val list = mutableList.map(_ * 2)
    }
  }
}
