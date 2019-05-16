/*扫码领福包*/

 
   
  function saoma_fubao(){
	var a= {code:1000,data:{closePage:'views/saomalinfubao.html',openPage:'views/fubao.html',isRefresh:'0',params:''}};
                      controlPage(a);

		};

		
		$('.home').click(function(){
			var a= {code:1000,data:{closePage:'views/saomalinfubao.html',openPage:'back',isRefresh:'0',params:''}};
                    controlPage(a);

		});

	 function dhcp(){
		var a= {code:1000,data:{closePage:'views/saomalinfubao.html',openPage:'exchangeLottery',isRefresh:'0',params:''}};
                      controlPage(a);

		};
		function dhcp(){
			 location.href="http://a.app.qq.com/o/simple.jsp?pkgname=com.bkgc.bvapp";
			
		};
	


		
			var groupId =''	;	
        	var expired = '';
            var userId = '';
   
            var json = {};
            var arr = location.search.substr(1).split('&');
            arr.forEach(function(item) {
                var tmp = item.split('=');
                json[tmp[0]] = tmp[1];
            });
            
            blessURL = json.blessURL;
            groupId=json.GroupId;
            expired = json.expired;

			function upload(){
						$.ajax({
							url:blessURL+"/blessenvelope/getBlessEnvelope",
						data:{groupId:groupId,userId:userId,expired:expired},

							success:function(data){
								var data=data;
									if(data.data.type==30){
//										var bless1=document.getElementById("bless");
//										bless1.hidden = true;
//										var blesssuccess1=document.getElementById("blesssuccess");
//										blesssuccess1.hidden = false;
//										var delName = decodeURIComponent(data.data.beName);
//										$("#money").text(data.data.amount);
//										$("#name").text(delName);
//										alert(blessURL+"/views/getBlessSuccessByWechat.html?amount="+data.data.amount+"&name="+delName);
//										alert(data.data.amount);
										window.location=blessURL+'/views/getBlessSuccessByWechat.html?v='+Math.random()+'&amount='+data.data.amount+"&name="+data.data.beName;
										
									}
									else{
//										var bless1=document.getElementById("bless");
//										bless1.hidden = true;
//										var blesssuccess1=document.getElementById("blesssuccess");
//										blesssuccess1.hidden = false;	
//										var pText=document.getElementById("description");
//										 pText.style.fontSize='30px';
//										 pText.style.fontWeight="bold";
//										 pText.style.marginLeft="25%";
//									/* 	 pText.style.backgroundColor='green'; */
//										var des = decodeURIComponent(json.description);
//										$("#description").text(des);
//										var title2=document.getElementById("title2");
//										title2.hidden = true;
//										alert(blessURL+"/views/getBlessSuccessByWechat.html?description="+data.data.description);
										window.location.href=blessURL+'/views/getBlessSuccessByWechat.html?v='+Math.random()+'&description='+data.data.description;
									}
			
							}
						})
             
		}
			function atOnce(){
						if($('.Phone').val()==""){
							tips('请输入手机号');
		         			$('.sendCode').attr("disabled",false);
		         			$('.sendCode').style.color = "red";
							return false;
						}
						var reg = /\d{6}/;
						if($('.smsCode').val()=="" && reg.test($('.smsCode').val())){
							tips('请输入验证码');
		         			$('.sendCode').attr("disabled",false);
							return false;
						}
						
	         			$('#CbtnJY').attr("disabled",true);	
						$.ajax({
							url:blessURL+"/auth/checkSmsCodeGetUser",
							data:{phone:$('.Phone').val(),smgCode:$('.smsCode').val()},
							success:function(data){
								var data=data;
								if(data.code == 1000){
									userId=data.data.UserId;
									upload();
								}else{
									tips(data.msg);
				         			//$('#CbtnJY').attr("disabled",false);			
								}
							}
						})

					
			}
