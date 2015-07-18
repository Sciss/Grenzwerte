// (458,1023)
play {
  RandID.ir(4)
  RandSeed.ir(1, 12) // highly dependent on seed
  
  val leastChange   = LeastChange.ar(a = 63.436977, b = 5.908494)
  val adParam       = FreeVerb2.ar(inL = 9456.961, inR = 9456.961, mix = 81.04753, room = 97.025475, damp = leastChange)
  val durScale      = DetectSilence.ar(63.436977, amp = leastChange, dur = 224.32455, doneAction = doNothing)
  val peakFollower  = PeakFollower.ar(15.453109, decay = 17.015131)
  val setResetFF    = SetResetFF.ar(trig = 17.015131, reset = 5.908494)
  val gendy2        = Gendy2.ar(ampDist = 1.3109143, durDist = 5.908494, adParam = adParam, ddParam = 63.436977, minFreq = -468.03256, maxFreq = 6414.315, ampScale = 17.015131, durScale = durScale, initCPs = 0.017860591, kNum = 17.015131, a = 0.017860591, c = 1.3109143)
  val mix_0         = Mix(Seq[GE](gendy2, setResetFF, peakFollower))
  val bad = CheckBadValues.ar(Mix.Mono(mix_0), id = 0.0, post = 0.0)
  val lim = LeakDC.ar(Limiter.ar(LeakDC.ar(Gate.ar(Mix.Mono(mix_0), gate = bad sig_== 0.0))))
  Out.ar(0, lim)
}
