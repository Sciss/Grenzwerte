play {
  val xi        = LFTri.ar(freq = 59.051933, iphase = 15.981656)
  val a_0       = Saw.ar(61.033268)
  val a_1       = CuspN.ar(freq = 59.051933, a = a_0, b = Seq(60.218716, 60.22), xi = xi)
  val rq        = LatoocarfianL.ar(freq = 149.33986, a = a_1, b = Seq(15.981656, 15.99), 
    c = Seq(25.823067, 25.83), d = Seq(59.051933, 59.06), xi = xi, yi = Seq(61.033268, 61.04))
  val level     = RLPF.ar(60.218716, freq = 59.051933, rq = rq)
  val pan       = GbmanL.ar(freq = 61.033268, xi = Seq(59.051933, 59.06), yi = Seq(15.981656, 15.99))
  val inL       = pan wrap2 61.033268
  val freeVerb2 = FreeVerb2.ar(inL = inL, inR = 60.218716, mix = 59.051933, room = 53.16145, damp = 53.16145)
  val inA       = pan atan2 59.051933
  val linXFade2 = LinXFade2.ar(inA = inA, inB = 53.16145, pan = pan, level = level)
  val sweep     = Sweep.ar(trig = -499.2625, speed = 59.051933)
  val mix_0     = Mix(Seq[GE](sweep, linXFade2, freeVerb2))
  Out.ar(0, Limiter.ar(LeakDC.ar(mix_0)))
}
