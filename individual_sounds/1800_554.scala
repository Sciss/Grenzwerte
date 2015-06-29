def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val delayTime       = GbmanL.ar(freq = 419.73846, xi = -2526.418, yi = 670.28094)
  val level           = BRF.ar(636.937, freq = -0.0029116, rq = 419.73846)
  val xi_0            = level.atan
  val bBandStop       = BBandStop.ar(-0.0054616947, freq = 670.28094, bw = 383.39972)
  val timeDown        = GbmanN.ar(freq = 8.225018E-4, xi = -0.0054616947, yi = 0.36766747)
  val freq_0          = GbmanN.ar(freq = -0.0054616947, xi = xi_0, yi = 7.644996E-4)
  val coeff           = LFTri.ar(freq = freq_0, iphase = 8.225018E-4)
  val in_0            = LeakDC.ar(125.63024, coeff = coeff)
  val gate            = Gate.ar(in_0, gate = 0.0024416046)
  val gbmanL          = GbmanL.ar(freq = 0.0024416046, xi = -1.2799277, yi = 636.937)
  val init            = BBandStop.ar(936.9255, freq = 670.28094, bw = 383.39972)
  val plus            = freq_0 + init
  val logistic        = Logistic.ar(chaos = 83.65495, freq = -0.0054616947, init = init)
  val in_1            = GbmanN.ar(freq = -0.0054616947, xi = xi_0, yi = 7.644996E-4)
  val linPan2         = LinPan2.ar(in_1, pos = 0.0, level = level)
  val lag2UD          = Lag2UD.ar(1.2398051, timeUp = -0.0054616947, timeDown = timeDown)
  val combN           = CombN.ar(19.27107, maxDelayTime = 0.9999994, delayTime = delayTime, decayTime = 2226.5508)
  val lagUD           = LagUD.ar(333.12207, timeUp = 19.27107, timeDown = 55.318325)
  val envGen_Triangle = EnvGen_Triangle(0.19429296, bBandStop, logistic, 2226.5508, timeDown, 0.9999994)
  val mix             = Mix(Seq[GE](envGen_Triangle, lagUD, combN, lag2UD, linPan2, plus, gbmanL, gate, 1.0))
  val mono            = Mix.Mono(mix)
  ConfigOut(mono)
}

