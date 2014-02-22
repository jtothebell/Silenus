
require(["renderer","fpstimer"], function callback(renderModule, fpstimer) {
	
	// first, load the JSON
	require(["json!upload/data.json"], function (json) {
		
		// animation
		function startAnimation(renderer) {
			
			// create FPS counter
			var fpsCounter = document.createElement('div');
			
			// add to DOM
			var content = document.getElementById("animation");
			content.appendChild(fpsCounter);
			content.appendChild(renderer.getCanvas());
			
			// start rendering at the correct frame rate
			var frame = 0;
			var timer = new fpstimer.FPSTimer(renderer.getFrameRate(), function() {
				//if (frame == 1) return;
				frame = (frame + 1) % renderer.getAnimationLength();
				renderer.draw(frame);
				fpsCounter.innerHTML = "FPS: " + timer.getFPS();
			});
			timer.start();
		}
		
		// create renderer
		var renderer = new renderModule.Renderer(json, {
			onDone: function(renderer) {
				startAnimation(renderer);
			}
		});
	});
});