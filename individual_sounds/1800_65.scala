def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val step            = BRF.ar(636.937, freq = -0.0029695828, rq = 419.73846)
  val gbmanL          = GbmanL.ar(freq = 0.8313345, xi = -2526.418, yi = 419.73846)
  val freq_0          = PeakFollower.ar(636.937, decay = 0.34497613)
  val index           = Saw.ar(freq_0)
  val c               = Select.ar(index = index, in = 936.9255)
  val a               = Blip.ar(freq = 174.76862, numHarm = 0.007095831)
  val lo              = LinCongL.ar(freq = 636.937, a = a, c = c, m = 1.0, xi = 636.937)
  val iphase          = GbmanN.ar(freq = 0.34497613, xi = -0.0058356896, yi = 8.225018E-4)
  val lFTri           = LFTri.ar(freq = 8.689841E-4, iphase = iphase)
  val tBall           = TBall.ar(83.65495, g = lFTri, damp = 0.006726554, friction = -0.0054616947)
  val envGen_Triangle = EnvGen_Triangle(-0.0054616947, index, 18.71311, c, 0.0024416046, lo)
  val trig_0          = GrayNoise.ar(18.71311)
  val stepper         = Stepper.ar(trig = trig_0, reset = 3.7335578E-4, lo = lo, hi = 670.28094, step = step, resetVal = lFTri)
  val in_0            = GbmanN.ar(freq = -0.0058356896, xi = 1.2, yi = 636.937)
  val gate            = Gate.ar(in_0, gate = 636.937)
  val mix             = Mix(Seq[GE](gate, stepper, envGen_Triangle, tBall, gbmanL))
  val mono            = Mix.Mono(mix)
  ConfigOut(mono)
}