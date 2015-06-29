play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val lagUD_0   = LagUD.ar(0.015382527, timeUp = 0.015382527, timeDown = 8.115047)
  val a         = ZeroCrossing.ar(lagUD_0)
  val linCongN  = LinCongN.ar(freq = 132.60497, a = a, c = Seq(0.6495046, 0.65), 
    m = lagUD_0, xi = Seq(656.4681, 656.49))
  val freeVerb2 = FreeVerb2.ar(inL = linCongN, inR = 2251.2712, mix = 6.4653874E-4, 
    room = 7.807298E-4, damp = 0.015382527)
  val dust2     = Dust2.ar(Seq.fill(2)(0.015382527))
  val median    = Median.ar(107.728004, length = 157.07405)
  val standardN = StandardN.ar(freq = 157.07405, k = Seq(1354.8416, 1354.85), 
    xi = Seq(-3.1591454, -3.16), yi = Seq(0.015382527, 0.016))
  val lFDNoise3 = LFDNoise3.ar(Seq.fill(2)(656.4681))
  val lagUD_1   = LagUD.ar(linCongN, timeUp = 19.208828, timeDown = 101.86243)
  val mix_0     = Mix(Seq[GE](lagUD_1, lFDNoise3, standardN, median, dust2, freeVerb2))
  val sig = Limiter.ar(LeakDC.ar(mix_0))
  sig
}