def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val xi            = GbmanL.ar(freq = 686.5739, xi = -2526.418, yi = 419.73846)
  val bRF_0         = BRF.ar(636.937, freq = -0.0029116, rq = 419.73846)
  val in_0          = Blip.ar(freq = 4.2980185E-4, numHarm = 0.007095831)
  val peakFollower  = PeakFollower.ar(in_0, decay = 0.34497613)
  val gate_0        = GbmanN.ar(freq = 0.34497613, xi = -0.0058356896, yi = 636.937)
  val gate_1        = Gate.ar(-676.2965, gate = gate_0)
  val c             = GbmanL.ar(freq = 83.65495, xi = 936.9255, yi = 4.2980185E-4)
  val plus          = bRF_0 + c
  val bRF_1         = BRF.ar(636.937, freq = 419.73846, rq = -0.0029116)
  val linCongL      = LinCongL.ar(freq = 636.937, a = -676.2965, c = c, m = bRF_1, xi = xi)
  val g             = LFTri.ar(freq = 1.0334905E-4, iphase = 8.225018E-4)
  val yi_0          = TBall.ar(83.65495, g = g, damp = 0.006726554, friction = -0.0054616947)
  val gbmanL        = GbmanL.ar(freq = -0.0054616947, xi = bRF_1, yi = yi_0)
  val gbmanN        = GbmanN.ar(freq = -4334.8867, xi = -0.0058356896, yi = 8.225018E-4)
  val grayNoise     = GrayNoise.ar(18.71311)
  val lag3          = Lag3.ar(419.73846, time = 636.937)
  val saw           = Saw.ar(0.022197612)
  val mix           = Mix(Seq[GE](saw, lag3, grayNoise, gbmanN, gbmanL, linCongL, plus, gate_1, peakFollower))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
