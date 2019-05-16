(function(window) {
	var Utils = {
		getQueryString: function(name) {
			var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
			var r = window.location.search.substr(1).match(reg);
			if(r != null) return unescape(r[2]);
			return null;
		},
		isArray: function(o) {
			return Object.prototype.toString.call(o) == '[object Array]';
		}
	};

	Utils.isMobile = {
		Android: function() {
			return navigator.userAgent.match(/Android/i) ? true : false;
		},
		BlackBerry: function() {
			return navigator.userAgent.match(/BlackBerry/i) ? true : false;
		},
		iOS: function() {
			return navigator.userAgent.match(/iPhone|iPad|iPod/i) ? true : false;
		},
		Windows: function() {
			return navigator.userAgent.match(/IEMobile/i) ? true : false;
		},
		any: function() {
			return(Utils.isMobile.Android() || Utils.isMobile.BlackBerry() || Utils.isMobile.iOS() || Utils.isMobile.Windows());
		}
	};
	
  Utils.customToast  = function(msg, duration){  
  duration = isNaN(duration) ? 3000 : duration;
  var m = document.createElement('div');
  m.innerHTML = msg;
  m.style.cssText = "background: rgb(0, 0, 0);\
                     opacity: 0.8;\
                     height: 0.6rem;\
                     color: rgb(255, 255, 255);\
                     text-align: center;\
                     border-radius: 5px;\
                     position: fixed;\
                     bottom: 10%;\
                     z-index: 999999;\
                     font-weight: 500;\
                     display: inline;\
                     padding: 10px;\
                     font-family: 'Lucida Grande', 'Helvetica', sans-serif;  \
                     font-size: 0.4rem;";
  document.body.appendChild(m);
  m.style.left = ((document.body.clientWidth - m.offsetWidth) / 2) + 'px';
  setTimeout(function() {
    var d = 0.5;
    m.style.webkitTransition = '-webkit-transform ' + d + 's ease-in, opacity ' + d + 's ease-in';
    m.style.opacity = '0';
    setTimeout(function() { document.body.removeChild(m) }, d * 1000);
  }, duration);
}
    
	window.Utils = Utils;
})(window);