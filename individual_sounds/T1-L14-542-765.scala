play {
val leastChange = LeastChange.ar(a = 394.84225, b = 327.47897)
val gbmanL      = GbmanL.ar(freq = Seq(394.84225, 394.85), xi = 742.4219, yi = -2726.2134)
val fBSineL     = FBSineL.ar(freq = Seq(859.40076, 859.41), im = 742.4219, fb = -0.0029116, a = 327.47897, c = 0.26494086, xi = 327.47897, yi = -0.46837935)
val lo          = Pulse.ar(freq = Seq(3988.951, 3988.9), width = 327.47897)
// TIRand.ar(lo = lo, hi = 636.34235, trig = -0.0032175202)
val levelScale  = FreeVerb2.ar(inL = 327.47897, inR = 20.02571, mix = -0.4713431, room = 1531.113, damp = fBSineL)
import Curve._
val envGen      = EnvGen.ar(envelope = Env(0.0,Vector(
    Env.Segment(TIRand.ar(Pulse.ar(3988.951,327.47897),636.34235,-0.0032175202), 327.47897, parametric(-4.0f)),
    Env.Segment(3988.951, 0.0, parametric(-4.0f))),1.0,-99.0), 
  gate = 3988.951, levelScale = levelScale, levelBias = 859.40076, timeScale = 9.444879E-4, doneAction = doNothing)
val slope       = Slope.ar(1.2822516)
val lag3        = Lag3.ar(636.34235, time = fBSineL)
val bRF         = BRF.ar(Seq.fill(2)(636.34235), freq = -0.0029116, rq = -49.179382)
val mix_0       = Mix(Seq[GE](bRF, lag3, slope, envGen, gbmanL, leastChange))
val mix_1     = mix_0 // Mix.mono(mix_0)
val bad       = CheckBadValues.ar(mix_1, id = 0.0, post = 0.0)
val gate_0    = Gate.ar(mix_1, gate = bad sig_== 0.0)
val lim       = LeakDC.ar(Limiter.ar(LeakDC.ar(gate_0, coeff = 0.995), level = 1.0, dur = 0.01), coeff = 0.995)
// val fade   = DelayN.ar(FadeIn(audio, "fade-in"), maxDelayTime = 0.02, delayTime = 0.02) * FadeOut(audio, "fade-out") * 1.0 - Attribute(control, "mute", 0.0) * Attribute(control, "gain", 1.0)
// ScanOut("out", lim * fade)
lim // Pan2.ar(lim)
}
