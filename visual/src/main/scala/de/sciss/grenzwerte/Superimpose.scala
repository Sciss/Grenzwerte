package de.sciss.grenzwerte

import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import composite.BlendComposite
import de.sciss.file._

object Superimpose {
  case class Config(dir: File = file("image_out"),
                    output  : File = file("superimpose.png"),
                    extent: Int = 512,
                    maxSide: Int = 1403,
                    scale: Double = 16.0)

  def main(args: Array[String]): Unit = {
    run(Config())
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

    val xs0 = dir.children(_.ext.toLowerCase == "png")
    val xs  = xs0.sortBy { f => val n = f.name; val i = n.indexOf('_'); n.substring(0, i) }

    println("_" * 100)
    var lastProg = 0

    xs.zipWithIndex.foreach { case (f, i) =>
      // frame2900_10303347189_248_248.png
      val words   = f.name.split("[_.]")
      // val time  = words(1).toLong
      val xQuad   = words(2).toInt
      val yQuad   = words(3).toInt

      val img     = ImageIO.read(f)
      val imgW    = img.getWidth
      val imgH    = img.getHeight
      val imgTemp = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB)
      val g2 = imgTemp.createGraphics()
      g2.drawImage(img, 0, 0, null)
      g2.dispose()
      val offX  = (xQuad * scale + maxSide - img.getWidth  * 0.5 + 0.5).toInt
      val offY  = (yQuad * scale + maxSide - img.getHeight * 0.5 + 0.5).toInt
      val atOrig = g.getTransform
      g.translate(offX, offY)
      g.drawImage(imgTemp, 0, 0, null) //  AffineTransform.getTranslateInstance(offX, offY), null)
      g.setTransform(atOrig)
      imgTemp.flush()
      img.flush()

      val prog = (i + 1) * 100 / xs.size
      while(lastProg < prog) {
        print('#')
        lastProg += 1
      }
    }

    ImageIO.write(bImg, "png", output)
    bImg.flush()
  }
}