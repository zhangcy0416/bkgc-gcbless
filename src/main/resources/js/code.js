//发送短信验证码



	$('.sendCode').on("click",function(){
    $('.sendCode').attr("disabled",true);
	if($(this).attr("rel")==0){

		$.ajax({
			url:blessURL+"/message/sendSMS",
			data:{phone:$('.Phone').val()},
			beforeSend:function(){
				if($('.Phone').val()==""){
					tips('请输入手机号');
         			$('.sendCode').attr("disabled",false);
					return false;
				}
				var patrn = /^1[3|4|5|7|8]\d{9}$/;
				if(!patrn.test($('.Phone').val())){
					tips('请输入正确的手机号');
         			$('.uu').attr("disabled",false);
					return false;
				}
			},
			success: function(data){
				if(data.indexOf("1000")>-1){
					send($('.Phone').val(),$('.sendCode'));
				}else{
					tips("短信验证码发送失败！");
					$('.sendCode').attr("disabled",false);
				}
			}
		})
	}
})


/**
  * [send description]短信验证码倒计时
  * @param  {[type]} moblie [手机号码]
  * @param  {[type]} obj     [发送验证码按钮]
  * @return {[type]}         [description]
*/
function send(mobile,obj){
     var time = 60;
     var obj = $(obj);
     var rel = obj.attr("rel");
    
     if(rel==0){
         var clr = setInterval(function(){
              time--;
              if(time>0){
                //禁用按钮
              	//obj.val(time+"秒后重新发送").attr("rel",1);
                  obj.html(time+'后重新发送').removeClass('sendCode').attr("rel",1);
              }else{
              		
                  obj.html("重新发送").addClass('sendCode').attr("rel",0);
                  $('.sendCode').attr("disabled",false);
                  clearInterval(clr);
              }
          }, 1000);
     }
}