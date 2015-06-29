def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val ddParam       = HPZ2.ar(1.6179903E-4)
  val bRF           = BRF.ar(660.6183, freq = -0.0029116, rq = 419.73846)
  val timeDown      = -0.0058356896 + bRF
  val lagUD         = LagUD.ar(-1.2799277, timeUp = 219.65942, timeDown = timeDown)
  val durDist       = Amplitude.ar(0.018322097, attack = 0.28420624, release = 7948.9946)
  val blip          = Blip.ar(freq = 0.0044420976, numHarm = 419.73846)
  val fBSineL_0     = FBSineL.ar(freq = 18.71311, im = 0.021857161, fb = 18.71311, a = 0.101056546, c = blip, xi = 7.4398813, yi = 0.101056546)
  val yi_0          = Blip.ar(freq = lagUD, numHarm = 419.73846)
  val a_0           = FBSineL.ar(freq = 0.0042042704, im = 0.021857161, fb = 383.39972, a = 98.5005, c = 623.5792, xi = 18.71311, yi = yi_0)
  val xi_0          = Gendy2.ar(ampDist = 0.021857161, durDist = durDist, adParam = -0.0029116, ddParam = ddParam, minFreq = 83.65495, maxFreq = 642.73553, ampScale = 7948.9946, durScale = fBSineL_0, initCPs = 18.71311, kNum = 623.5792, a = a_0, c = lagUD)
  val freq_0        = GbmanN.ar(freq = 83.65495, xi = xi_0, yi = -717.26953)
  val bBandStop     = BBandStop.ar(219.65942, freq = freq_0, bw = -0.0058356896)
  val pulseCount    = PulseCount.ar(trig = bBandStop, reset = 7948.9946)
  val peakFollower  = PeakFollower.ar(7948.9946, decay = -717.26953)
  val inB           = -962.5887 - peakFollower
  val saw           = Saw.ar(83.65495)
  val hPF           = HPF.ar(lagUD, freq = saw)
  val xFade2        = XFade2.ar(inA = saw, inB = inB, pan = 660.6183, level = 752.094)
  val runningMin    = RunningMin.ar(bRF, trig = 219.65942)
  val dur           = LinCongL.ar(freq = 1.6179903E-4, a = -1.2799277, c = bBandStop, m = 219.65942, xi = bRF)
  val fBSineL_1     = FBSineL.ar(freq = -0.0027191031, im = 0.021857161, fb = 0.0042042704, a = 18.71311, c = 623.5792, xi = 98.5005, yi = blip)
  val lFGauss       = LFGauss.ar(dur = 1.0, width = 0.1, phase = fBSineL_0, loop = fBSineL_1, doneAction = doNothing)
  val tDelay        = TDelay.ar(trig = 0.021857161, dur = dur)
  val lFDNoise1     = LFDNoise1.ar(383.39972)
  val syncSaw       = SyncSaw.ar(syncFreq = 83.65495, sawFreq = fBSineL_1)
  val mix           = Mix(Seq[GE](syncSaw, lFDNoise1, tDelay, lFGauss, runningMin, xFade2, hPF, pulseCount))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
