define(["compose"], function (Compose) {
	
			// smart animation frame requesting
	var requestAnimationFrame = (function(){
      	return  window.requestAnimationFrame       || 
              window.webkitRequestAnimationFrame || 
              window.mozRequestAnimationFrame    || 
              window.oRequestAnimationFrame      || 
              window.msRequestAnimationFrame     || 
              function( callback ){
                window.setTimeout(callback, 1000 / 60);
              };
    })();
	
	// create the Renderer class
	var FPSTimer = Compose(function (FPS, drawCallback) {
	
		this.FPS = FPS;
		this.lastDrawTime = 0;
		this.callback = drawCallback;
		this.enabled = false;
		this.drawTimes = [];
		
		
		/*function redraw()*/
		
		// redraw function
		var that = this;
		this.redraw = function() {
			
			// get the dt from the previous draw
			var curTime = new Date().getTime();
			var dt = curTime - that.lastDrawTime;
			that.lastDrawTime = curTime;
			that.callback(dt);
				
			// update the draw times
			that.drawTimes[that.drawTimes.length] = dt;
			if (that.drawTimes.length > that.FPS) that.drawTimes.shift();
			
			// keep trying
			//if (that.enabled) requestAnimationFrame(that.redraw);
			//if (that.enabled) setTimeout(that.redraw,1000/this.FPS);
		}
		
	},{
		start: function() {
			this.lastDrawTime = new Date().getTime() - 1000 / this.FPS;
			this.enabled = true;
			this.timer = window.setInterval(this.redraw, 1000 / this.FPS);
			//this.redraw();
		},
		
		stop: function() {
			this.enabled = false;
			window.clearInterval(this.timer);
		},
		
		getFPS: function() {
			var sum = 0;
			for (var i = 0; i < this.drawTimes.length; ++i) {
				sum += this.drawTimes[i];
			}
			return Math.round(this.drawTimes.length / sum * 1000 * 10) / 10;
		}
	});
	
    return {
        FPSTimer: FPSTimer
    }
});