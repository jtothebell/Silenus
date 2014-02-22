/*
	The MIT License (MIT)
	Copyright (c) 2012 Karel Crombecq, Sileni Studios
	
	Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	
	
	Silenus website: http://code.google.com/p/silenus/
	Sileni Studios: http://www.silenistudios.com
*/



define(["imageLoader", "compose"], function (imageLoader, Compose) {
	
	// create the Renderer class
	var Renderer = Compose(function constructor(json, callback) {
		
		this.json = json;
		
		// configure the canvas element
		this.canvas = document.createElement('canvas');
		this.canvas.width = json.width;
		this.canvas.height = json.height;
		
		// the frame rate
		this.frameRate = json.frameRate;
		
		// pre-rendered shapes
		this.shapes = [];
		
		// these are pre-rendered versions of clipped/masked shapes and bitmaps
		// this array is filled as the masked versions are encountered during the first draw cycle
		this.maskedInstanceCache = [];
		
		// go over all instances, and load them
		for (var i = 0; i < json.instances.length; ++i) {
			var instance = json.instances[i];
			
			// this is a bitmap type - load the image
			if (instance.type == "bitmap") {
				imageLoader.loadImage(instance.path);
			}
			if (instance.type == "shape") {
				this.renderShape(instance, i);
			}
		}
		
		// perform a callback when we're done
		var that = this;
		imageLoader.addCallback({
			onDone: function() {
				callback.onDone(that);
			}
		});
	},{
		getAnimationLength: function() {
			return this.json.frames.length;
		},
		
		draw: function(frame) {

			// get the context
			var ctx = this.canvas.getContext('2d');
			
			// clean the frame
			ctx.clearRect(0, 0, this.json.width, this.json.height);
			
			// load the appropriate frame
			var frame = this.json.frames[frame];
			
			// go over all instances
			for (var i = 0; i < frame.instances.length; ++i) {
				
				// get the instance from the frame
				var instance = frame.instances[i];
				
				// this is a mask - we only draw it when we draw masked images
				if (instance.mask == true) {
					// skip
					continue;
				}
				
				// get the base instance that this instance refers to
				var baseInstance = this.json.instances[instance.instanceIndex];
				
				// save the current state - in the case of clipping, this will also reset the clipping region
				ctx.save();
				
				// this is a masked image - draw the masks first
				if (instance.masked == true) {
					this.drawMasks(ctx, frame, instance.masks);
				}
				
				// move to the right position for the main instance
				ctx.translate(instance.translate[0], instance.translate[1]);
				ctx.scale(instance.scale[0], instance.scale[1]);
				ctx.rotate(instance.rotation);
				
				// this is a bitmap instance - draw the image
				var type = baseInstance.type;
				
				
				// now draw the actual stuff
				if (type == "bitmap") {
					var img = imageLoader.getImage(baseInstance.path);
					ctx.drawImage(img, 0, 0);
				}
				else if (type == "shape") {
					
					// because we pre-render to a separate canvas for speed, we now have to offset by the bounding box coordinates
					ctx.translate(this.json.instances[instance.instanceIndex].bb.minX, this.json.instances[instance.instanceIndex].bb.minY);
					ctx.drawImage(this.shapes[instance.instanceIndex], 0, 0);
				}
				
				
				ctx.restore();
			}
		},
		
		
		drawMasks: function(ctx, frame, maskIndices) {
			
			// go over all masks
			for (var i = 0; i < maskIndices.length; ++i) {
				
				// get the mask from the frame
				var instance = frame.instances[maskIndices[i]];
				
				// get the base instance that this instance refers to
				var baseInstance = this.json.instances[instance.instanceIndex];
				
				// go to the right position - we cannot use save or restore, because this will also destroy our clipping region
				ctx.translate(instance.translate[0], instance.translate[1]);
				ctx.scale(instance.scale[0], instance.scale[1]);
				ctx.rotate(instance.rotation);
				
				// this is a bitmap instance - draw the image
				var type = baseInstance.type;
				
				// bitmaps clip their entire region, so we draw the contours
				if (type == "bitmap") {
					var img = imageLoader.getImage(baseInstance.path);
					ctx.beginPath();
					ctx.moveTo(0, 0);
					ctx.lineTo(img.width, 0);
					ctx.lineTo(img.width, img.height);
					ctx.lineTo(0, img.height);
					ctx.lineTo(0, 0);
					ctx.clip();
				}
				
				// shapes, we have to draw in real-time, as we cannot use the pre-drawn canvases, as these do not "remember" the clipping region
				// TODO is it possible somehow to copy the clip data from one canvas to another?
				else if (type == "shape") {
					for (var i = 0; i < baseInstance.fillPaths.length; ++i) {
						var path = baseInstance.fillPaths[i];
						var fillStyle = baseInstance.fillStyles[path.index];
						this.configureFillStyle(ctx, fillStyle);
						this.renderPath(ctx, path);
						ctx.clip();
					}
				}
				
				// move back
				ctx.rotate(-instance.rotation);
				ctx.scale(1/instance.scale[0], 1/instance.scale[1]);
				ctx.translate(-instance.translate[0], -instance.translate[1]);
			}
			
		},
		
		getCanvas: function() {
			return this.canvas;
		},
		
		getFrameRate: function() {
			return this.frameRate;
		},
		
		renderShape: function(instance, instanceIndex) {
			
			// bounding box
			var bb = this.computeBoundingBox(instance);
			instance.bb = bb;

			// create a new canvas element this size
			var canvas = document.createElement('canvas');
			this.shapes[instanceIndex] = canvas;
			var ctx = canvas.getContext('2d');
			canvas.width = bb.maxX - bb.minX;
			canvas.height = bb.maxY - bb.minY;
			
			// translate towards the minimum position so we draw square in the middle of the shape
			ctx.translate(-bb.minX, -bb.minY);
			
			// go over all paths, and render them - fills first
			for (var i = 0; i < instance.fillPaths.length; ++i) {
				var path = instance.fillPaths[i];
				var fillStyle = instance.fillStyles[path.index];
				this.configureFillStyle(ctx, fillStyle);
				this.renderPath(ctx, path);
				ctx.fill();
			}
			for (var i = 0; i < instance.strokePaths.length; ++i) {
				var path = instance.strokePaths[i];
				var strokeStyle = instance.strokeStyles[path.index];
				this.configureStrokeStyle(ctx, strokeStyle);
				this.renderPath(ctx, path);
				ctx.stroke();
			}
			
			/*var wrapper = document.getElementById('wrapper');
			wrapper.appendChild(canvas);*/
		},
		
		computeBoundingBox: function(instance) {
			
			// the bounding box
			var bb = {
				minX: Number.MAX_VALUE,
				minY: Number.MAX_VALUE,
				maxX: -Number.MAX_VALUE,
				maxY: -Number.MAX_VALUE
			}
			
			// go over all stroke paths and fill paths, and compute the min/max of all points in the instruction set
			for (var i = 0; i < instance.strokePaths.length; ++i) {
				var path = instance.strokePaths[i];
				for (var j = 0; j < path.points.length; ++j) {
					var instruction = path.points[j];
					if (instruction.p[0] < bb.minX) bb.minX = instruction.p[0];
					if (instruction.p[1] < bb.minY) bb.minY = instruction.p[1];
					if (instruction.p[0] > bb.maxX) bb.maxX = instruction.p[0];
					if (instruction.p[1] > bb.maxY) bb.maxY = instruction.p[1];
				}
			}
			for (var i = 0; i < instance.fillPaths.length; ++i) {
				var path = instance.fillPaths[i];
				for (var j = 0; j < path.points.length; ++j) {
					var instruction = path.points[j];
					if (instruction.p[0] < bb.minX) bb.minX = instruction.p[0];
					if (instruction.p[1] < bb.minY) bb.minY = instruction.p[1];
					if (instruction.p[0] > bb.maxX) bb.maxX = instruction.p[0];
					if (instruction.p[1] > bb.maxY) bb.maxY = instruction.p[1];
				}
			}
			
			// create a safety border
			bb.minX -= 20;
			bb.minY -= 20;
			bb.maxX += 20;
			bb.maxY += 20;
			
			// return the box
			return bb;
		},
		
		renderPath: function(ctx, path) {
			
			// begin a new path
			ctx.beginPath();
			
			for (var i = 0; i < path.points.length; ++i) {
				var instruction = path.points[i];
				var type = instruction.type;
				var p = instruction.p;
				
				// move to command
				if (type == "moveTo") {
					ctx.moveTo(p[0], p[1]);
				}
				
				// line to command
				else if (type == "lineTo") {
					ctx.lineTo(p[0], p[1]);
				}
				
				// quad curve
				else if (type == "quadraticCurveTo") {
					ctx.quadraticCurveTo(instruction.control[0], instruction.control[1], p[0], p[1]);
				}
			}
			
			// done
			//ctx.closePath();
		},
		
		configureFillStyle: function(ctx, fillStyle) {
			var type = fillStyle.type;
			if (type == "solidColor") {
				ctx.fillStyle = this.getColorCode(fillStyle);
			}
			else if (type == "linearGradient") {
				var grad = ctx.createLinearGradient(fillStyle.start[0], fillStyle.start[1], fillStyle.stop[0], fillStyle.stop[1]);
				for (var i = 0; i < fillStyle.colorStops.length; ++i) {
					var stop = fillStyle.colorStops[i];
					grad.addColorStop(stop.ratio, this.getColorCode(stop));
				}
				ctx.fillStyle = grad;
			}
		},
		
		configureStrokeStyle: function(ctx, strokeStyle) {
			// TODO solidStyles (dashed, ...) are not supported by canvas - we should implement them ourselves if we want support
			ctx.lineWidth = strokeStyle.weight;
			if (strokeStyle.caps == "none") ctx.lineCap = "butt"; // called "none" in flash
			else ctx.lineCap = strokeStyle.caps;
			ctx.lineJoin = strokeStyle.joints;
			ctx.strokeStyle = this.getColorCode(strokeStyle);
		},
		
		getColorCode: function(obj) {
			return "rgba(" + obj.red + "," + obj.green + "," + obj.blue + "," + obj.alpha + ")";
		}
		
	});
	
	return {
		Renderer: Renderer
	}
	
});
