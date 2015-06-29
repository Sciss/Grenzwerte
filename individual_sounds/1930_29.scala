def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val in            = BRF.ar(636.937, freq = -0.0029116, rq = 419.73846)
  val freq_0        = Timer.ar(170.5119)
  val aPF           = APF.ar(in, freq = freq_0, radius = 0.0034435873)
  val dust2_0       = Dust2.ar(0.12379083)
  val crackle       = Crackle.ar(dust2_0)
  val bRF           = BRF.ar(in, freq = -0.020758213, rq = dust2_0)
  val dust2_1       = Dust2.ar(583.8691)
  val unaryOpUGen   = dust2_1.ampdb
  val peakFollower  = PeakFollower.ar(8.225018E-4, decay = 0.0034435873)
  val a_0           = Timer.ar(-848.3215)
  val m             = RunningMin.ar(23.7046, trig = 1.2398051)
  val trig_0        = LinCongL.ar(freq = 583.8691, a = a_0, c = 8.225018E-4, m = m, xi = 1.2398051)
  val xi_0          = Lag2UD.ar(-0.0054616947, timeUp = 83.65495, timeDown = 0.006726554)
  val cuspN         = CuspN.ar(freq = -1.277892, a = 1.2398051, b = -0.0054616947, xi = xi_0)
  val impulse       = Impulse.ar(freq = 8.689841E-4, phase = -0.35325)
  val freq_1        = LFTri.ar(freq = 636.937, iphase = -1.277892)
  val lFSaw         = LFSaw.ar(freq = freq_1, iphase = 1015.2887)
  val gbmanN        = GbmanN.ar(freq = 83.65495, xi = 636.937, yi = 2.1)
  val a_1           = Timer.ar(trig_0)
  val fBSineN       = FBSineN.ar(freq = -0.020758213, im = 41.55496, fb = -0.020758213, a = a_1, c = 8.689841E-4, xi = -0.0054616947, yi = -2526.418)
  val pinkNoise     = PinkNoise.ar(0.29428017)
  val saw           = Saw.ar(-0.020758213)
  val mix           = Mix(Seq[GE](saw, pinkNoise, fBSineN, gbmanN, lFSaw, impulse, cuspN, peakFollower, unaryOpUGen, bRF, crackle, aPF))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}