play {
RandSeed.ir(1, 6)
val fb          = FBSineL.ar(freq = Seq(730.197, 730.3), im = 419.73846, fb = 0.25334778, 
  a = 419.73846, c = 0.0018429848, xi = 184.53993, yi = 0.024258142)
val reset       = TIRand.ar(lo = 419.73846, hi = -0.0029116, trig = Seq.fill(2)(0.010493266))
val gbmanL      = GbmanL.ar(freq = Seq(184.53993, 184.6), xi = 4768.4634, yi = 0.010493266)
val bRF         = BRF.ar(-409.91626, freq = Seq(419.73846, 419.8), rq = -0.0029116)
val setResetFF  = SetResetFF.ar(trig = 0.27029708, reset = reset)
val fBSineL     = FBSineL.ar(freq = Seq(0.25334778, 0.254), im = 419.73846, fb = fb, a = 0.25334778, 
  c = 0.0018429848, xi = 730.197, yi = 730.197)
val lFSaw       = LFSaw.ar(freq = Seq(0.0018429848, 0.002), iphase = -409.91626)
val bitOr       = 150.86961 | lFSaw
val mix         = Mix(Seq[GE](bitOr, fBSineL, setResetFF, bRF, gbmanL))
val mix_0   = mix // Mix.mono(mix)
val bad       = CheckBadValues.ar(mix_0, id = 0.0, post = 0.0)
val gate      = Gate.ar(mix_0, gate = bad sig_== 0.0)
val lim       = LeakDC.ar(Limiter.ar(LeakDC.ar(gate, coeff = 0.995), level = 1.0, dur = 0.01), coeff = 0.995)
// val fade   = DelayN.ar(FadeIn(audio, "fade-in"), maxDelayTime = 0.02, delayTime = 0.02) * FadeOut(audio, "fade-out") * 1.0 - Attribute(control, "mute", 0.0) * Attribute(control, "gain", 1.0)
// ScanOut("out", lim * fade)
lim // Pan2.ar(lim)
}
