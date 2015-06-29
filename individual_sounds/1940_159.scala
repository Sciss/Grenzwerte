def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val bBandStop = BBandStop.ar(219.65942, freq = 383.39972, bw = 0.0042042704)
  val blip_0    = Blip.ar(freq = 333.12207, numHarm = -2526.418)
  val loop      = PeakFollower.ar(6382788.0, decay = 623.5792)
  val hi        = LFGauss.ar(dur = blip_0, width = 0.38179046, phase = 0.21466221, loop = loop, doneAction = doNothing)
  val fold      = Fold.ar(bBandStop, lo = 0.0042850445, hi = hi)
  val linCongL  = LinCongL.ar(freq = 1.685643E-4, a = -1.2799277, c = bBandStop, m = 219.65942, xi = -2526.418)
  val bRF       = BRF.ar(703.5526, freq = 629.3649, rq = 1.1710696)
  val plus      = bRF + linCongL
  val lFDNoise1 = LFDNoise1.ar(0.0032836022)
  val freq_0    = HPZ2.ar(18.71311)
  val phase_0   = FBSineL.ar(freq = freq_0, im = 623.5792, fb = 0.0042850445, a = 6382788.0, c = blip_0, xi = 18.71311, yi = 83.65495)
  val lFGauss   = LFGauss.ar(dur = 0.34497613, width = 1.685643E-4, phase = phase_0, loop = -4.190655, doneAction = doNothing)
  val saw       = Saw.ar(105.56122)
  val inB       = 0.0013110363 - blip_0
  val xFade2    = XFade2.ar(inA = saw, inB = inB, pan = 703.5526, level = saw)
  val fSinOsc   = FSinOsc.ar(freq = 703.5526, iphase = 0.018322097)
  val blip_1    = Blip.ar(freq = 83.65495, numHarm = 0.28420624)
  val leakDC    = LeakDC.ar(0.38179046, coeff = 7948.9644)
  val mix       = Mix(Seq[GE](leakDC, blip_1, fSinOsc, xFade2, lFGauss, lFDNoise1, plus, fold))
  val mono      = Mix.Mono(mix)
  ConfigOut(mono)
}
