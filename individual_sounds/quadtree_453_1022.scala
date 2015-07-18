// (453,1022)
play {
  RandID.ir(5)
  RandSeed.ir(1, 4) // highly dependent on seed
  
val trig        = TwoZero.ar(19.04878, freq = 4262.3745, radius = -1.1194268E-4)
val toggleFF    = ToggleFF.ar(trig)
val brownNoise  = BrownNoise.ar(19.04878)
val damp        = BPeakEQ.ar(0.5408357, freq = 1.5581306, rq = -2497.34, gain = -520.0794)
val freeVerb    = FreeVerb.ar(41.140858, mix = 0.051799387, room = 41.140858, damp = damp)
val lFNoise1    = LFNoise1.ar(0.051799387)
val maxFreq     = Slew.ar(4262.3745, up = 5.278439, down = 0.0092450725)
val xLine       = XLine.ar(start = 7.024671E-4, end = 41.140858, dur = 0.49523568, doneAction = doNothing)
val gendy1      = Gendy1.ar(ampDist = -13.354166, durDist = -1.1194268E-4, adParam = 1.5581306, ddParam = 0.0092450725, minFreq = -520.0794, maxFreq = maxFreq, ampScale = brownNoise, durScale = -1.1194268E-4, initCPs = brownNoise, kNum = 0.0092450725)
val mix_0       = Mix(Seq[GE](gendy1, xLine, lFNoise1, freeVerb, toggleFF))

  val bad = CheckBadValues.ar(Mix.Mono(mix_0), id = 0.0, post = 0.0)
  val lim = LeakDC.ar(Limiter.ar(LeakDC.ar(Gate.ar(Mix.Mono(mix_0), gate = bad sig_== 0.0))))
  Out.ar(0, lim)
}
