play {
val yi        = VarSaw.ar(freq = 0.27931982, iphase = 66.87137, width = -0.01881874)
val in_0      = StandardN.ar(freq = 0.65676665, k = -0.01881874, xi = -5697.0, yi = yi)
val index     = Saw.ar(67.40677)
val freq_0    = Select.ar(index = index, in = 0.24274482)
val standardN = StandardN.ar(freq = freq_0, k = -171.54652, xi = 0.27931982, yi = yi)
val in_1      = BPeakEQ.ar(in_0, freq = -122.01076, rq = -122.01076, gain = standardN)
val lPZ2      = LPZ2.ar(in_1)
val rq_0      = Trig.ar(57.79411, dur = 57.79411)
val inA       = BLowPass.ar(in_0, freq = -0.01881874, rq = rq_0)
val level     = Schmidt.ar(0.022520326, lo = 0.24274482, hi = 0.27931982)
val b         = Ball.ar(-95.29864, g = 0.27931982, damp = 0.27931982, friction = 0.24274482)
val inB       = CuspL.ar(freq = 0.24274482, a = standardN, b = b, xi = 0.65676665)
val iphase_0  = LinXFade2.ar(inA = inA, inB = inB, pan = -122.01076, level = level)
val varSaw    = VarSaw.ar(freq = 67.40677, iphase = iphase_0, width = 0.27931982)
val gt        = -0.0050865123 > in_1
val mix       = Mix(Seq[GE](gt, varSaw, lPZ2))
val bad       = CheckBadValues.ar(Mix.Mono(mix), id = 0.0, post = 0.0)
val gate      = Gate.ar(Mix.Mono(mix), gate = bad sig_== 0.0)
val lim       = LeakDC.ar(Limiter.ar(LeakDC.ar(gate, coeff = 0.995), level = 1.0, dur = 0.01), coeff = 0.995)
// val fade   = DelayN.ar(FadeIn(audio, "fade-in"), maxDelayTime = 0.02, delayTime = 0.02) * FadeOut(audio, "fade-out") * 1.0 - Attribute(control, "mute", 0.0) * Attribute(control, "gain", 1.0)
// ScanOut("out", lim * )
Pan2.ar(lim)
}
