$.cookie('codePhone',"");//每次进入清除存储的发短信的手机号
$(function(){

	//var access_token = access_token;//获取tokend

//alert(access_token);
	/*注册*/
	$('.registerBtn').click(function(){
		$.ajax({
			url:url+"/bvi/auth/register",
			data:{AccessToken:access_token,Phone:$('.Phone').val(),Password:$('.passWord').val(),SmsCode:$('.smsCode').val(),NickName:"大橙子"},
			beforeSend:function(){
				if($('.Phone').val()==""){
					tips('请输入手机号!','');
					return false;
				}
				var patrn = /^1[3|4|5|7|8]\d{9}$/;
				if(!patrn.test($('.Phone').val())){
					tips('请输入正确的手机号!','');
					return false;
				}
				if($.cookie('codePhone')!=""&&$('.Phone').val()!=$.cookie('codePhone')){
					tips('请输入正确手机号！','');
					return false;
				}
				if($('.smsCode').val()==""){
					tips('请输入短信验证码！','');
					return false;
				}
				var reg = /^[a-zA-Z\d]{6,100}$/;
				if(!reg.test($('.passWord').val())){
					tips('密码为六位以上数字或字母！','');
					return false;
				}
			},
			success: function(data){console.log(data);
				//var data=eval("("+data+")");//转换为json对象
				var data=data;

				if(data.code==1){
					
					$.cookie("userid",data.data.UserId);
					document.location.href="myPage.html";
					/*$.ajax({
						url:url+"/bvi/auth/register",
						data:{UserName:$('.Phone').val(),Password:$('.passWord').val()},
						
						success: function(data){console.log(data);
							
						}
					});*/
				}else{
					tips(data.error);
				}
			}
		})
	})

	/*登录*/
	$('.loginBtn').click(function(){
		$.ajax({
			url:url+"/bvi/auth/login",
			data:{AccessToken:access_token,UserName:$('.Phone').val(),Password:$('.passWord').val()},
			beforeSend:function(){
				if($('.Phone').val()==""){
					tips('请输入手机号!','');
					return false;
				}
				var patrn = /^1[3|4|5|7|8]\d{9}$/;
				if(!patrn.test($('.Phone').val())){
					tips('请输入正确的手机号!','');
					return false;
				}
				var reg = /^[a-zA-Z\d]{6,100}$/;
				if(!reg.test($('.passWord').val())){
					tips('密码为六位以上数字或字母！','');
					return false;
				}
			},
			success: function(data){console.log(data);
				//var data=eval("("+data+")");//转换为json对象
				var data=data;

				if(data.code==1){
					
					$.cookie("userid",data.data.UserId);
					document.location.href="myPage.html";
					/*$.ajax({
						url:url+"/bvi/auth/register",
						data:{UserName:$('.Phone').val(),Password:$('.passWord').val()},
						
						success: function(data){console.log(data);
							
						}
					});*/
				}else{
					tips("用户名或密码错误！");
				}
			}
		})
	})

})
