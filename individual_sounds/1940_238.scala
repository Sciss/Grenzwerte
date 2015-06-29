def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val a         = BRF.ar(-0.0029116, freq = 636.937, rq = 419.73846)
  val saw       = Saw.ar(419.73846)
  val trig_0    = LFSaw.ar(freq = 1015.2887, iphase = 0.34133184)
  val tDelay    = TDelay.ar(trig = 670.28094, dur = 0.011279295)
  val xi        = PulseDivider.ar(trig = trig_0, div = 7883.0576, start = 670.28094)
  val lFPulse   = LFPulse.ar(freq = 670.28094, iphase = 0.0, width = 170.5119)
  val timer     = Timer.ar(170.5119)
  val gbmanN    = GbmanN.ar(freq = 121.303734, xi = 670.28094, yi = 0.27751106)
  val lag3UD    = Lag3UD.ar(636.937, timeUp = 629.9954, timeDown = 83.65495)
  val cuspN     = CuspN.ar(freq = lag3UD, a = gbmanN, b = -1.277892, xi = -0.0054616947)
  val pan       = LinCongC.ar(freq = 2.3241599, a = 83.65495, c = 1015.2887, m = 3.6387606, xi = gbmanN)
  val coeff     = GbmanN.ar(freq = 121.303734, xi = 636.937, yi = 0.011279295)
  val oneZero   = OneZero.ar(41.55496, coeff = coeff)
  val in_0      = LinXFade2.ar(inA = 41.55496, inB = 629.9954, pan = pan, level = 1015.2887)
  val bitAnd    = in_0 & 0.34497613
  val in_1      = gbmanN.log
  val freq_0    = Delay2.ar(in_1)
  val fBSineC   = FBSineC.ar(freq = freq_0, im = -2501.7903, fb = 0.1, a = a, c = 0.006726554, xi = xi, yi = 3.6387606)
  val plus      = freq_0 + 2.699921
  val linCongL  = LinCongL.ar(freq = 629.9954, a = -0.0029116, c = 41.55496, m = 41.55496, xi = 0.0)
  val linPan2   = LinPan2.ar(0.27751106, pos = 8.1660674E-4, level = 3.6387606)
  val hi        = Ball.ar(in_0, g = 41.55496, damp = lag3UD, friction = 629.9954)
  val tExpRand  = TExpRand.ar(lo = 4900.0625, hi = hi, trig = 0.34133184)
  val bitOr     = tExpRand | 3.6387606
  val lFDNoise0 = LFDNoise0.ar(0.0034435873)
  val mix       = Mix(Seq[GE](lFDNoise0, bitOr, linPan2, linCongL, plus, fBSineC, bitAnd, oneZero, cuspN, timer, lFPulse, tDelay, saw))
  val mono      = Mix.Mono(mix)
  ConfigOut(mono)
}
