def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val specs       = PulseDivider.ar(trig = -0.12558462, div = -1.6349065E-4, start = 0.2578632)
  val freqOffset  = IEnvGen.ar(envelope = 0.27029708, index = 0.27029708)
  val freq        = Klank.ar(specs = specs, in = 419.73846, freqScale = 0.2578632, freqOffset = freqOffset, decayScale = 165.68047)
  val yi          = LFTri.ar(freq = freq, iphase = 0.27029708)
  val fBSineL     = FBSineL.ar(freq = 0.041307002, im = 0.2578632, fb = 0.27029708, a = 317.49088, c = -1.6349065E-4, xi = 103.6289, yi = yi)
  val in_0        = GbmanL.ar(freq = 317.49088, xi = 317.49088, yi = 3133.3755)
  val bPZ2        = BPZ2.ar(in_0)
  val unaryOpUGen = bPZ2.sinh
  val mix         = Mix(Seq[GE](unaryOpUGen, fBSineL))
  val mono        = Mix.Mono(mix)
  ConfigOut(mono)
}
