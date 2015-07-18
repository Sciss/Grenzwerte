package de.sciss.grenzwerte.visual

import java.awt.geom.Rectangle2D

import prefuse.action.layout.graph.ForceDirectedLayout
import prefuse.util.force.ForceItem
import prefuse.visual.{NodeItem, VisualItem}

class MyForceDirectedLayout(group: String, enforceBounds: Boolean = false)
  extends ForceDirectedLayout(group, enforceBounds) {

  def runOnce(): Unit = {
    val anchor = getLayoutAnchor
    val iter = m_vis.visibleItems(m_nodeGroup)
    while (iter.hasNext) {
      val item = iter.next().asInstanceOf[NodeItem]
      item.setX(anchor.getX)
      item.setY(anchor.getY)
    }
    val fsim = getForceSimulator
    fsim.clear()
    // var timestep = 1000L
    initSimulator(fsim)
    val iterations = getIterations
    var i = 0
    while (i < iterations) {
      // timestep *= (1.0 - i / iterations.toDouble)
      val step = 50 // timestep + 50
      fsim.runSimulator(step)
      i += 1
    }
    updateNodePositions()
  }

  private def updateNodePositions(): Unit = {
    val bounds: Rectangle2D = getLayoutBounds
    var x1: Double = 0
    var x2: Double = 0
    var y1: Double = 0
    var y2: Double = 0
    if (bounds != null) {
      x1 = bounds.getMinX
      y1 = bounds.getMinY
      x2 = bounds.getMaxX
      y2 = bounds.getMaxY
    }
    val iter: java.util.Iterator[_] = m_vis.visibleItems(m_nodeGroup)
    while (iter.hasNext) {
      val item: VisualItem = iter.next.asInstanceOf[VisualItem]
      val fitem: ForceItem = item.get(ForceDirectedLayout.FORCEITEM).asInstanceOf[ForceItem]
      if (item.isFixed) {
        fitem.force(0) = 0.0f
        fitem.force(1) = 0.0f
        fitem.velocity(0) = 0.0f
        fitem.velocity(1) = 0.0f
        if (java.lang.Double.isNaN(item.getX)) {
          setX(item, referrer, 0.0)
          setY(item, referrer, 0.0)
        }

      } else {
        var x: Double = fitem.location(0)
        var y: Double = fitem.location(1)

        if (enforceBounds && bounds != null) {
          val b: Rectangle2D = item.getBounds
          val hw: Double = b.getWidth / 2
          val hh: Double = b.getHeight / 2
          if (x + hw > x2) x = x2 - hw
          if (x - hw < x1) x = x1 + hw
          if (y + hh > y2) y = y2 - hh
          if (y - hh < y1) y = y1 + hh
        }
        setX(item, referrer, x)
        setY(item, referrer, y)
      }
    }
  }
}