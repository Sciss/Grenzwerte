def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val bRF           = BRF.ar(695.37335, freq = -0.0029116, rq = 419.73846)
  val gbmanL        = GbmanL.ar(freq = 419.73846, xi = 0.42893913, yi = 0.0023813124)
  val in_0          = Klank.ar(specs = 0.26494086, in = -469.48996, freqScale = 23.868387, freqOffset = 325.59705, decayScale = 23.868387)
  val in_1          = RHPF.ar(in_0, freq = 325.59705, rq = 3760.9478)
  val bw            = Trig1.ar(in_1, dur = 419.73846)
  val a             = Formant.ar(fundFreq = 0.0015142808, formFreq = 0.0016092974, bw = bw)
  val latoocarfianL = LatoocarfianL.ar(freq = -1.0239681, a = a, b = 0.3661115, c = 0.00648538, d = 0.2578632, xi = -469.48996, yi = 107.30127)
  val xi_0          = BBandStop.ar(0.42893913, freq = 637.2363, bw = -1.0239681)
  val rHPF_0        = RHPF.ar(in_0, freq = 325.59705, rq = 3760.9478)
  val unaryOpUGen   = in_1.cubed
  val density       = StandardN.ar(freq = unaryOpUGen, k = 0.26494086, xi = xi_0, yi = 3.0152974)
  val in_2          = Dust2.ar(density)
  val sweep         = Sweep.ar(trig = 412.14645, speed = -1.0239681)
  val formlet       = Formlet.ar(in_2, freq = 440.0, attack = 0.0019275966, decay = 412.14645)
  val freq_0        = Ramp.ar(419.73846, dur = 0.42893913)
  val ampCompA      = AmpCompA.ar(freq = freq_0, root = unaryOpUGen, minAmp = 0.004496857, rootAmp = 0.0015142808)
  val lastValue     = LastValue.ar(0.14840527, thresh = 23.868387)
  val leastChange   = LeastChange.ar(a = 0.26494086, b = 0.26494086)
  val rHPF_1        = RHPF.ar(-10.767268, freq = 0.0019275966, rq = 23.484463)
  val mix           = Mix(Seq[GE](rHPF_1, leastChange, lastValue, ampCompA, formlet, sweep, rHPF_0, latoocarfianL, gbmanL, bRF))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
