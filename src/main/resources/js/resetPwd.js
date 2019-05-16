
$(function(){
	 
	$('#Cbtn').click(function(){
		/*修改登录密码*/
		if($('.Phone').val()==""){
			tips('请输入手机号!','');
			return false;
		}
		var patrn = /^1[3|4|5|7|8]\d{9}$/;
		if(!patrn.test($('.Phone').val())){
			tips('请输入正确的手机号!','');
			return false;
		}
		if($('.CSmsCode').val()==""){
			tips('请输入短信验证码！','');
			return false;
		}
		var reg = /^[a-zA-Z\d]{6,100}$/;
			if(!reg.test($('.Cpassword').val())){
			tips('密码为六位数字或字母！','');
			return false;
		}
		mui.confirm('你确定要修改登录密码吗？', '提示', ['是','否'], function(e) {
			if(e.index==1){

			}else{
				$.ajax({
					url:url+"/bvi/auth/resetPassword",
					data:{AccessToken:access_token,Phone:$('.Phone').val(),Password:$('.Cpassword').val(),SmsCode:$('.CSmsCode').val()},
					success: function(data){
						//var data=eval("("+data+")");
						var data=data;
						if(data.code==1){
							mui.toast('登录密码修改成功！');
							 $.cookie("userid",null);
							exit();
							setTimeout("window.location.href='login.html'",500);
							//document.location.href="login.html";
						}else{
							tips("登录密码修改失败");
						}
					}
				});
			}
	});
});
});

