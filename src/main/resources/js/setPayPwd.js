cookies();

$(function(){

	$('#CbtnJY').click(function(){
		/*设置支付密码*/
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
		var reg = /^\d{6}$/;
		if(!reg.test($('.Cpassword').val())){
			tips('密码为六位数字！','');
			return false;
		}
		
		mui.confirm('你确定要设置支付密码吗？', '提示', ['是','否'], function(e) {
			if(e.index==1){

			}else{
				$.ajax({
					url:url+"/bvi/auth/resetPayPassword",
					data:{AccessToken:access_token,Phone:$('.Phone').val(),PayPassword:$('.Cpassword').val(),SmsCode:$('.CSmsCode').val()},
					success: function(data){
						var data=data;
						if(data.code==1){
							mui.toast('支付密码设置成功！');
							  var a= {code:1,data:{closePage:'views/shezhimima.html',params:'',isRefresh:'0',openPage:'back'}};
                   			 controlPage(a);

						}else{
							tips("支付密码设置失败！");
						}
					}
				});
			}
		});
	});


	$('.shezhi_back').click(function(){
            var a= {code:1,data:{closePage:'views/shezhimima.html',params:'',isRefresh:'0',openPage:'back'}};
                    controlPage(a);

        });
});