def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val saw       = Saw.ar(109.95167)
  val width     = Saw.ar(109.95167)
  val in_0      = Pulse.ar(freq = saw, width = width)
  val pan       = LagUD.ar(in_0, timeUp = 0.0021199628, timeDown = -0.0029116)
  val xFade2    = XFade2.ar(inA = 660.6183, inB = saw, pan = pan, level = -669.9117)
  val bRF       = BRF.ar(660.6183, freq = -0.0029116, rq = 409.98706)
  val plus      = 0.4496765 + bRF
  val lFDNoise1 = LFDNoise1.ar(18.803278)
  val fBSineL   = FBSineL.ar(freq = -0.0026550447, im = 0.018322097, fb = 0.0032836022, a = 18.803278, c = 0.0042042704, xi = 665.6453, yi = 1.793959E-4)
  val gbmanN    = GbmanN.ar(freq = 660.6183, xi = -70.555954, yi = -669.9117)
  val in_1      = HPZ2.ar(0.0042042704)
  val trig_0    = -0.0029116 - in_0
  val latch     = Latch.ar(in_1, trig = trig_0)
  val amplitude = Amplitude.ar(333.12207, attack = 0.0042042704, release = 0.018322097)
  val mix       = Mix(Seq[GE](amplitude, latch, gbmanN, fBSineL, lFDNoise1, plus, xFade2))
  val mono      = Mix.Mono(mix)
  ConfigOut(mono)
}
