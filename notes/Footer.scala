RandSeed.ir(1, 56789)


val mix       = Mix.Mono(mix_0)
val bad       = CheckBadValues.ar(mix, id = 0.0, post = 0.0)
val gate      = Gate.ar(mix, gate = bad sig_== 0.0)
val lim       = LeakDC.ar(Limiter.ar(LeakDC.ar(gate), dur = 0.01), 0.995)
val env       = DelayN.ar(FadeIn.ar, maxDelayTime = 0.02, delayTime = 0.02)
val amp       = env * FadeOut.ar * (1.0 - Attribute.kr("mute", 0.0)) * Attribute.kr("gain", 1.0)
val sig       = lim * amp
ScanOut(sig)

