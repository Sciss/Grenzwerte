def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val peakFollower_0  = PeakFollower.ar(6998.1514, decay = 53.821236)
  val xi              = FSinOsc.ar(freq = -1323.2048, iphase = 0.018322097)
  val peakFollower_1  = PeakFollower.ar(333.12207, decay = 0.38179046)
  val freq_0          = GbmanN.ar(freq = 419.73846, xi = xi, yi = peakFollower_1)
  val bBandStop       = BBandStop.ar(219.65942, freq = freq_0, bw = 0.0042042704)
  val saw             = Saw.ar(53.821236)
  val inB             = peakFollower_0 - 0.0013110363
  val xFade2          = XFade2.ar(inA = saw, inB = inB, pan = 703.5526, level = saw)
  val pos             = HPZ2.ar(18.71311)
  val peakFollower_2  = PeakFollower.ar(333.12207, decay = 0.38179046)
  val coeff           = Amplitude.ar(peakFollower_2, attack = peakFollower_1, release = 0.018322097)
  val freeVerb2       = FreeVerb2.ar(inL = 0.38179046, inR = 0.34497613, mix = 0.38179046, room = 0.0013110363, damp = 0.035733066)
  val leakDC          = LeakDC.ar(0.38179046, coeff = coeff)
  val lFGauss         = LFGauss.ar(dur = -0.019077966, width = 219.65942, phase = 83.65495, loop = 419.73846, doneAction = doNothing)
  val lFDNoise1       = LFDNoise1.ar(83.65495)
  val x1              = Formlet.ar(0.34497613, freq = freq_0, attack = peakFollower_2, decay = 0.0013110363)
  val x               = HenonL.ar(freq = 0.015733983, a = 0.38179046, b = 0.3, x0 = 0.0013110363, x1 = x1)
  val rotate2         = Rotate2.ar(x = x, y = 703.5526, pos = pos)
  val lag3            = Lag3.ar(0.004512733, time = 419.73846)
  val mix_0           = Mix(Seq[GE](lag3, rotate2, lFDNoise1, lFGauss, leakDC, freeVerb2, xFade2, bBandStop))
  val mono            = Mix.Mono(mix_0)
  ConfigOut(mono)
}
