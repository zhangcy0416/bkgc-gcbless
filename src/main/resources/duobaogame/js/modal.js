+ function($, window) {
	var modal = {};
	/**
	 * 显示Toast，默认显示时长为3s
	 * @param {Object} msg
	 */
	modal.showToast = function(msg){
		var html = '<div class="toast-container">';
		html+='<span class="toast" id="toast">'+msg+'</span></div>';
		$(html).appendTo($('body'));
		setTimeout(function(){
			dismissToast();
		},5*1000);
	}
	
	function dismissToast(){
		var container = $('.toast-container');
		if(container){
			container.remove();
		}
	}
	window.Modal = modal;
}(jQuery, window);