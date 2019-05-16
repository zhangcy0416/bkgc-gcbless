cookies();

/**/
/*福包信息*/
$(function(){
//返回事件
 $('.cash_fubao').click(function(){
            var a= {code:1, data:{closePage:'views/fubao_cash.html',openPage:'views/fubao.html',params:'',isRefresh:'1'}};
                    controlPage(a);
        });

$.ajax({
	url:url+"/bvi/amount/getAmountInfo",
	 data:{AccessToken:access_token,UserId:userid},

	success: function(data ){//console.log( );
		//var  =eval("("+ +")");//转换为json对象
        var  data=data ;
		if( data.code==1){
			$('.AccountBalance').html((data.data.AccountBalance).toFixed(2));
			$('.BlessAmount').html(( data.data.BlessAmount).toFixed(2));
			
		}else{
			mui.alert("数据读取失败，请稍后再试！");
		}
	}
});

$('.allMoney').click(function(){
	//alert($('.AccountBalance').text());

	$('#accountMoney').val($('.AccountBalance').text());		
});

	$('.chong_btn').click(function(){
	var WithdrawAmount='';//提现金额
	var WithdrawType='';//提现方式
	var TargetAccount='';//提现账户
	var UserName=''//提现姓名
				if (parseFloat($('#accountMoney').val())<1) {
					mui.alert('提现金额必须大于等于1!','');
					return false;
				
				}
				if(isNaN(parseFloat($('#accountMoney').val()))){
					mui.alert('请输入正确提现金额!','');
					return false;
				}
				if(parseFloat($('#accountMoney').val())>parseFloat($('.AccountBalance').text())){
					mui.alert('提现金额不得大于可提现余额!','');
					return false;
				}
				var accountMoney=parseFloat($('#accountMoney').val());
			//	alert(accountMoney);
				 WithdrawAmount=accountMoney.toFixed(2);
				
				if($('.weixinzhanghao').val()==''){//如果微信号为空 去判断支付宝
					if($('.zhifubao').val()==''){//全都为空
					mui.alert('请输入正确的提现账号!','');
					return false;
					}else{//支付宝存在
							if($('.zrealName').val()==''){//判断姓名
								mui.alert('请输入真实姓名！','');
								return false;
						}else{
							 /*WithdrawType=2;*/
							 TargetAccount=$('.zhifubao').val() ;
							 UserName=$('.zrealName').val();
						}
					}
				}else{//微信号存在
					if($('.wrealName').val()==''){//判断姓名
						mui.alert('请输入真实姓名！','');
						return false;
					}else{
							/* WithdrawType=1;*/
							 TargetAccount=$('.weixinzhanghao').val() ;
							 UserName=$('.wrealName').val();
					}
				}
				$('.chong_btn').css("background","#c1c1c1");
				$('.chong_btn').attr("disabled",true);
				$.ajax({
					url:url+"/bvi/amount/withdraw",
					data:{AccessToken:access_token,UserId:userid,WithdrawAmount:WithdrawAmount,WithdrawType:2,TargetAccount:TargetAccount,UserName:UserName},
					success: function( data){
						//var  =eval("("+ +")");//转换为json对象
						var  data=data ;
						if( data.code==1){
							$('.chong_btn').attr("disabled",false);
							var a= {code:1, data:{closePage:'views/fubao_cash.html',openPage:'views/fubao.html',params:'',isRefresh:'1'}};
							mui.alert( data.data.description+',24小时左右到账！',function(){
								controlPage(a);
							});
							
                    		

						}else{
							mui.alert('提现请求失败,请稍后重试');
						}
					}
				})
	//	
			
		
	
	})
});

