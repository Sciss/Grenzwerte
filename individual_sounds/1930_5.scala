def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val orient        = BRF.ar(636.937, freq = -0.0029116, rq = 419.73846)
  val peakFollower  = PeakFollower.ar(0.05488034, decay = 670.28094)
  val freq_0        = RunningMin.ar(121.303734, trig = 22.63516)
  val xi            = Timer.ar(1)
  val linCongL      = LinCongL.ar(freq = freq_0, a = 1.1298052, c = 0.05488034, m = 583.8691, xi = xi)
  val cuspN         = CuspN.ar(freq = -1.277892, a = 1.2398051, b = 0.004446634, xi = 1.0)
  val rq_0          = Dust2.ar(0.12379083)
  val saw           = Saw.ar(freq_0)
  val bRF           = BRF.ar(8.281224E-5, freq = -0.0029116, rq = 636.937)
  val trig_0        = BRF.ar(bRF, freq = 0.015301899, rq = rq_0)
  val timer         = Timer.ar(trig_0)
  val trig_1        = GbmanN.ar(freq = 170.5119, xi = 636.937, yi = bRF)
  val x             = Timer.ar(trig_1)
  val decodeB2      = DecodeB2.ar(numChannels = 1, w = 22.63516, x = x, y = 1.2398051, orient = orient)
  val freq_1        = LFTri.ar(freq = -1.277892, iphase = 76.87258)
  val lFSaw         = LFSaw.ar(freq = freq_1, iphase = 1015.2887)
  val impulse       = Impulse.ar(freq = -0.0054616947, phase = 8.689841E-4)
  val mix           = Mix(Seq[GE](impulse, lFSaw, decodeB2, timer, saw, cuspN, linCongL, peakFollower))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
