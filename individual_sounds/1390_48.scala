def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val trig1         = Trig1.ar(362.69864, dur = 11.697126)
  val freq_0        = BrownNoise.ar(trig1)
  val lFDClipNoise  = LFDClipNoise.ar(freq_0)
  val freq_1        = lFDClipNoise <= 362.69864
  val gbmanL        = GbmanL.ar(freq = freq_1, xi = -220.82477, yi = -220.82477)
  val aPF           = APF.ar(319.7083, freq = 42.375736, radius = trig1)
  val xpos          = Ringz.ar(319.7083, freq = 42.375736, decay = -220.82477)
  val trig_0        = Logistic.ar(chaos = 11.697126, freq = -220.82477, init = 0.57593817)
  val level         = SetResetFF.ar(trig = trig_0, reset = 11.697126)
  val trig_1        = Pan4.ar(-220.82477, xpos = xpos, ypos = -220.82477, level = level)
  val lFDNoise3     = LFDNoise3.ar(319.7083)
  val pulseDivider  = PulseDivider.ar(trig = trig_1, div = 319.7083, start = 11.697126)
  val mix           = Mix(Seq[GE](pulseDivider, lFDNoise3, aPF, -220.82477, 0.0, gbmanL))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
