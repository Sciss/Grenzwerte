def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val gbmanL        = GbmanL.ar(freq = 419.73846, xi = 631.3588, yi = -2526.418)
  val peakFollower  = PeakFollower.ar(636.937, decay = 8.689841E-4)
  val bRF           = BRF.ar(636.937, freq = 0.018255187, rq = 419.73846)
  val blip          = Blip.ar(freq = 636.937, numHarm = 0.007095831)
  val bPZ2          = BPZ2.ar(312.89795)
  val m_0           = GbmanN.ar(freq = 65.58365, xi = -0.84394026, yi = 0.0011724766)
  val m_1           = Gate.ar(-2526.418, gate = 0.006726554)
  val sampleRate    = SampleRate.ir
  val freq_0        = sampleRate / 2.0
  val linCongL      = LinCongL.ar(freq = freq_0, a = 0.006726554, c = -0.7992942, m = m_1, xi = 0.007095831)
  val mod           = linCongL % 77.419426
  val gate_0        = Gate.ar(0.0057315924, gate = 16.915588)
  val gbmanN        = GbmanN.ar(freq = 0.36766747, xi = 65.58365, yi = 0.0057315924)
  val combC         = CombC.ar(372.90106, maxDelayTime = 0.24059312, delayTime = 8.689841E-4, decayTime = 8.689841E-4)
  val linCongN      = LinCongN.ar(freq = 8.689841E-4, a = 16.915588, c = 372.90106, m = m_0, xi = 622.0609)
  val freq_1        = LFTri.ar(freq = 8.689841E-4, iphase = 0.0)
  val freq_2        = RHPF.ar(636.937, freq = freq_1, rq = 8.689841E-4)
  val lFClipNoise   = LFClipNoise.ar(freq_2)
  val plus          = (-7492.812: GE) + freq_2
  val mix           = Mix(Seq[GE](plus, lFClipNoise, 0.0, linCongN, combC, gbmanN, gate_0, mod, bPZ2, blip, bRF, peakFollower, gbmanL))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}