
cookies();

var RNickName;
var REmailVal;
var RGender;
	/*�û���ϸ��Ϣ*/
$(function(){

	$.ajax({
		url:url+"/bvi/auth/getUserInfo",
		data:{AccessToken:access_token,UserId:userid},
		beforeSend:function(){
			 img = $("<img class='fixImg' src='../images/loading.gif'/>");
        		$("body").append(img);

		},
		success: function(data){
			//var data=eval("("+data+")");
			var data=data;
			if(data.code==1){
				var fixImg = document.getElementsByClassName("fixImg")[0];
                document.body.removeChild(fixImg);
				if (data.data.FacePhotoPath!=''&&data.data.FacePhotoPath!=null) {
				$(".FacePhotoPath").attr('src',data.data.FacePhotoPath);
				}
				if (data.data.Gender=='女') {
					$(".Gender").attr('src','../images/wode/nvxing@2x.png');
				}else if (data.data.Gender=='男') {
					$(".Gender").attr('src','../images/wode/nansheng@2x.png');
				}else{
					$(".Gender").attr('src','../images/wode/baomi@2x.png');
				}
				RNickName=data.data.NickName;
				REmailVal=data.data.Email;
				RGender=data.data.Gender;
				$("#tphoto").attr("src",data.data.FacePhotoPath)
				$('.CName').val(data.data.Name);
				$('.CNickName').val(data.data.NickName);
				$('.CEmail').val(data.data.Email);
				$('.CNickName').html(data.data.NickName);
				$('.CFacePhotoPath').html(data.data.FacePhotoPath);
				$('.Phone').val(data.data.Phone);
				$('.phone').val(data.data.Phone);
				$('.CPhone').html(data.data.Phone);
				$('.CEmail').html(data.data.Email);
				$('.CGender').html(data.data.Gender);
				$('.CIdCardNumber').val(data.data.IdCardNumber);
			}else{
				tips("数据读取失败，请稍后再试！");
			}
		}
	});
	
	/*我的页面*/
	/*个人信息*/
		$('.my_message').click(function(){
			var a= {code:1,data:{closePage:'views/myPage_mymessage.html',isRefresh:'1',params:'',openPage:'back'}};
                    controlPage(a);

		});

		$('.myPage_mymessage').click(function(){
			var a= {code:1,data:{closePage:'views/myPage.html',isRefresh:'0',params:'',openPage:'views/myPage_mymessage.html'}};
                    controlPage(a);

		});
		/*退出按钮*/
		$('.tui_btn').click(function(){
		mui.confirm('确定要退出登录吗？', '提示', ['是','否'], function(e) {
			if(e.index==1){

			}else{
				  $.cookie("userid",null);
				exit();
			}
		});
		})
		/*实名认证*/
		$('.shimingrenzheng').click(function(){
			var a= {code:1,data:{closePage:'views/myPage.html',isRefresh:'0',params:'',openPage:'views/myPage_shimingrenzheng.html'}};
                    controlPage(a);

		});
		$('.shiming_mypage').click(function(){
			var a= {code:1,data:{closePage:'views/myPage_shimingrenzheng.html',isRefresh:'0',params:'',openPage:'back'}};
                    controlPage(a);

		});

		/*常用设置*/
		$('.myPage_changyongshezhi').click(function(){
			var a= {code:1,data:{closePage:'views/myPage.html',isRefresh:'0',params:'',openPage:'views/myPage_changyongshezhi.html'}};
                    controlPage(a);

		});
		$('.changyong_mypage').click(function(){
			var a= {code:1,data:{closePage:'views/myPage_changyongshezhi.html',isRefresh:'0',params:'',openPage:'back'}};
                    controlPage(a);

		});

		/*帮助说明*/
		$('.myPage_bangzhushuoming').click(function(){
			var a= {code:1,data:{closePage:'views/myPage.html',isRefresh:'0',params:'',openPage:'views/myPage_bangzhushuoming.html'}};
                    controlPage(a);

		});
		$('.bangzhu_mypage').click(function(){
			var a= {code:1,data:{closePage:'views/myPage_bangzhushuoming.html',isRefresh:'0',params:'',openPage:'back'}};
                    controlPage(a);

		});

		/*用户反馈*/
		$('.myPage_yonghufankui').click(function(){
			var a= {code:1,data:{closePage:'views/myPage.html',isRefresh:'0',params:'',openPage:'views/myPage_yonghufankui.html'}};
                    controlPage(a);

		});
		$('.yonghu_mypage').click(function(){
			var a= {code:1,data:{closePage:'views/myPage_yonghufankui.html',isRefresh:'0',params:'',openPage:'back'}};
                    controlPage(a);

		});

		/*关于我们*/
		$('.myPage_guanyuwomen').click(function(){
			var a= {code:1,data:{closePage:'views/myPage.html',isRefresh:'0',params:'',openPage:'views/myPage_guanyuwomen.html'}};
                    controlPage(a);

		});
		$('.guanyu_mypage').click(function(){
			var a= {code:1,data:{closePage:'views/myPage_guanyuwomen.html',isRefresh:'0',params:'',openPage:'back'}};
                    controlPage(a);

		});

})


