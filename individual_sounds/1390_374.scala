def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

val x = play {
  val c1 = "c1".ar(Seq(242.49637, 242.49638))
  val freq_0    = GbmanL.ar(freq = c1, xi = Seq(317.49088, 317.49089), yi = Seq(695.37335, 695.37336))
  val bAllPass  = BAllPass.ar(316.04404, freq = freq_0, rq = 0.27029708)
  Limiter.ar(LeakDC.ar(bAllPass))
}

x.set("c1" -> 222)
x.set("c1" -> 242.5)
