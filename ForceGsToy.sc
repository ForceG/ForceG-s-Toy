ForceGsToy{
	var <bus,<window,<table,<knob,<slider,<pad,<padx,<pady,<button,lastMousePos,<buttonDownAction,<buttonUpAction,<ioButtonAction;

	*new{
		^super.new.init()
	}

	init{
		bus= Dictionary(0);
		window = Window(\myWindow,Rect(1038,157,400,713));
		window.visible = true;
		window.alwaysOnTop=true;
		window.background=Color.black;

		knob=[[],[],[],[]];
		slider=[];
		pad=[];
		button=[];
		lastMousePos=nil;

		buttonDownAction=Dictionary(0);
		buttonUpAction=Dictionary(0);
		ioButtonAction=Dictionary(0);

		32.do({|i|var knob;
			bus=bus[asSymbol("knob"++floor(i/8).asInteger++(i%8))]=Bus.control;
			knob=EZKnob(window,Rect(i%8*50,floor(i/8)*70,50,70),action:{|knob|bus[asSymbol("knob"++floor(i/8).asInteger++(i%8))].set(knob.value)});
			knob.setColors(
				nil,nil,Color.grey(0.25),nil,Color.hsv(1/8*(i%8),0.5,1),
				background:Color.black,
				knobColors:[Color.hsv(1/8*(i%8),0.7,0.6) ,Color.hsv(1/8*(i%8),0.8,1),Color.grey(0.3), Color.hsv(1/8*(i%8),0.5,1)]);
			knob.knobView.mouseDownAction={|v,x,y,m,b,c|if(c==2){(if(knob.centered){knob.controlSpec.minval=0;v.centered=false}{knob.controlSpec.minval=(-1);v.centered=true})}};
			this.knob[floor(i/8)]=this.knob[floor(i/8)].add(knob)	});
		knob.flop;

		8.do({|i|var slid;bus=bus[asSymbol("slider"++i)]=Bus.control;
			slid=EZSlider(window,Rect(0,i*20+280,200,20),action:{|knob|bus[asSymbol("slider"++i)].set(knob.value)}).setColors(
				nil,nil,Color.hsv(1/8*(i%8),0.5,0.5),Color.grey(0.25),
				nil,Color.hsv(1/8*(i%8),0.5,1),Color.white,Color.hsv(1/8*(i%8),0.3,0.8));
			slid.sliderView.mouseDownAction_({|v,x,y,m,b,c|if(c==2){(if(slid.controlSpec.minval==1.neg){slid.controlSpec.minval=0}{slid.controlSpec.minval=(-1)})}});
			slider=slider.add(slid) });
		8.do({|i|var slid;bus=bus[asSymbol("slider"++(i+8))]=Bus.control;
			slid=EZSlider(window,Rect(i*25+200,280,25,160),layout:\vert,action:{|knob|bus[asSymbol("slider"++(i+8))].set(knob.value)}).setColors(
				nil,nil,Color.hsv(1/8*(i%8),0.5,0.5),Color.grey(0.25),
				nil,Color.hsv(1/8*(i%8),0.5,1),Color.white,Color.hsv(1/8*(i%8),0.3,0.8));
			slid.sliderView.mouseDownAction_({|v,x,y,m,b,c|if(c==2){(if(slid.controlSpec.minval==(-1)){slid.controlSpec.minval=0}{slid.controlSpec.minval=(-1)})}});
			slider=slider.add(slid) });
		4.do({|i|var ens,s,col=[Color(0.45,0.3,0.3),Color(0.3,0.45,0.3),Color(0.45,0.45,0.3),Color(0.3,0.3,0.45)],
			busX=bus[asSymbol("pad"++(i)++"X")]=Bus.control,busY=bus[asSymbol("pad"++(i)++"Y")]=Bus.control;
			ens = [EZNumber(window,Rect(i*100,540,50,20)),EZNumber(window,Rect(i*100+50,540,50,20))];
			s=Slider2D(window,Rect(i*100,440,100,100)).action={
				|knob|
				bus[asSymbol("pad"++i++"X")].set(knob.x);
				bus[asSymbol("pad"++i++"Y")].set(knob.y);
				ens[0].value = knob.x;
				ens[1].value = knob.y;};
			s.background = col[i];
			ens[0].action={|view|s.x=view.value};
			ens[1].action={|view|s.y=view.value};
			ens[0].setColors(numBackground:Color.grey(0.25),numNormalColor:Color.hsv(col[i].asHSV[0],0.5,1));
			ens[1].setColors(numBackground:Color.grey(0.25),numNormalColor:Color.hsv(col[i].asHSV[0],0.5,1));
			pad=pad.add(s);
			padx=padx.add(ens[0]);
			pady=pady.add(ens[1]);
		});

		table=DrawableWaveTable(window,Rect(69,562,330,150));
		table.eText.stringColor=Color.white;
		table.interText.stringColor=Color.white;
		table.wavText.stringColor=Color.white;
		table.samplesText.stringColor=Color.white;

		10.do{|iter_y|4.do{|iter_x|var
			newButton=Button(window,Rect(15*iter_x+4,15*iter_y+562,15,15));
			if(iter_x>0&&(iter_y<5)){
				bus[("button"++(iter_x-1)++iter_y).asSymbol]=Bus.control();
				newButton.states_( [ ["",Color.hsv(iter_y/5,0.8,0.9),Color.hsv(iter_y/5,1,0.5)], ["",Color.hsv(iter_y/5,1,0.5),Color.hsv(iter_y/5,0.8,0.9)] ]);
				newButton.action_{|v|v.value=0;bus[("button"++(iter_x-1)++iter_y).asSymbol].set(0); nil };
				newButton.mouseDownAction_{|v, x, y, mod, buttonNumber, clickCount|
					if(buttonNumber==0){
						v.value=1;
						bus[("button"++(iter_x-1)++iter_y).asSymbol].set(1);
						if(buttonDownAction[(""++(iter_x-1)++iter_y).asSymbol].notNil){buttonDownAction[(""++(iter_x-1)++iter_y).asSymbol].(v)};
						nil }};
				newButton.mouseUpAction_{|v,x,y|if(buttonUpAction[(""++(iter_x-1)++iter_y).asSymbol].notNil&&(0<x)&&(x<15)&&(0<x)&&(x<15)){buttonUpAction[(""++(iter_x-1)++iter_y).asSymbol].(v)}; nil};
				newButton.mouseLeaveAction_{|v, x, y|
					v.value=0;
					bus[("button"++(iter_x-1)++iter_y).asSymbol].set(0); nil };
			}{if(iter_x>0){
				bus[("ioButton"++(iter_x-1)++(iter_y-5)).asSymbol]=Bus.control();
				newButton.action_{|v|bus[("ioButton"++(iter_x-1)++(iter_y-5)).asSymbol].set(v.value);if(ioButtonAction[(""++(iter_x-1)++(iter_y-5)).asSymbol].notNil){ioButtonAction[(""++(iter_x-1)++(iter_y-5)).asSymbol].(v)}};
				newButton.states_( [ ["0",Color.hsv((iter_y-5)/5,0.8,0.9),Color.hsv((iter_y-5)/5,1,0.5)], ["|",Color.hsv((iter_y-5)/5,1,0.5),Color.hsv((iter_y-5)/5,0.8,0.9)] ])
			}{
				bus[("sweeper"++iter_y++"X").asSymbol]=Bus.control();
				bus[("sweeper"++iter_y++"Y").asSymbol]=Bus.control();
				newButton.states_( [ ["",Color.black,Color.hsv(iter_y/10,0.8,0.7)] ]);
				newButton.acceptsMouseOver_(true);
				newButton.mouseDownAction_{|v, x, y, mod, buttonNumber, clickCount|
					if(buttonNumber==1){bus[("sweeper"++iter_y++"X").asSymbol].set(0);bus[("sweeper"++iter_y++"Y").asSymbol].set(0)};
					lastMousePos=[x,y] };
				newButton.mouseMoveAction_{|v, x, y, mod|
					if(lastMousePos.notNil){
						bus[("sweeper"++iter_y++"X").asSymbol].set(bus[("sweeper"++iter_y++"X").asSymbol].getSynchronous()+(x-lastMousePos[0]/100));
						bus[("sweeper"++iter_y++"Y").asSymbol].set(bus[("sweeper"++iter_y++"Y").asSymbol].getSynchronous()+(y-lastMousePos[1]/100));
						lastMousePos=[x,y] }};
				newButton.action_{lastMousePos=nil};
			}};

			button=button.add(newButton);
		}};
	}

	hide{window.visible=false}
	front{window.front}
	close{window.close}
}
