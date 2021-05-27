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
	var dict,lastTabTime,<>clock,<>autoFix,<>sync,sync_time,synced;

	*new{|autoFix=nil,sync=\near,repeats=inf,clock=nil|
		if(clock.isNil){clock=TempoClock.default};
		^super.new([nil],repeats).init(autoFix,clock,sync)
	}

	init{|argFix,argClock,argSync|
		this.list=[];
		autoFix=argFix;
		clock=argClock;
		sync=argSync;
		sync_time=0;
		synced=false;
		^this
	}

	tab{
		if(lastTabTime.isNil){
			lastTabTime=clock.beats;
		}{
			var newTime=clock.beats;
			this.list=this.list.add(newTime-lastTabTime);
			lastTabTime=newTime;
		};
		if((autoFix.notNil).and(list.size>0)){
			if((sync==\early).or((sync==\near).and((list[list.size-1]+sync_time%autoFix)<(autoFix/2))))
			{
				sync_time=(list[list.size-1]%autoFix)+sync_time;
				if(sync_time>autoFix){sync_time=sync_time%autoFix; if(synced!=true){list[list.size-1]=list[list.size-1]+autoFix}};
				list[list.size-1]=floor(list[list.size-1]/autoFix)*autoFix;
				if(synced){synced=false}
			}{
				if((list[list.size-1]%autoFix)+sync_time>autoFix){sync_time=(sync_time+list[list.size-1])%autoFix; list[list.size-1]=floor(list[list.size-1]/autoFix+1)*autoFix; synced=true}
				{
					sync_time=sync_time+(list[list.size-1]%autoFix);
					if((synced.not).and(list[list.size-1]%autoFix>0))
					{list[list.size-1]=floor(list[list.size-1]/autoFix+1)*autoFix; synced=true}
					{list[list.size-1]=floor(list[list.size-1]/autoFix  )*autoFix}
				}
			}
		};
		^this
	}

	fix{|step,sync=\near|var sync_time=0,synced=false;
		list.size.do{|i|
			if((sync==\early).or((sync==\near).and((list[i]+sync_time%step)<(step/2))))
			{
				sync_time=(list[i]%step)+sync_time;
				if(sync_time>step){sync_time=sync_time%step; if(synced!=true){list[i]=list[i]+step}};
				list[i]=floor(list[i]/step)*step;
				if(synced){synced=false}
			}{
				if((list[i]%step)+sync_time>step){sync_time=(sync_time+list[i])%step; list[i]=floor(list[i]/step+1)*step; synced=true}
				{
					sync_time=sync_time+(list[i]%step);
					if((synced.not).and(list[i]%step>0))
					{list[i]=floor(list[i]/step+1)*step; synced=true}
					{list[i]=floor(list[i]/step  )*step}
				}
			}
		};
		^this
	}

	reset{ this.list=[];
		lastTabTime=nil;
		sync_time=0;
		synced=false;
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
		