/*
Created by Force_G                ( https://github.com/ForceG/ForceG-s-Toy )
Enjoy!
*/

Pxo{
	*new{|string,repeats=inf,beatsPerBar=8|var out=[],delta=0;
		string.do({|letter|
			if((letter==$X).or(letter==$x),{
				out = out.add( (delta/beatsPerBar*4) );
				delta=1
			},{ if((letter==$o||(letter==$O)||(letter==$0)),{
				delta=delta+1 }) })
		});
		out = out.add( (delta/beatsPerBar*4)+out.removeAt(0) );
		^Pseq(out,repeats)
	}
}

P0f{
	*new{|string,repeats=inf|var out=[],delta=0;
		string.do{|letter|var letterI=letter.asInteger;
			if(47<letterI&&(letterI<58)) {
				out = out.add( letterI-48 );
			}{ if(96<letterI&&(letterI<103)){
				out = out.add( letterI-87 ) }}}
		^Pseq(out,repeats)
		}
	}


Ptab : Pseq{
	var dict,lastTabTime,<>clock,<>autoFix;

	*new{|autoFix=nil,repeats=inf,clock=nil|
		if(clock.isNil){clock=TempoClock.default};
		^super.new([nil],repeats).init(autoFix,clock)
	}

	init{|argFix,argClock|
		this.list=[];
		autoFix=argFix;
		clock=argClock
		^this
	}

	tab{
		if(lastTabTime.isNil){
			lastTabTime=clock.beats
		}{
			var newTime=clock.beats;
			this.list=this.list.add(newTime-lastTabTime);
			lastTabTime=newTime
		};
		if(autoFix.notNil){this.fix(autoFix)};
		^this
	}

	fix{|step,sync=\near|var newList;
		switch(sync,
			\early,{ list.do{|e|newList=newList.add(floor(e/step)*step)} },
			\late,{ list.do{|e|if(e%step!=0){newList=newList.add(floor(e+step/step)*step)}{newList=newList.add(e)} } },
			\near,{ list.do{|e|newList=newList.add(floor((step/2+e)/step)*step)} } );
		if(newList.size!=0){this.list=newList};
		^this
	}

	reset{ this.list=[];
		lastTabTime=nil;
	}
}

Pin : Prout{
	var busRef;

	*new{|bus,len=inf| var busRef=`(bus);
		^super.new({var c=0; loop{busRef.value.getSynchronous.yield; c=c+1; if(c>=len){nil.yield} }}).init(busRef)
	}

	init{|argBusRef|
		busRef=argBusRef
		^this
	}

	bus{
		^busRef.value
	}

	bus_{|bus|
		busRef.set(bus)
	}
}
		