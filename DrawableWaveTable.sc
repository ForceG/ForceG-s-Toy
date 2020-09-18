DrawableWaveTable{
	var <parent,<bounds,<>samples,<userView,<samplesNum,<e,<table,<eText,<samplesText,<copyButton,<pasteButton;
	var <tableUpButton,<tableDownButton,<tableNumber,<smoothButton,<wavButton,<interButton,<normalizeButton,<mouseInterButton;
	var <>tables,<>envs,tableI,<interText,<wavText,<>lastDrawPos=nil,<newWindowButton;
	var <>linkedTables,dynBuffer,server,but0;

	*new{|parent=nil,bounds=nil,samples=128,server|
		var argServer=Server.default,argBounds=if(bounds.isNil){Rect(0,0,330,150)}{bounds},
		argParent=if(parent.isNil){Window("Force_G's DrawableWaveTable",Rect(400,400,330,150))}{parent},
		userRect=(argBounds.copy().width=argBounds.width*0.9),
		numberRect=(((argBounds.copy().width=argBounds.width*0.1).height=argBounds.height*0.2).left=argBounds.width*0.9);
		if(server.notNil){argServer=server};

		^super.new.init(argParent,argBounds,samples,argServer);
	}
	init{|argParent,argBounds,argSamples,argServer|
		parent = argParent;
		server = argServer;
		bounds = argBounds;
		samples = argSamples;
		userView = UserView(argParent,Rect(bounds.left,bounds.top,bounds.width*0.9,bounds.height));
		samplesText = StaticText(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.9),bounds.width*0.02,bounds.height*0.1)).string_("s").align_(\right);
		samplesNum = EZNumber(argParent,
			Rect(bounds.left+(bounds.width*0.92),bounds.top+(bounds.height*0.9),bounds.width*0.08,bounds.height*0.1)).action_({|v|
			samples=v.value;
			this.refresh() });
		eText = StaticText(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top,bounds.width*0.02,bounds.height*0.1)).string_("e").align_(\right);
		e = EZNumber(parent,
			Rect(bounds.left+(bounds.width*0.92),bounds.top,bounds.width*0.08,bounds.height*0.1));
		e.controlSpec.step=0.001;
		e.action={tables[tableNumber.value][3]=e.value;this.refresh()};
		table = [];
		linkedTables=[this];
		tables=[];
		dynBuffer=[];
		but0=false;

		parent.front.alwaysOnTop=true;
		parent.onClose={linkedTables.pop(linkedTables.indexOf(this));if(linkedTables.size==0){}};
		userView.background=Color(0.3*0.9,0.5*0.9,0.45*0.9);

		interText = StaticText(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.1),bounds.width*0.1,bounds.height*0.1)).string_("lin").align_(\center);

		512.do{	table=table.add(0); dynBuffer=dynBuffer.add(nil)};
		tables=tables.add([table,interText.string.asSymbol,samples,e.value,[]]);
		99.do({tables=tables.add([table.copy(),interText.string.asSymbol,samples,e.value,[]])});

		samplesNum.controlSpec.maxval=512;
		samplesNum.controlSpec.minval=1;
		samplesNum.controlSpec.step=1;
		samplesNum.value=samples;
		samplesNum.action={|v|samples=v.value;
			tables[tableNumber.value][2]=v.value;
			this.refresh()};

		tableNumber = EZNumber(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.6),bounds.width*0.05,bounds.height*0.1));
		tableNumber.action_({this.refresh}).controlSpec.step_(1).maxval_(99);
		tableUpButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.95),bounds.top+(bounds.height*0.7),bounds.width*0.05,bounds.height*0.1)).states_(
			[[">",Color.black,Color.white]]).action_({
			tableNumber.value=tableNumber.value+1;
			this.refresh() });
		tableDownButton  = Button(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.7),bounds.width*0.05,bounds.height*0.1)).states_(
			[["<",Color.black,Color.white]]).action_({
			tableNumber.value=tableNumber.value-1;
			this.refresh() });
		smoothButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.3),bounds.width*0.05,bounds.height*0.1)).states_(
			[["s",Color.black,Color.white]]).action_({
			this.smooth(1);
			this.refresh()});
		normalizeButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.95),bounds.top+(bounds.height*0.3),bounds.width*0.05,bounds.height*0.1)).states_(
			[["n",Color.black,Color.white]]).action_({
			this.normalize();
			this.refresh()});
		mouseInterButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.2),bounds.width*0.05,bounds.height*0.1)).states_(
			[["mi",Color.grey,Color.white],["mi",Color.white,Color.grey]]);
		copyButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.8),bounds.width*0.05,bounds.height*0.1)).states_(
			[["C",Color.black,Color.white]]).action_({linkedTables.do{|v|
			v.wavText.string="usr"++tableNumber.value.asInteger}});
		interButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.95),bounds.top+(bounds.height*0.2),bounds.width*0.05,bounds.height*0.1)).states_(
			[["i",Color.black,Color.white]]).action_({
			switch(interText.string.asSymbol,
				"---".asSymbol,{interText.string_("lin")},
				\lin,{interText.string_("exp")},
				\exp,{interText.string_("sin")},
				\sin,{interText.string_("sqr")},
				\sqr,{interText.string_("cub")},
				\cub,{interText.string_("---")} );
			tables[tableNumber.value][1]=interText.string.asSymbol;
			this.refresh() });
		pasteButton  = Button(argParent,
			Rect(bounds.left+(bounds.width*0.95),bounds.top+(bounds.height*0.8),bounds.width*0.05,bounds.height*0.1)).states_(
			[["P",Color.black,Color.white]]).action_({
			if(pasteButton.states[0][2]==Color.white){
				pasteButton.states_([["P",Color.black,Color.hsv(0.1,1,1)]])
			}{
				pasteButton.states_([["P",Color.black,Color.white]]);
				switch(wavText.string[..2].asSymbol,
					\usr,{samples.do{|i|tables[tableNumber.value][0][i]=tables[wavText.string[3..].asInteger][0][i]};
						tables[tableNumber.value][1]=tables[wavText.string[3..].asInteger][1];
						tables[tableNumber.value][2]=tables[wavText.string[3..].asInteger][2];
						tables[tableNumber.value][3]=tables[wavText.string[3..].asInteger][3];
						tables[tableNumber.value][4]=tables[wavText.string[3..].asInteger][4] },
					\sin,{samples.do{|i|table[i]=sin(2*pi/samples*i)/2+0.5}},
					\saw,{samples.do{|i|table[i]=i/(samples-1)}},
					\sqr,{samples.do{|i|table[i]=(i/(samples-1)+0.5).floor}},
					\rnd,{samples.do{|i|table[i]=1.0.rand}},
					\flt,{samples.do{|i|table[i]=0.5}},
					"___".asSymbol,{samples.do{|i|table[i]=0}} );
				this.refresh;
			};
		}).mouseLeaveAction_({pasteButton.states_([["P",Color.black,Color.white]])});

		wavText = StaticText(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.4),bounds.width*0.1,bounds.height*0.1)).string_("sin").align_(\center);
		wavButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.9),bounds.top+(bounds.height*0.5),bounds.width*0.1,bounds.height*0.1)).states_(
			[["wav",Color.black,Color.white]]).action_({
			switch(wavText.string[..2].asSymbol,
				\usr,{wavText.string_("sin")},
				\sin,{wavText.string_("saw")},
				\saw,{wavText.string_("sqr")},
				\sqr,{wavText.string_("rnd")},
				\rnd,{wavText.string_("flt")},
				\flt,{wavText.string_("___".asSymbol)},
				"___".asSymbol,{wavText.string_("sin")} );
			linkedTables.do{|v|if(v!=this){v.wavText.string=this.wavText.string}} });
		newWindowButton = Button(argParent,
			Rect(bounds.left+(bounds.width*0.95),bounds.top+(bounds.height*0.6),bounds.width*0.05,bounds.height*0.1)).states_(
			[["+w",Color.black,Color.white]]).action_({var newWindow=DrawableWaveTable();
			newWindow.table=table;
			newWindow.samples=samples;
			newWindow.e.value=e.value;
			newWindow.tables=tables;
			newWindow.envs=envs;
			newWindow.tableNumber.value=tableNumber.value;
			newWindow.wavText.string=wavText.string;
			linkedTables.add(newWindow);
			newWindow.linkedTables=linkedTables;
			newWindow.refresh;
		});

		userView.drawFunc = { |v|var
			simpleTable=(this.rdp(table,e.value**2,0,samples-1)++tables[tableNumber.value][4]).sort{|a,b|a.x<b.x};
			Pen.strokeColor = Color.hsv(0.6,0.1,1);
			Pen.moveTo((simpleTable[0].x*v.bounds.width)@(1-simpleTable[0].y*v.bounds.height));

			if(e.value>0) {(simpleTable.size-1).do{|i|var p=simpleTable[i],np=simpleTable[i+1];
				if(p.x==np.x){Pen.lineTo(np.x/samples*v.bounds.width@1-np.y*v.bounds.height)}
				{(np.x-p.x).do{|i2|	switch(interText.string.asSymbol,
					"---".asSymbol,{Pen.lineTo((p.x+i2/(samples)*v.bounds.width)@(1-p.y*v.bounds.height))},
					\lin,{Pen.lineTo((p.x+i2/(samples)*v.bounds.width)@(1-(i2*(np.y-p.y)/(np.x-p.x)+p.y)*v.bounds.height))},
					\exp,{var x=i2/(np.x-p.x); Pen.lineTo((p.x+i2/(samples)*v.bounds.width)@(1-
						(x*(exp(x.neg+1*1.15).neg+2) + (exp(x*1.15)-1*(1-x))*(np.y-p.y)+p.y)   *v.bounds.height))},
					\sin,{Pen.lineTo((p.x+i2/(samples)*v.bounds.width)@(1-(cos( i2*pi/(np.x-p.x) )-1/(-2)*(np.y-p.y)+p.y)*v.bounds.height))},
					\sqr,{var x=i2/(np.x-p.x); Pen.lineTo((p.x+i2/(samples)*v.bounds.width)@
						(1-( x*(((x-1)**2).neg+1) + (x**2*(1-x)) * (np.y-p.y) +p.y )  *v.bounds.height))},
					\cub,{var x=i2/(np.x-p.x); Pen.lineTo((p.x+i2/(samples)*v.bounds.width)@(1- ( x*(abs(x-1**3).neg+1) +(abs(x**3)*(1-x)) *(np.y-p.y) +p.y) *v.bounds.height))},
				)}}
			};
			Pen.lineTo(v.bounds.width@(1-simpleTable[0].y*v.bounds.height));
			Pen.stroke};
			/*
			(simpleTable.size-1).do{|i|
				switch((interText.string.asSymbol),
					"---".asSymbol,{Pen.lineTo((simpleTable[i+1].x/(samples)*v.bounds.width)@(1-simpleTable[i].y*v.bounds.height));
						Pen.lineTo((simpleTable[i+1].x/(samples)*v.bounds.width)@(1-simpleTable[i+1].y*v.bounds.height)) },
					\lin,{Pen.lineTo((simpleTable[i+1].x/(samples)*v.bounds.width)@(1-simpleTable[i+1].y*v.bounds.height))} );
				if(interText.string.asSymbol!=\lin&&(interText.string.asSymbol!="---".asSymbol)){var
					p1=(simpleTable[i].x/(samples)*v.bounds.width)@(1-simpleTable[i].y*v.bounds.height),
					p2=(simpleTable[i+1].x/(samples)*v.bounds.width)@(1-simpleTable[i+1].y*v.bounds.height);
					Pen.curveTo(p2,(p1.x+p2.x)/2@(p1.y),(p1.x+p2.x)/2@(p2.y))
				}};
			*/

			Pen.strokeColor = Color.hsv(0.1,1,1);
			Pen.moveTo(0@table[0]);
			(samples+1).do(){|i|
				Pen.lineTo((v.bounds.width/(samples)*(i))@(1-table[(i%samples)]*v.bounds.height));
			};
			Pen.stroke;

			Pen.strokeColor = Color.grey(0.25);
			Pen.moveTo(0@(v.bounds.height/2));
			Pen.lineTo(v.bounds.width@(v.bounds.height/2));
			Pen.stroke;
			Pen.strokeColor = Color.green;
			Pen.fillColor = Color.green;
			tables[tableNumber.value][4].do{|p|Pen.fillOval(Rect((p.x/samples*v.bounds.width)-1,(1-p.y*v.bounds.height)-1,3,3))}
		};

		userView.mouseMoveAction={|v,x,y|
			if( (0<=x)&&(x<=(v.bounds.width-1))&&(0<=y)&&(y<=v.bounds.height)&&(but0==true) ){
				tables[tableNumber.value][0][(x/v.bounds.width*samples).floor] = 1-(y/v.bounds.height);
				if(mouseInterButton.value==1){
					if(this.lastDrawPos.notNil){if(this.lastDrawPos.x-x!=0){var lastIx=this.lastDrawPos.x; for(this.lastDrawPos.x,x){
						|ix|( abs((ix/v.bounds.width*samples).floor-(lastIx/v.bounds.width*samples).floor)+1 ).do{|interI|var addI=interI;
							if(y-lastDrawPos.y!=0){
								if(x<this.lastDrawPos.x){addI=interI.neg+1};
								tables[tableNumber.value][0][(ix/v.bounds.width*samples+addI).floor%512] = 1-( (y-this.lastDrawPos.y)*((ix+addI)-lastDrawPos.x/(x-lastDrawPos.x))+this.lastDrawPos.y/v.bounds.height)}
							{tables[tableNumber.value][0][(ix/v.bounds.width*samples+addI).floor%512] = 1-(y/v.bounds.height)};
						};
						lastIx=ix
					}}};
					this.lastDrawPos=(x@y)
				}{this.lastDrawPos=nil};
				this.refresh()};};

		userView.mouseDownAction={|v,x,y,mod,bn|if(bn==0){but0=true}};

		userView.mouseUpAction={|v,x,y,mod,bn|this.lastDrawPos=nil;
			if(bn==1){var found=nil;
				tables[tableNumber.value][4].size.do{|i|if(tables[tableNumber.value][4][i].dist(floor(x/v.bounds.width*samples)@1-(y/v.bounds.height))<2){found=i}{
					if(tables[tableNumber.value][4][i].x==(floor(x/v.bounds.width*samples))){
					tables[tableNumber.value][4][i].y= 1-(y/v.bounds.height)  }}};
				if(found.notNil){tables[tableNumber.value][4].removeAt(found)}
				{tables[tableNumber.value][4]=tables[tableNumber.value][4].add(floor(x/v.bounds.width*samples)@1-(y/v.bounds.height))};
				this.refresh };
			if(bn==0){but0=false}}
	}

	table_ {|val|table=val;this.refresh()}

	asBuffer{|n,server|var
		argN=if(n.isNil){tableNumber.value}{n};
		if(server.isNil){server=Server.default};
		^Buffer.sendCollection(server,tables[argN][0][..tables[argN][2]-1],1)
	}

	asEnv{|t=1,n|var curve=\hold,lastP=0,levels=[],times=[],
		argN=if(n.isNil){n=tableNumber.value}{n},
		newTable = (this.rdp(table,e.value**2,0,samples-1)++tables[tableNumber.value][4]).sort{|a,b|a.x<b.x};
		if(tables[argN][1].asSymbol!="---".asSymbol){
			curve=tables[tableNumber.value][1]};
		newTable.do{|p|
			levels=levels.add(p.y);
			if(levels.size!=1){times=times.add(p.x-lastP.x)};
			lastP=p;
		};
		^Env(levels,times*t/tables[tableNumber.value][2],curve)
	}

	dynBuffer{|n|var i=n;
		if(i.isNil){i=tableNumber.value};
		if(dynBuffer[i].isNil){dynBuffer[i]=this.asBuffer(i)};
		if(dynBuffer[i].bufnum.isNil){dynBuffer[i]=this.asBuffer(i)};
		^dynBuffer[i]
	}

	rdp {|waveTable,e,p1=0,p2=nil,loop=false,final=true|var m,n,newTable=[], maxDist=0,furthestI=p1;
		p2.isNil.if{p2=waveTable.size-1};
		loop.if{p2=waveTable.size;waveTable=waveTable.copy.add(waveTable[0])};
		m=waveTable[p1]-waveTable[p2]/(p1-p2);
		n=p1.neg*m+waveTable[p1];
		(p2-p1+1).do{|val|var
			x=p1+val,
			y=waveTable[x],
			d=this.getDist(x,y,m,n);
			if(d>maxDist){maxDist=d;furthestI=x};
		};
		if((maxDist>=e)&&(p1!=furthestI)&&(p2!=furthestI)){
			newTable=this.rdp(waveTable,e,p1,furthestI,false,false) ++ [furthestI@waveTable[furthestI]] ++ this.rdp(waveTable,e,furthestI,p2,false,false);
			final.if{newTable=[p1@waveTable[p1]]++newTable++[p2@waveTable[p2]]};
			^newTable
		}{
			final.if{
				loop.if
				{^[waveTable.size@waveTable[p1]]}
				{^([p1@waveTable[p1]]++[p2@waveTable[p2]])}
			}
			^[]
		}
	}

	getDist {|x,y,m,n| var m2,n2,sx,sy;
		if(m==0){^abs(y-n)};
		m2=1/m.neg;
		n2=x.neg*m2+y;
		sx=n-n2/(m2-m);
		sy=sx*m+n;
		^sqrt((x-sx)**2+(y-sy**2))
	}

	smooth{|iterations|
		iterations.do{var newTable=[];
			samples.do{|i|
				newTable=newTable.add(table[i]*7/8 + (table[(i+1)%samples]/16) + (table.[(i-1).wrap(0,samples-1)]/16));
			};
			samples.do{|i|
				table[i]=newTable[i]}
		}
	}

	normalize{var min=1,max=0,delta=0;
		samples.do{|i|
			if(table[i]>max){max=table[i]};
			if(table[i]<min){min=table[i]} };
		delta=max-min;
		if(delta==0){
			samples.do{|i|
				table[i]=0.5}
		}{
			samples.do{|i|
				table[i]=table[i]-min/delta}};
	}

	refresh{
		linkedTables.do{|v|
			if(v.tableNumber.value==this.tableNumber.value){
				if(v.table!=tables[tableNumber.value][0]){v.table=tables[tableNumber.value][0]};
				if(v.interText.string.asSymbol!=tables[tableNumber.value][1]){v.interText.string=tables[tableNumber.value][1]};
				if(v.samples!=tables[tableNumber.value][2]){v.samples=tables[tableNumber.value][2]};
				if(v.samplesNum.value!=tables[tableNumber.value][2]){v.samplesNum.value=tables[tableNumber.value][2]};
				if(v.e.value!=tables[tableNumber.value][3]){v.e.value=tables[tableNumber.value][3]};
				v.userView.refresh}};
		if(dynBuffer[tableNumber.value].notNil){if(dynBuffer[tableNumber.value].bufnum.notNil){var interTable=table.copy;
			if(dynBuffer[tableNumber.value].numFrames!=samples){
				dynBuffer[tableNumber.value].numFrames=samples;
				dynBuffer[tableNumber.value].alloc({TemposClock.default.sched(0.01,{this.refresh})})};
			if(e.value>0) {var simpleTable=(this.rdp(table,e.value**2,0,samples-1)++tables[tableNumber.value][4]).sort{|a,b|a.x<b.x};
				(simpleTable.size-1).do{|i|var p=simpleTable[i],np=simpleTable[i+1];
					if(p.x==np.x){}
					{(np.x-p.x).do{|i2|
						switch(interText.string.asSymbol,
							"---".asSymbol,{interTable[p.x+i2]=p.y},
							\lin,{interTable[p.x+i2]=i2*(np.y-p.y)/(np.x-p.x)+p.y},
							\exp,{var x=i2/(np.x-p.x);interTable[p.x+i2]=(x*(exp(x.neg+1*1.15).neg+2) + (exp(x*1.15)-1*(1-x))*(np.y-p.y)+p.y)},
							\sin,{interTable[p.x+i2]=cos( i2*pi/(np.x-p.x) )-1/(-2)*(np.y-p.y)+p.y},
						\sqr,{var x=i2/(np.x-p.x);interTable[p.x+i2]= x*(((x-1)**2).neg+1) + (x**2*(1-x)) * (np.y-p.y) +p.y },
							\cub,{var x=i2/(np.x-p.x);interTable[p.x+i2]=  x*(abs(x-1**3).neg+1) +(abs(x**3)*(1-x)) *(np.y-p.y) +p.y},
					)}}
			}};
			dynBuffer[tableNumber.value].setn(0,interTable)}
	}}

	ar{|num,frq=440,mul=1,add=0|
		^(PlayBuf.ar(1,this.dynBuffer(num),frq*this.tables[num][2]/SampleRate.ir,loop:1)-0.5*mul+add) }
	kr{|num,time=1,mul=1,add=0,loop=0,doneAction|
		if(doneAction.isNil){doneAction=0};
		^(PlayBuf.kr(1,this.dynBuffer(num),(this.tables[num][2]/ControlRate.ir)/time,loop:loop,doneAction:doneAction)*mul+add) }
}


