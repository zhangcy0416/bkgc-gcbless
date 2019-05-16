$(function(){
	
		$.ajax({
			url:url+"/bvi/site/getPlayContentList",
			data:{AccessToken:access_token},
			success: function(data){
		//var data=eval("("+data+")");//转换为json对象
		var data=data;
		
		str="";
		$.each(data.data.List, function(index,el) {
			str+='<div class="swiper-slide">';
			str+= '<img src="'+el.ImageUrl+'" title="'+el.Title+'"  alt=""/>';
			str+='</div>';
		});
			$('.swiper-wrapper').append(str);
	}
			});
		})
