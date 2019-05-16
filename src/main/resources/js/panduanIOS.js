

//手机端判断各个平台浏览器及操作系统平台
function adnORios(){
  if(/MicroMessenger/i.test(navigator.userAgent)){
  $('#safeExit').hide();
  
  	if(/android/i.test(navigator.userAgent)){
      alert(1);
	  }
	  if(/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)){
	     alert(2);
	  }

  }
}