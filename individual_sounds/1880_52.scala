def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val decodeB2      = DecodeB2.ar(numChannels = 1, w = 650.94977, x = 0.007095831, y = 0.007095831, orient = 636.937)
  val linCongL_0    = LinCongL.ar(freq = -4339.4595, a = 1.2398051, c = decodeB2, m = 2.8008559, xi = 11.076442)
  val freq_0        = GbmanN.ar(freq = 0.34288365, xi = linCongL_0, yi = -0.0058660484)
  val freq_1        = GrayNoise.ar(5663.021)
  val saw           = Saw.ar(freq_1)
  val leakDC        = LeakDC.ar(3.0124836, coeff = 8.6487544E-4)
  val lFDNoise3     = LFDNoise3.ar(leakDC)
  val linCongL_1    = LinCongL.ar(freq = 650.94977, a = leakDC, c = -622.14514, m = 2.8008559, xi = -0.4259863)
  val freq_2        = LeakDC.ar(linCongL_1, coeff = 216.59688)
  val lFTri         = LFTri.ar(freq = freq_0, iphase = -622.14514)
  val gbmanN        = GbmanN.ar(freq = freq_2, xi = linCongL_1, yi = -622.14514)
  val formant       = Formant.ar(fundFreq = gbmanN, formFreq = 0.29428017, bw = 0.29428017)
  val pos           = Lag2UD.ar(14.625873, timeUp = 383.39972, timeDown = gbmanN)
  val panAz         = PanAz.ar(numChannels = 1, in = 0.007095831, pos = pos, level = 0.022559516, width = 0.34288365, orient = linCongL_0)
  val detectSilence = DetectSilence.ar(0.042024028, amp = 8.6487544E-4, dur = decodeB2, doneAction = doNothing)
  val toggleFF      = ToggleFF.ar(18.71311)
  val mix           = Mix(Seq[GE](toggleFF, detectSilence, panAz, formant, lFTri, lFDNoise3, saw))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
