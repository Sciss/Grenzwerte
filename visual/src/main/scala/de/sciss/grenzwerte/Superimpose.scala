package de.sciss.grenzwerte

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import composite.BlendComposite
import de.sciss.file._
import de.sciss.intensitypalette.IntensityPalette
import de.sciss.numbers

object Superimpose {
  case class Config(dir     : File = file("image_out"),
                    output  : File = file("superimpose.png"),
                    extent  : Int = 512,
                    maxSide : Int = 1600,
                    scale   : Double = 16.0)

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("Superimpose") {
      opt[File]('d', "directory") text "input directory"   action { (x, c) => c.copy(dir    = x) }
      opt[File]('o', "output"   ) text "image output file" action { (x, c) => c.copy(output = x) }
      opt[Int] ('e', "extent"   ) text "quad-tree extent"  action { (x, c) => c.copy(extent = x) }
      opt[Int] ('m', "max-side" ) text "maximum input image side length" action { (x, c) => c.copy(maxSide = x) }
      opt[Double] ('s', "scale" ) text "image coordinate scaling" action { (x, c) => c.copy(scale = x) }
    }
    parser.parse(args, Config()).fold(sys.exit(1)) { config =>
      run(config)
    }
  }

  def run(config: Config): Unit = {
    import config._
    if (output.exists()) {
      println(s"File '${output.name}' already exists. Not overwriting.")
      sys.exit(1)
    }

    val outSide = math.ceil(extent * 2 * scale + maxSide).toInt

    val bImg = new BufferedImage(outSide, outSide, BufferedImage.TYPE_INT_ARGB)
    val g = bImg.createGraphics()
    g.setColor(Color.black)
    g.fillRect(0, 0, outSide, outSide)
    val comp = BlendComposite.Screen
    g.setComposite(comp)

    val xs = dir.children(_.ext.toLowerCase == "png")

    println("_" * 100)
    var lastProg = 0

    case class Entry(f: File, frame: Long, xQuad: Int, yQuad: Int)

    val entries = xs.map { f =>
      // frame2900_10303347189_248_248.png
      val words   = f.name.split("[_.]")
      val frame   = words(1).toLong
      val xQuad   = words(2).toInt
      val yQuad   = words(3).toInt
      Entry(f, frame = frame, xQuad = xQuad, yQuad = yQuad)
    } .sortBy(_.frame)

    val firstFrame  = entries.head.frame
    val lastFrame   = entries.last.frame

    import numbers.Implicits._

    var foundSide = maxSide

    entries.zipWithIndex.foreach { case (entry, i) =>
      import entry._
      val img     = ImageIO.read(f)
      val imgW    = img.getWidth
      val imgH    = img.getHeight
      val imgSide = math.max(imgW, imgH)
      if (imgSide > foundSide) foundSide = imgSide

      val imgTemp = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB)

      val g2      = imgTemp.createGraphics()
      g2.drawImage(img, 0, 0, null)
      g2.setComposite(BlendComposite.Multiply)
      val pos     = frame.toDouble.linlin(firstFrame, lastFrame, 0.0, 1.0).clip(0.0, 1.0)
      // println(pos)
      val rgb     = IntensityPalette.apply(pos.toFloat)
      val colr    = new Color(rgb)
      g2.setColor(colr)
      g2.fillRect(0, 0, imgW, imgH)

      val offX    = (xQuad * scale + (maxSide - imgW) * 0.5 + 0.5).toInt
      val offY    = (yQuad * scale + (maxSide - imgH) * 0.5 + 0.5).toInt
      val atOrig  = g.getTransform
      g.translate(offX, offY)
      g.drawImage(imgTemp, 0, 0, null) //  AffineTransform.getTranslateInstance(offX, offY), null)
      g.setTransform(atOrig)

      g2.dispose()
      imgTemp .flush()
      img     .flush()

      val prog = (i + 1) * 100 / xs.size
      while(lastProg < prog) {
        print('#')
        lastProg += 1
      }
    }

    g   .dispose()
    ImageIO.write(bImg, "png", output)
    bImg.flush()

    if (foundSide > maxSide) {
      Console.err.println(s"Warning: images exceeded given maximum side ($foundSide > $maxSide).")
    }
  }
}