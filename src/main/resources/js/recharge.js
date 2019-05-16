$(function(){

	 $('.recharge_fubao').click(function(){
            var a= {code:1,data:{closePage:'views/fubao_recharge.html',openPage:'views/fubao.html',isRefresh:'1',params:''}};
                    controlPage(a);

        });

	 $.ajax({
		url:url+"/bvi/amount/getAmountInfo",
		data:{AccessToken:access_token,UserId:userid},
		success:function(data){
				$('#balance').html((data.data.BlessAmount).toFixed(2));
		}

		})

	$(".mp").on("click",function(){
		$("#money_count").val($(this).val()+".00")
	})

	var payWay="WeChat";


	$(".way a").click(function(){
		$(this).append($(".ift")).find("a").removeChild($(".ift"))
	})
	$(".way>div").click(function(){
		payWay=$(this).attr("title")
		/*alert(payWay)*/
	})


	$("#chong_btn").click(function(){
		
		var money_count = $("#money_count").val();
		if(money_count==""){
			mui.alert("请输入充值金额");
			return false;
		}
		
		var c = {code:1,data:{amount:money_count,subject:"福包充值",type:payWay}};

		recharge(c)
		$('#chong_btn').css("background","#c1c1c1");
		$("#chong_btn").attr("disabeld",true)
		
	})
})