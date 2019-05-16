cookies();

$(function(){
	$('.confirm').click(function(){
		/*更新用户姓名和身份证信息*/
		
		$.ajax({
			url:url+"/bvi/auth/updateRealInfo",
			data:{AccessToken:access_token,UserId:userid,Name:$('.CName').val(),IdCardNumber:$('.CIdCardNumber').val()},
			beforeSend:function(){
				if($('.CName').val()==""){
					tips('请输入真实姓名');
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
				if($('.CName').val()!=""&&$('.CIdCardNumber').val()!=""&&$('.CIdCardNumber').val()!=""){
					tips123("确定要修改吗？" )
				}
				
			},
			success: function(data){
				//var data=eval("("+data+")");	
				var data=data;
				if(data.code==1){
					tips("修改成功");	
					document.location.href="myPage.html";
				}else{
					tips("修改失败");
				}
			}
		});
	});	
})


function tips123(val,url){
	if (url==''||typeof(url)=="undefined") {
		url="javascript:void(0)"
	}
	var str = "";
	str+="<div class='tips-bgc'></div>";
	str+="	<div class='publicTip'>";
	str+="		<div class='publicTip-cont'>"+val+"</div>";
	str+="		<div class='publicTip-close'>";
	str+="			<a href="+url+" class='publicTip-s'>确定</a>";
	str+="			<a href="+url+" class='publicTip-s'>取消</a>";
	str+="		</div>";
	str+="	</div>";
	$('body').append(str);
	$('.publicTip').css('margin-top','-'+$('.publicTip').outerHeight()/2+'px');
}
$(function(){//双按钮时区分点击确实或取消
	$(document).on('click','.publicTip-close a',function(){
		$('.tips-bgc,.publicTip').remove();
		return false;
		
	});
	$(document).on('click','.publicTip-close a',function(){
		$('.tips-bgc,.publicTip').remove();
		
	});
})