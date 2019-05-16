$(function(){
	$("#tip_name").on("click",function(){
			$(".mark").css("display","block");
			$(".upW_name").css("display","block");

			$(function(){
				if($("#nickname").val()==""||$("#nickname").val()==null){
					$("#HideNick").hide();
				}
				if(RNickName!=null){
					$('#nickname').val(RNickName);
					$("#HideNick").show();
				}
			})

			$("#HideNick").on("click",function(){
				$("#HideNick").hide();
				$('#nickname').val("");
			})

			$("#close_name").on("click",function(){
				$(".mark").css("display","none");
				$(".upW_name").css("display","none");
			})
			$("#ensure_name").on("click",function(){
				NickName();
				$(".mark").css("display","none");
				$(".upW_name").css("display","none");

			})

	})

	$("#tip_email").on("click",function(){
			$(".mark").css("display","block");
			$(".upW_email").css("display","block");

			$(function(){
				if($("#newemail").val()==""||$("#newemail").val()==null){
					$("#HideEmail").hide();
				}
				if(REmailVal!=null){
					$('#newemail').val(REmailVal);
					$("#HideEmail").show();
				}
			})

			$("#HideEmail").on("click",function(){
				$("#HideEmail").hide();
				$('#newemail').val("");
			})

			$("#close_email").on("click",function(){
				$(".mark").css("display","none");
				$(".upW_email").css("display","none");
			})
			$("#ensure_email").on("click",function(){
				UpdateEmail();
				$(".mark").css("display","none");
				$(".upW_email").css("display","none");

			})

	})
	$("#tip_sex").on("click",function(){
		$(".upU").css("display","block");
		$(".mark").css("display","block");


	})
	$("#close_sex").on("click",function(){
			$(".upU").css("display","none");
			$(".mark").css("display","none");

	})
	$(".mark").on("click",function(){
		$(".upU").css("display","none");
		$(".mark").css("display","none");
		$(".upW_email").css("display","none");
		$(".upW_name").css("display","none");
	})


})


$(function(){
	var $inputwrapper = $(' .upW_name .nick_entry');
	$inputwrapper.find('input').on('input propertychange',function(){
		var result = $(this).val();
		if(result==""){
			$("#HideNick").hide();
		}else{
			$("#HideNick").show();
		}

	});
})

$(function(){
	var $inputwrapper = $(' .upW_email .nick_entry');
	$inputwrapper.find('input').on('input propertychange',function(){
		var result = $(this).val();
		if(result==""){
			$("#HideEmail").hide();
		}else{
			$("#HideEmail").show();
		}

	});
})