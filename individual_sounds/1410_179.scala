def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

val x = play {
  val freq_0    = BPF.ar(245.93669, freq = -1.7752516, rq = 0.017937798)
  val varSaw    = VarSaw.ar(freq = freq_0, iphase = -2.97758E-4, width = 405.06628)
  val in_0      = FSinOsc.ar(freq = -2.97758E-4, iphase = 419.73846)
  val friction  = Klank.ar(specs = 419.73846, in = in_0, freqScale = 419.73846, freqOffset = 0.26891705, decayScale = 0.27273506)
  val ball      = Ball.ar(varSaw, g = 405.06628, damp = 0.0044011557, friction = friction)
  val fBSineL   = FBSineL.ar(freq = 103.6289, im = 405.06628, fb = varSaw, a = 245.93669, c = 1359.637, xi = 419.73846, yi = -1.6349065E-4)
  val freq_1    = GbmanL.ar(freq = 405.06628, xi = -1.6349065E-4, yi = -2.97758E-4)
  val lFPar     = LFPar.ar(freq = freq_1, iphase = -1.6349065E-4)
  val mix       = Mix(Seq[GE](lFPar, fBSineL, ball))
  val mono      = Mix.Mono(mix)
  ConfigOut(mono)
}
