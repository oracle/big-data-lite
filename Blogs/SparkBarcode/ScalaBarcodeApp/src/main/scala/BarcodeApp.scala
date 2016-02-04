/* BarcodeApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import barcodedemo.BarcodeProcessor

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object BarcodeApp {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Scala ZXing Barcode App")
    val sc = new SparkContext(conf)
    
    //val files = sc.binaryFiles("barcode/images")
    val files = sc.binaryFiles(args(0))

    val imageResults = files.map(processSparkImage(_))

    imageResults.collect.foreach(
               result => println("\nFile:" + result._1 + "\n Results:" + result._2)
                                ) 
                          //this println gets written to the main stdout


    sc.stop()

    println("*")
    println("*")
    println("*")
    
  }
  
  def processSparkImage (
                    file: (String, org.apache.spark.input.PortableDataStream)
                   ) : (String, String)  =
  {
    println("In processSparkImage for "+file._1) //this println gets written to the executor stdout

    val img: BufferedImage = ImageIO.read(file._2.open)
    file._2.close

    return (file._1, BarcodeProcessor.processImage(img))
  }

}
