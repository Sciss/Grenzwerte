def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val bRF_0         = BRF.ar(695.37335, freq = -0.0029116, rq = 419.73846)
  val gbmanL_0      = GbmanL.ar(freq = 419.73846, xi = 0.00788784, yi = -2726.2134)
  val unaryOpUGen   = gbmanL_0.log
  val fBSineN       = FBSineN.ar(freq = 12.325766, im = 637.2363, fb = -0.0029116, a = -2029.8915, c = 582.82227, xi = 23.868387, yi = 0.262003)
  val xpos          = LeastChange.ar(a = 419.73846, b = 12.325766)
  val xi_0          = Pan4.ar(fBSineN, xpos = xpos, ypos = 254.25714, level = 582.82227)
  val gbmanL_1      = GbmanL.ar(freq = 0.262003, xi = xi_0, yi = 22.71261)
  val in_0          = FBSineN.ar(freq = 419.73846, im = 0.262003, fb = -0.0029116, a = -2029.8915, c = 637.2363, xi = 23.868387, yi = 582.82227)
  val freq_0        = Ramp.ar(in_0, dur = 0.1)
  val im_0          = SetResetFF.ar(trig = 12.325766, reset = 12.325766)
  val formFreq      = FBSineL.ar(freq = 419.73846, im = im_0, fb = 419.73846, a = 1.1, c = 0.020259222, xi = fBSineN, yi = 2.3985734)
  val a_0           = Formant.ar(fundFreq = 419.73846, formFreq = formFreq, bw = 254.25714)
  val bBandStop_0   = BBandStop.ar(0.00788784, freq = 0.262003, bw = 637.2363)
  val standardN     = StandardN.ar(freq = 107.30127, k = 9.444879E-4, xi = bBandStop_0, yi = -962.5887)
  val bBandStop_1   = BBandStop.ar(637.2363, freq = 0.262003, bw = 0.00788784)
  val bRF_1         = BRF.ar(bBandStop_1, freq = bBandStop_0, rq = 0.00788784)
  val latoocarfianL = LatoocarfianL.ar(freq = freq_0, a = a_0, b = 12.325766, c = 2.3985734, d = 107.30127, xi = bBandStop_1, yi = -466.74478)
  val lFClipNoise   = LFClipNoise.ar(-466.74478)
  val mix           = Mix(Seq[GE](lFClipNoise, latoocarfianL, bRF_1, standardN, gbmanL_1, unaryOpUGen, bRF_0))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
