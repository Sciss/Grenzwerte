def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val yi_0          = GrayNoise.ar(637.2363)
  val freeVerb      = FreeVerb.ar(637.2363, mix = 0.262003, room = 0.00788784, damp = 309.95212)
  val formant       = Formant.ar(fundFreq = 419.73846, formFreq = 419.73846, bw = 0.0015142808)
  val fb            = FBSineN.ar(freq = 12.325766, im = 0.262003, fb = -0.0029116, a = -2029.8915, c = 637.2363, xi = 23.868387, yi = 23.868387)
  val yi_1          = FBSineN.ar(freq = 666.4933, im = formant, fb = fb, a = -2029.8915, c = -10.767268, xi = 23.868387, yi = 637.2363)
  val xi_0          = BBandStop.ar(0.00788784, freq = 0.262003, bw = 637.2363)
  val dur           = StandardN.ar(freq = 114.05373, k = 9.444879E-4, xi = xi_0, yi = yi_1)
  val freq_0        = Ramp.ar(254.42952, dur = dur)
  val neq           = freeVerb sig_!= freq_0
  val freq_1        = GbmanL.ar(freq = 419.73846, xi = freeVerb, yi = yi_0)
  val pan4          = Pan4.ar(419.73846, xpos = 0.0015142808, ypos = 0.36766747, level = 1.0)
  val hPZ1          = HPZ1.ar(419.73846)
  val latoocarfianL = LatoocarfianL.ar(freq = freq_0, a = formant, b = 12.325766, c = 0.5, d = 114.05373, xi = freeVerb, yi = -466.74478)
  val setResetFF    = SetResetFF.ar(trig = 12.325766, reset = 12.210541)
  val bRF           = BRF.ar(freeVerb, freq = 419.73846, rq = 0.00788784)
  val blip          = Blip.ar(freq = freq_1, numHarm = -466.74478)
  val mix_0         = Mix(Seq[GE](blip, bRF, setResetFF, latoocarfianL, hPZ1, pan4, neq))
  val mono          = Mix.Mono(mix_0)
  ConfigOut(mono)
}