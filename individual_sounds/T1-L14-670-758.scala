play {
RandSeed.ir(1, 5)
val pulseCount    = PulseCount.ar(trig = Seq(633.6489, 633.7), reset = 633.6489)
val s             = MoogFF.ar(pulseCount, freq = Seq(184.53993, 184.6), gain = 184.53993, reset = 4.067585)
val kNum          = LorenzL.ar(freq = Seq(-2726.2134, -2726.3), 
  s = s, r = 0.023583055, b = 633.6489, h = 9.705616, xi = 323.5501, yi = 436.66745, zi = 436.66745)
val ampScale      = GbmanL.ar(freq = Seq(419.73846, 419.8), xi = -2726.2134, yi = 303.30566)
val b_0           = Slew.ar(633.6489, up = 419.73846, down = 9.444879E-4)
val quadC         = QuadC.ar(freq = Seq(0.42893913, 0.43), a = 9.444879E-4, b = b_0, c = -2726.2134, xi = 633.6489)
val delayC        = DelayC.ar(323.5501, maxDelayTime = Seq(0.0015226471, 0.0016), delayTime = 303.30566)
val inA           = Slope.ar(633.6489)
val in_0          = Gendy2.ar(ampDist = Seq(419.73846, 419.8), durDist = 633.6489, adParam = 419.73846, 
  ddParam = 633.6489, minFreq = 38.73199, maxFreq = 323.5501, ampScale = ampScale, 
  durScale = 695.37335, initCPs = 0.023583055, kNum = kNum, a = 0.26494086, c = 0.26494086)
val leastChange_0 = LeastChange.ar(a = 0.26494086, b = 419.73846)
val in_1          = Dust2.ar(Seq.fill(2)(87.92129))
val klank         = Klank.ar(specs = 6.157444, in = Seq(633.6489, 633.7), 
  freqScale = 436.66745, freqOffset = 0.0015029017, decayScale = 419.73846)
val ringz         = Ringz.ar(in_1, freq = 436.66745, decay = 6.157444)
val slope         = Slope.ar(6.157444)
val xFade2        = XFade2.ar(inA = inA, inB = slope, pan = 303.30566, level = pulseCount)
val varSaw        = VarSaw.ar(freq = slope, iphase = -2529.0552, width = Seq(0.42893913, 0.43))
val leastChange_1 = LeastChange.ar(a = Seq(6.157444, 6.16), b = 0.26494086)
val formant       = Formant.ar(fundFreq = Seq(0.0015226471, 0.0016), formFreq = 419.73846, bw = 38.73199)
val im            = HPF.ar(in_0, freq = 323.5501)
val fBSineL       = FBSineL.ar(freq = -2029.8915, im = im, fb = 303.30566, a = slope, c = 0.010040017, xi = 9.444879E-4, yi = 0.010040017)
val mix           = Mix(Seq[GE](0.0, fBSineL, formant, leastChange_1, varSaw, xFade2, ringz, klank, leastChange_0, delayC, quadC))
val mix_0   = mix // Mix.mono(mix)
val bad       = CheckBadValues.ar(mix_0, id = 0.0, post = 0.0)
val gate      = Gate.ar(mix_0, gate = bad sig_== 0.0)
val lim       = LeakDC.ar(Limiter.ar(LeakDC.ar(gate, coeff = 0.995), level = 1.0, dur = 0.01), coeff = 0.995)
// val fade   = DelayN.ar(FadeIn(audio, "fade-in"), maxDelayTime = 0.02, delayTime = 0.02) * FadeOut(audio, "fade-out") * 1.0 - Attribute(control, "mute", 0.0) * Attribute(control, "gain", 1.0)
// ScanOut("out", lim * fade)
lim // Pan2.ar(lim)
}
