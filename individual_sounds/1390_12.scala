def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

play {
  val gbmanL    = GbmanL.ar(freq = 419.73846, xi = 317.49088, yi = 6863.464)
  val freq_0    = GbmanL.ar(freq = 242.49637, xi = 317.49088, yi = 678.4506)
  val freq_1    = Klank.ar(specs = 0.001798869, in = 673.7658, freqScale = 673.7658, freqOffset = 317.49088, decayScale = 268.2182)
  val freq_2    = FBSineL.ar(freq = 242.49637, im = 316.04404, fb = -497.0404, a = 317.49088, c = 0.001798869, xi = 242.49637, yi = 268.2182)
  val b         = LFDClipNoise.ar(freq_2)
  val cuspN     = CuspN.ar(freq = freq_1, a = -1.6349065E-4, b = b, xi = -0.0011796366)
  val t2K       = T2K.kr(9.444879E-4)
  val bAllPass  = BAllPass.ar(316.04404, freq = freq_0, rq = t2K)
  val klank     = Klank.ar(specs = t2K, in = 317.49088, freqScale = 0.2578632, freqOffset = -1.6349065E-4, decayScale = 0.2578632)
  val mix       = Mix(Seq[GE](klank, bAllPass, cuspN, gbmanL))
  val mono      = Mix.Mono(mix)
  Pan2.ar(Limiter.ar(LeakDC.ar(mono)))
}
