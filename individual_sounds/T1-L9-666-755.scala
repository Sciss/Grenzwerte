play {
RandSeed.ir(1, 3)
val fBSineL   = FBSineL.ar(freq = Seq(419.73846, 419.74), im = 0.278064, fb = 242.49637, a = 0.27029708, c = 103.6289, xi = 327.47897, yi = 242.49637)
val bPF       = BPF.ar(Seq(0.278064, 0.28), freq = 404.10397, rq = -0.0027731077)
val minus     = bPF - fBSineL
val pulse     = Pulse.ar(freq = Seq(0.26891705, 0.27), width = 0.9640592)
val bBandPass = BBandPass.ar(Seq(7.6741614, 7.69), freq = 404.10397, bw = 103.6289)
val xi_0      = VarSaw.ar(freq = Seq(-0.0027731077, -0.0029), iphase = -0.0027731077, width = 419.73846)
val lorenzL   = LorenzL.ar(freq = Seq(-0.0027731077, -0.0029), s = -2.7246692E-4, r = 0.26891705, b = 242.49637, h = 0.024258142, xi = xi_0, yi = 242.49637, zi = 0.024258142)
val gbmanL    = GbmanL.ar(freq = Seq(419.73846, 419.8), xi = 0.2578632, yi = 0.26891705)
val mix       = Mix(Seq[GE](gbmanL, lorenzL, bBandPass, pulse, minus))
val mix_0   = mix // Mix.mono(mix)
val bad       = CheckBadValues.ar(mix_0, id = 0.0, post = 0.0)
val gate      = Gate.ar(mix_0, gate = bad sig_== 0.0)
val lim       = LeakDC.ar(Limiter.ar(LeakDC.ar(gate, coeff = 0.995), level = 1.0, dur = 0.01), coeff = 0.995)
// val fade   = DelayN.ar(FadeIn(audio, "fade-in"), maxDelayTime = 0.02, delayTime = 0.02) * FadeOut(audio, "fade-out") * 1.0 - Attribute(control, "mute", 0.0) * Attribute(control, "gain", 1.0)
// ScanOut("out", lim * fade)
lim // Pan2.ar(lim)
}
