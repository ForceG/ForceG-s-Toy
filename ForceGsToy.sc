ForceGsToy{
	var <bus,<window,<table;

	*new{
		^super.new.init()
	}

	init{
		bus= Dictionary(0);
		window = Window(\myWindow,Rect(1038,157,400,713));
		window.visible = true;
		window.alwaysOnTop=true;
		window.background=Color.black;

		32.do({|i|var knob;
			bus=bus[asSymbol("knob"++floor(i/8).asInteger++(i%8))]=Bus.control;
			knob=EZKnob(window,Rect(i%8*50,floor(i/8)*70,50,70),action:{|knob|bus[asSymbol("knob"++floor(i/8).asInteger++(i%8))].set(knob.value)});
			knob.setColors(
				nil,nil,Color.grey(0.25),nil,Color.hsv(1/8*(i%8),0.5,1),
				background:Color.black,
				knobColors:[Color.hsv(1/8*(i%8),0.7,0.6) ,Color.hsv(1/8*(i%8),0.8,1),Color.grey(0.3), Color.hsv(1/8*(i%8),0.5,1)]);
			knob.knobView.mouseDownAction={|v,x,y,m,b,c|if(c==2){(if(knob.centered){knob.controlSpec.minval=0;v.centered=false}{knob.controlSpec.minval=(-1);v.centered=true})}}});

		8.do({|i|var slid;bus=bus[asSymbol("slider"++i)]=Bus.control;
			slid=EZSlider(window,Rect(0,i*20+280,200,20),action:{|knob|bus[asSymbol("slider"++i)].set(knob.value)}).setColors(
				nil,nil,Color.hsv(1/8*(i%8),0.5,0.5),Color.grey(0.25),
				nil,Color.hsv(1/8*(i%8),0.5,1),Color.white,Color.hsv(1/8*(i%8),0.3,0.8));
		slid.sliderView.mouseDownAction_({|v,x,y,m,b,c|if(c==2){(if(slid.controlSpec.minval==1.neg){slid.controlSpec.minval=0}{slid.controlSpec.minval=(-1)})}})});
		8.do({|i|var slid;bus=bus[asSymbol("slider"++(i+8))]=Bus.control;
			slid=EZSlider(window,Rect(i*25+200,280,25,160),layout:\vert,action:{|knob|bus[asSymbol("slider"++(i+8))].set(knob.value)}).setColors(
				nil,nil,Color.hsv(1/8*(i%8),0.5,0.5),Color.grey(0.25),
				nil,Color.hsv(1/8*(i%8),0.5,1),Color.white,Color.hsv(1/8*(i%8),0.3,0.8));
			slid.sliderView.mouseDownAction_({|v,x,y,m,b,c|if(c==2){(if(slid.controlSpec.minval==(-1)){slid.controlSpec.minval=0}{slid.controlSpec.minval=(-1)})}})});
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
		});

		table=DrawableWaveTable(window,Rect(1,562,330,150));
		table.eText.stringColor=Color.white;
		table.interText.stringColor=Color.white;
		table.wavText.stringColor=Color.white;
		table.samplesText.stringColor=Color.white;
	}
}
