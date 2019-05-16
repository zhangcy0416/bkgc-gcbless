$(function(){



	$("#yj_btn").click(function(){

			var jy_content = $("#jy_content").val();
			var jy_phone = $("#jy_phone").val();

			$.ajax({
				url:url+"/bvi/feedback/insertFeedback",
				data:{
					UserId:userid,
					AccessToken:access_token,
					description:jy_content,
					Phone:jy_phone
				},
				
				success:function(data){
					if(data.code == 1){
						var a= {code:1,data:{closePage:'views/myPage_yonghufankui.html',params:'',isRefresh:'0',openPage:'back'}};
                    		
						mui.alert(data.data.result,function(){
							controlPage(a);
						});
					}else{
						mui.alert(data.error);
					}
				}
			})

	})

})