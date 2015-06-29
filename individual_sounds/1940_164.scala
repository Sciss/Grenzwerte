def ConfigOut(in: GE) = Out.ar(0, Pan2.ar(Limiter.ar(LeakDC.ar(in))))

def EnvGen_Triangle(dur: GE = 1.0f, level: GE = 1.0f, gate: GE = 1, 
  levelScale: GE = 1.0f, levelBias: GE = 0.0f, timeScale: GE = 1.0f): GE = {
    val mkEnv: Env = Env.triangle(dur = dur, level = level)
    EnvGen.ar(mkEnv, gate = gate, levelScale = levelScale,
     levelBias = levelBias, timeScale = timeScale)
}

play {
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val a             = Lag3.ar(-0.0027190964, time = 660.6183)
  val xi            = BRF.ar(660.6183, freq = -0.0029116, rq = 419.73846)
  val sampleRate    = SampleRate.ir
  val freq_0        = sampleRate / 2.0
  val freq_1        = GbmanN.ar(freq = freq_0, xi = -717.26953, yi = 192.43965)
  val m             = BBandStop.ar(219.65942, freq = freq_1, bw = 7.4398813)
  val linCongL      = LinCongL.ar(freq = 1.685643E-4, a = 219.65942, c = 83.65495, m = m, xi = xi)
  val henonC        = HenonC.ar(freq = 333.12207, a = a, b = 1.685643E-4, x0 = -80.40248, x1 = 7.4398813)
  val fBSineL       = FBSineL.ar(freq = 130.35602, im = 48250.26, fb = 83.65495, a = 1.5849164, c = 7.4398813, xi = 64.18257, yi = 7.4398813)
  val amplitude     = Amplitude.ar(47.592278, attack = 7948.9946, release = 0.018322097)
  val ddParam       = HPZ2.ar(1.685643E-4)
  val trig_0        = Gendy2.ar(ampDist = 0.021857161, durDist = amplitude, adParam = -0.0029116, ddParam = ddParam, minFreq = 130.35602, maxFreq = 714.5509, ampScale = 7948.9946, durScale = fBSineL, initCPs = 1.685643E-4, kNum = amplitude, a = -962.5887, c = 48250.26)
  val runningMin    = RunningMin.ar(219.65942, trig = trig_0)
  val level         = Saw.ar(105.56122)
  val peakFollower  = PeakFollower.ar(-962.5887, decay = 48250.26)
  val inB           = 7.4398813 - peakFollower
  val setResetFF    = SetResetFF.ar(trig = 383.39972, reset = 105.56122)
  val lFDNoise1     = LFDNoise1.ar(383.39972)
  val formlet       = Formlet.ar(setResetFF, freq = lFDNoise1, attack = -0.0029116, decay = 83.65495)
  val xFade2        = XFade2.ar(inA = setResetFF, inB = inB, pan = 660.6183, level = level)
  val plus          = xi + 130.35602
  val lFGauss       = LFGauss.ar(dur = 1.0, width = 0.1, phase = fBSineL, loop = -0.0027190964, doneAction = doNothing)
  val toggleFF      = ToggleFF.ar(lFDNoise1)
  val in_0          = EnvGen_CutOff(-0.0027191031, lFGauss, 383.39972, 1.5849164, 130.35602, 419.73846)
  val gate          = Gate.ar(in_0, gate = 623.5792)
  val mix           = Mix(Seq[GE](gate, toggleFF, plus, xFade2, formlet, runningMin, henonC, linCongL))
  val mono          = Mix.Mono(mix)
  ConfigOut(mono)
}
