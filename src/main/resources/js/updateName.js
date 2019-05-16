cookies();

$(function(){	
	$('.confirm').click(function(){
		/*更新用户姓名和身份证信息*/
		if($('.CName').val()==""){
			tips('请输入真实姓名');
			return false;
		}
		var rug=/^[\u4e00-\u9fa5\s\.]+$/;
		if(!rug.test($('.CName').val())){
			tips('姓名只能为汉字！','');
			return false;
		}	
		
		if($('.CIdCardNumber').val()==""){
			tips('请输入身份证号');
			return false;
		}
		
		var reg = /^(^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$)|(^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[Xx])$)$/;
		if(!reg.test($('.CIdCardNumber').val())){
			tips('身份证格式不正确！','');
			return false;
		}	
		
		mui.confirm('你确定要修改实名信息吗？', '提示', ['是','否'], function(e) {
			if(e.index==1){

			}else{
				
				$.ajax({
					url:url+"/bvi/auth/updateRealInfo",
					data:{AccessToken:access_token,UserId:userid,Name:$('.CName').val(),IdCardNumber:$('.CIdCardNumber').val()},
					success: function(data){
						var data=data;
						if(data.code==1){
							mui.toast('实名信息修改成功！');
							//document.location.href="myPage.html";
						}else{
							tips("实名信息修改失败");
						}
					}
				});
			}
		});
	});	
});


