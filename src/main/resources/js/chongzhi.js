cookies();
/*兑奖记录*/
$(function(){
        $('.chongji_fubao').click(function(){
            var a= {code:1,data:{closePage:'views/fubao_chongzhijilu.html',params:'',isRefresh:'0',openPage:'views/fubao.html'}};
                    controlPage(a);

        });
    });
load();
//upload(0);		


            	var num=0;
			function upload(num){
				$.ajax({
						url:url+"/bvi/amount/depositRecord",
						data:{AccessToken:access_token,UserId:userid,PageIndex:num,PageSize:5},
						success: function(data){//console.log(data);
						//var data=eval("("+data+")");//转换为json对象
                        var data=data;
						if(data.code==1){
						var str="";
						$.each(data.data.List, function(index,el) {

							str+='<div class="cz">';
    							str+='<div class="cz_l">';
        							str+=' <h5>充值成功 <span>'+el.Amount+'元</span></h5>';
        							str+='<p>'+el.CreateTime+'<span>'+el.InWay+'</span></p>';
    							str+='</div>';
							str+='</div>';
						});
							$('#list').append(str);
					
						}else{

						tips("数据读取失败，请稍后再试！");
					}
				}	});
			}



/*刷新  加载*/
    function  load(){
    mui.init({
                pullRefresh: {
                    container: '#pullrefresh',
                    down: {

                        callback: pulldownRefresh
                    },
                    up: {
                        contentrefresh: '正在加载...',
                        callback: pullupRefresh
                    }
                }
            });
            /**
             * 下拉刷新具体业务实现
             */
            function pulldownRefresh() {
                setTimeout(function() {
                  $('#list').html("");
                  num=1;
                   upload(0);
                    mui('#pullrefresh').pullRefresh().endPulldownToRefresh(); //refresh completed
                }, 1500);
            }
           // var count = 0;
            /**
             * 上拉加载具体业务实现
             */
            function pullupRefresh() {
                setTimeout(function() {
                    mui('#pullrefresh').pullRefresh().endPullupToRefresh(); //参数为true代表没有更多数据了。
                    upload(num);
                    num++;
                   
                }, 1500);
            }
            if (mui.os.plus) {
                mui.plusReady(function() {
                    setTimeout(function() {
                        mui('#pullrefresh').pullRefresh().pullupLoading();
                    }, 1000);

                });
            } else {
                mui.ready(function() {
                    mui('#pullrefresh').pullRefresh().pullupLoading();
                });
            }


}