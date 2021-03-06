val x = play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val slew        = Slew.ar(0.46569878, up = 80.98442, down = 9.444879E-4)
  val b           = LeastChange.ar(a = -93.37612, b = 0.010040017)
  val linen       = Linen.kr(gate = 0.26494086, attack = 0.10608994, sustain = -0.0029116, release = 0.46569878, doneAction = doNothing)
  val quadC       = QuadC.ar(freq = 0.42893913, a = 0.26494086, b = b, c = 9.444879E-4, xi = slew)
  val leastChange = LeastChange.ar(a = 80.98442, b = 419.73846)
  val gbmanL      = GbmanL.ar(freq = 419.73846, xi = leastChange, yi = 633.6489)
  val decay       = Decay.ar(leastChange, time = 633.6489)
  val slope       = Slope.ar(633.6489)
  val freq_0      = EnvGen.ar(Env.dadsr(419.73846, 0.3661115, 13.045165, 453.7693, 419.73846, decay), b, 80.98442, 6.157444, -2029.8915)
  val lFDNoise3   = LFDNoise3.ar(freq_0)
  val bRF         = BRF.ar(419.73846, freq = 0.010040017, rq = 633.6489)
  val syncSaw     = SyncSaw.ar(syncFreq = 0.10608994, sawFreq = -93.37612)
  val henonL      = HenonL.ar(freq = 266.40073, a = 0.46569878, b = 0.42893913, x0 = 419.73846, x1 = slew)
  val pan2        = Pan2.ar(667.69073, pos = henonL, level = 80.98442)
  val dust2_0     = Dust2.ar(80.98442)
  val iphase      = Slope.ar(266.40073)
  val varSaw      = VarSaw.ar(freq = 0.42893913, iphase = iphase, width = henonL)
  val dust2_1     = Dust2.ar(1.5559106)
  val pulseCount  = PulseCount.ar(trig = 633.6489, reset = 80.98442)
  val sqrdif      = pan2 sqrdif -1.0506817
  val mix         = Mix(Seq[GE](sqrdif, pulseCount, dust2_1, varSaw, dust2_0, syncSaw, bRF, lFDNoise3, slope, gbmanL, quadC, linen))
  val mono        = Mix.Mono(mix)
  val leak = LeakDC.ar(mono)
  val bad = CheckBadValues.ar(leak, post = 0)
  val gate_0 = Gate.ar(leak, bad sig_== 0)
  val lim = Pan2.ar(Limiter.ar(gate_0)) * "amp".kr(0.05) // * DelayN.ar(Line.ar(0, 1, 1), 0.2, 0.2)
  Out.ar(0, lim)
}