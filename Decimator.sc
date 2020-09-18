Decimator{
	*ar{|in,samplerate=44100,bitres=32| ^((Gate.ar(in, Impulse.ar(samplerate))*2.pow(bitres)).floor/2.pow(bitres)) }

*kr{|in,samplerate=44100,bitres=32| ^((Gate.kr(in, Impulse.kr(samplerate))*2.pow(bitres)).floor/2.pow(bitres)) }
}