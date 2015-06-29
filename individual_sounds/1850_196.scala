def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val bRF           = BRF.ar(660.6183, freq = -0.0029116, rq = 419.73846)
  val gbmanN        = GbmanN.ar(freq = -0.0058356896, xi = 0.0042042704, yi = 27.10518)
  val freq_0        = GrayNoise.ar(950.97156)
  val xi_0          = BBandStop.ar(0.074246645, freq = freq_0, bw = gbmanN)
  val freq_1        = Blip.ar(freq = 0.14769231, numHarm = 174.76862)
  val g_0           = GbmanN.ar(freq = -0.0058356896, xi = 27.10518, yi = 0.0042042704)
  val g_1           = Ball.ar(27.10518, g = g_0, damp = 0.16703467, friction = -0.0054616947)
  val damp_0        = Gate.ar(-0.0058356896, gate = 170.67592)
  val tBall         = TBall.ar(0.0065136594, g = g_1, damp = damp_0, friction = 0.107436255)
  val freq_2        = FBSineL.ar(freq = freq_1, im = bRF, fb = gbmanN, a = -0.0029116, c = tBall, xi = tBall, yi = 0.5958211)
  val freq_3        = LFTri.ar(freq = freq_2, iphase = 660.6183)
  val linCongL      = LinCongL.ar(freq = freq_3, a = 0.16703467, c = -0.0029116, m = bRF, xi = xi_0)
  val plus          = bRF + xi_0
  val peakFollower  = PeakFollower.ar(0.018294215, decay = 0.36401057)
  val minus         = -0.0058356896 - freq_2
  val gbmanL        = GbmanL.ar(freq = 660.6183, xi = -2526.418, yi = 0.018294215)
  val lFDNoise1     = LFDNoise1.ar(313.72003)
  val latch         = Latch.ar(9.095092E-4, trig = 0.16703467)
  val bPF           = BPF.ar(660.6183, freq = 27.10518, rq = 0.022197612)
  val schmidt       = Schmidt.ar(0.48492587, lo = 660.6183, hi = 0.11113872)
  val mix           = Mix(Seq[GE](schmidt, bPF, latch, lFDNoise1, gbmanL, minus, peakFollower, plus, linCongL))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
