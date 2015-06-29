def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val gbmanL        = GbmanL.ar(freq = 660.6183, xi = 0.018294215, yi = -2526.418)
  val m             = BRF.ar(660.6183, freq = -0.0029116, rq = 419.73846)
  val peakFollower  = PeakFollower.ar(0.018322097, decay = 0.36401057)
  val in_0          = Saw.ar(peakFollower)
  val c_0           = BBandStop.ar(in_0, freq = 0.007095831, bw = peakFollower)
  val plus          = c_0 + m
  val linCongL      = LinCongL.ar(freq = 656.25867, a = 1.0427988, c = c_0, m = m, xi = -0.0029116)
  val gate          = Gate.ar(-0.0058356896, gate = 1.0427988)
  val gbmanN        = GbmanN.ar(freq = -0.0058356896, xi = 0.34497613, yi = 0.0042042704)
  val g             = LFTri.ar(freq = -0.0054616947, iphase = gbmanN)
  val xi_0          = TBall.ar(83.65495, g = g, damp = 0.006726554, friction = -0.0054616947)
  val c_1           = Blip.ar(freq = 333.12207, numHarm = 174.76862)
  val decay_0       = FBSineL.ar(freq = 18.71311, im = 660.6183, fb = gbmanN, a = -0.0029116, c = c_1, xi = xi_0, yi = 0.5958211)
  val formlet       = Formlet.ar(0.006726554, freq = -389.67557, attack = 383.39972, decay = decay_0)
  val grayNoise     = GrayNoise.ar(0.34497613)
  val lFDNoise1     = LFDNoise1.ar(333.12207)
  val minus         = 1.0427988 - decay_0
  val toggleFF      = ToggleFF.ar(0.28420624)
  val quadN         = QuadN.ar(freq = 0.28420624, a = 170.67592, b = 0.018322097, c = -0.75, xi = 0.018294215)
  val mix           = Mix(Seq[GE](quadN, toggleFF, minus, lFDNoise1, grayNoise, formlet, gate, linCongL, plus, gbmanL))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
