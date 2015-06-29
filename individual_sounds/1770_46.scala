def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val gbmanL_0      = GbmanL.ar(freq = 419.73846, xi = -2526.418, yi = 670.28094)
  val latoocarfianL = LatoocarfianL.ar(freq = -0.0054616947, a = 0.36766747, b = 8.225018E-4, c = 333.12207, d = 90.96002, xi = 670.28094, yi = 670.28094)
  val lFTri         = LFTri.ar(freq = latoocarfianL, iphase = 8.225018E-4)
  val gbmanL_1      = GbmanL.ar(freq = 4713.5117, xi = lFTri, yi = 83.65495)
  val peakFollower  = PeakFollower.ar(697.0505, decay = lFTri)
  val bRF           = BRF.ar(697.0505, freq = -0.0029116, rq = 419.73846)
  val c_0           = BBandStop.ar(670.28094, freq = 936.9255, bw = latoocarfianL)
  val m             = c_0 + latoocarfianL
  val linCongL_0    = LinCongL.ar(freq = 636.937, a = 1.1, c = c_0, m = m, xi = 636.937)
  val gate          = GbmanN.ar(freq = 8.225018E-4, xi = -0.0054616947, yi = 0.36766747)
  val freq_0        = Gate.ar(18.71311, gate = gate)
  val c_1           = Impulse.ar(freq = 0.0024416046, phase = 90.96002)
  val xi_0          = Blip.ar(freq = -0.0054616947, numHarm = 0.007095831)
  val linCongL_1    = LinCongL.ar(freq = freq_0, a = 8.225018E-4, c = c_1, m = -0.0054616947, xi = xi_0)
  val saw           = Saw.ar(-0.0054616947)
  val mix           = Mix(Seq[GE](saw, linCongL_1, linCongL_0, bRF, peakFollower, gbmanL_1, gbmanL_0))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}