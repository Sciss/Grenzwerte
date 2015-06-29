def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val sampleRate  = SampleRate.ir
  val freq_0      = sampleRate / 2.0
  val freq_1      = GbmanN.ar(freq = freq_0, xi = 0.03487708, yi = 143.81136)
  val bBandStop   = BBandStop.ar(219.65942, freq = freq_1, bw = 7.4398813)
  val pulseCount  = PulseCount.ar(trig = 7948.9946, reset = bBandStop)
  val xi_0        = BRF.ar(419.73846, freq = 333.12207, rq = 660.6183)
  val a_0         = StandardN.ar(freq = 18.231197, k = 623.5792, xi = 83.65495, yi = -2.3472204)
  val linCongL    = LinCongL.ar(freq = 219.65942, a = a_0, c = bBandStop, m = 1.685643E-4, xi = xi_0)
  val blip        = Blip.ar(freq = 333.12207, numHarm = 419.73846)
  val level       = Saw.ar(440.0)
  val saw         = Saw.ar(105.56122)
  val xFade2      = XFade2.ar(inA = saw, inB = 18.71311, pan = 660.6183, level = level)
  val standardN_0 = StandardN.ar(freq = 18.231197, k = 623.5792, xi = 83.65495, yi = -2.3472204)
  val a_1         = RunningMin.ar(219.65942, trig = 219.65942)
  val gendy2      = Gendy2.ar(ampDist = 83.65495, durDist = saw, adParam = -962.5887, ddParam = 18.71311, minFreq = 83.65495, maxFreq = 83.65495, ampScale = 7948.9946, durScale = standardN_0, initCPs = 18.231197, kNum = 83.65495, a = a_1, c = -2.3472204)
  val lFDNoise1   = LFDNoise1.ar(383.39972)
  val hPF         = HPF.ar(-2.3472204, freq = standardN_0)
  val standardN_1 = StandardN.ar(freq = 7948.9946, k = -2.3472204, xi = 18.231197, yi = standardN_0)
  val mix         = Mix(Seq[GE](standardN_1, hPF, lFDNoise1, gendy2, xFade2, blip, linCongL, pulseCount))
  val mono        = Mix.Mono(mix)
  ConfigOut(mono)
}
