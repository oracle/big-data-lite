/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object SimpleApp {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Simple Scala Image App")
    val sc = new SparkContext(conf)
    
    val files = sc.binaryFiles("barcode/images/test.jpg")

    val imageResults = files.map(processSparkImage(_))

    imageResults.collect.foreach(
         result => println("\nFile:"+result._1+" Width:"+result._2+" Height:"+result._3)
                          ); //this println gets printed to the main stdout

    sc.stop()

    println("*")
    println("*")
    println("*")
    
  }
  
  def processSparkImage (
                    file: (String, org.apache.spark.input.PortableDataStream)
                   ) : (String, Int, Int)  =
  {
    println("In processSparkImage for "+file._1) //this println gets printed to the executor stdout
    val img: BufferedImage = ImageIO.read(file._2.open)
    val height = img.getHeight()
    println("Height is "+height+" for "+file._1)
    file._2.close

    return (file._1, img.getWidth(), img.getHeight)
  }
}
