def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val bRF_0         = BRF.ar(695.37335, freq = -0.0029116, rq = 419.73846)
  val gbmanL        = GbmanL.ar(freq = 419.73846, xi = -466.74478, yi = -2726.2134)
  val fBSineN       = FBSineN.ar(freq = 0.36766747, im = 0.00788784, fb = -0.0029116, a = -2029.8915, c = 637.2363, xi = 23.868387, yi = 637.2363)
  val xpos          = LeastChange.ar(a = 419.73846, b = 12.325766)
  val trig_0        = Pan4.ar(3.0152974, xpos = xpos, ypos = fBSineN, level = 0.0015142808)
  val dur           = SetResetFF.ar(trig = trig_0, reset = 12.325766)
  val formFreq      = Trig1.ar(419.73846, dur = dur)
  val formant       = Formant.ar(fundFreq = 419.73846, formFreq = formFreq, bw = 0.0015142808)
  val bBandStop_0   = BBandStop.ar(0.00788784, freq = 107.30127, bw = 637.2363)
  val bBandStop_1   = BBandStop.ar(0.00788784, freq = 0.262003, bw = 637.2363)
  val bRF_1         = BRF.ar(bBandStop_0, freq = bBandStop_1, rq = 0.00788784)
  val sampleRate    = SampleRate.ir
  val freq_0        = sampleRate / 2.0
  val standardN     = StandardN.ar(freq = freq_0, k = 9.444879E-4, xi = bBandStop_1, yi = 3.0152974)
  val latoocarfianN = LatoocarfianN.ar(freq = standardN, a = 2.13177E-4, b = 695.37335, c = fBSineN, d = 9.444879E-4, xi = formant, yi = 107.30127)
  val freq_1        = Ramp.ar(2083.8538, dur = standardN)
  val latoocarfianL = LatoocarfianL.ar(freq = freq_1, a = formant, b = 12.325766, c = 4245.225, d = 107.30127, xi = bBandStop_0, yi = -466.74478)
  val mix           = Mix(Seq[GE](latoocarfianL, latoocarfianN, bRF_1, gbmanL, bRF_0))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}