define(function () {
	
	// the registered callbacks
	var callbacks = [];
	
	// number of pending images
	var nPendingImages = 0;
	
	// images
	var images = [];
	
	// load an image asynchronously and call a callback when done
	function loadImage(fileName) {
		++nPendingImages;
		require(['image!' + "upload/" + fileName], function(img) {
			
			// add the image
			--nPendingImages;
			images[fileName] = img;
			
			// we're done loading, let all callbacks know
			if (nPendingImages == 0) {
				for (var i = 0; i < callbacks.length; ++i) {
					callbacks[i].onDone();
				}
				callbacks = [];
			}
		});
	}
	
	
	
	// get an image
	function getImage(fileName) {
		return images[fileName];
	}
	
	
	function addCallback(callback) {
		
		// no pending images - just call the callback straight away
		if (nPendingImages == 0) callback.onDone();
		else callbacks.push(callback);
	}
	
	
    return {
        loadImage: loadImage,
        getImage: getImage,
        addCallback: addCallback
    }
});