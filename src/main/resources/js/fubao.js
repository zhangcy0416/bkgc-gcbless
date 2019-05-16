cookies();
/**/
/*福包信息*/
var img;
$(function(){

$.ajax({
	url:url+"/bvi/amount/getAmountInfo",
	data:{AccessToken:access_token,UserId:userid},
    beforeSend:function(){
        img = $("<img class='fixImg' src='../images/loading.gif'/>");
        $("body").append(img);

    },
	success: function(data){//console.log(data);

		//var data=eval("("+data+")");//转换为json对象
        var data=data;
		if(data.code==1){
             var fixImg = document.getElementsByClassName("fixImg")[0];
                document.body.removeChild(fixImg);
			$('.AccountBalance').html(data.data.AccountBalance.toFixed(2));
			$('.BlessAmount').html((data.data.BlessAmount).toFixed(2));
			echar(data.data.BlessAmount,data.data.AccountBalance);
          
		}else{
			tips("数据读取失败，请稍后再试！");
		}
	}
});

/*福包饼状图*/
function echar(blessAmount,accountBalance){

        var blessMoney =  accountBalance/ blessAmount;
var myChart = echarts.init(document.getElementById('myChart')); 
	var percentage = [1,blessMoney];
    var radius = [.8];
    for (var i = 1; i < percentage.length; ++i) {
        radius.push(radius[0] / Math.sqrt (percentage[0] / percentage[i]));
    }
    var colors = ['#f9435a', '#fbc04f'];
    var labels = ['兑奖金额：'+accountBalance+"元", '福包账户：'+blessAmount+"元"];
    var top = 0.5;

    var series = [];
    
    for (var i = 0; i < percentage.length; ++i) {
        series.push({
            type: 'pie',
            silent: true,
            name: labels[percentage.length - i - 1],
            radius: [0, radius[i] * 100 + '%'],
            label: {
                normal: {
                    show: false,
                    textStyle:{
                        fontsize:'6rem'
                    }
                }
            },
            color: [colors[i]],
            center: ['30%', (top + (-radius[i] + radius[0]) / 2) * 100 + '%'],
            data: [{
                value: 1,
                itemStyle: {
                    textStyle:{
                        fontsize:'6rem'
                    },
                    normal: {
                        color: colors[i],
                        shadowBlur: 30,
                        shadowColor: '#f9435a',
                        
                    }
                }
            }],
            animationType: 'scale'
        });
    }
    myChart.setOption({
        legend: {
            data: labels,
            top: 'center',
            right: 20,
            orient: 'vertical'
        },
        series: series
    });
  }  


    
  /*福包页面*/
    /*账单*/
         $('.fubao_zhangdan').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_zhangdan.html'}};             
                    controlPage(a);

        });
     

        /*充值*/
        $('.fubao_recharge').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_recharge.html'}};           
                    controlPage(a);

        });

        /*提现*/
        $('.fubao_cash').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_cash.html'}};           
                    controlPage(a);

        });
       

        /*充值记录*/
        $('.fubao_chongzhijilu').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_chongzhijilu.html'}};           
                    controlPage(a);

        });
       

        /*提现记录*/
        $('.fubao_tixianjilu').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_tixianjilu.html'}};          
                    controlPage(a);

        });
      

        /*领福包记录*/
        $('.fubao_linfubao').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_linfubao.html'}};           
                    controlPage(a);

        });
     

         /*福包兑换彩票记录*/
        $('.fubao_duihuancaipiao').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_duihuancaipiao.html'}};           
                    controlPage(a);

        });
      
         /*兑奖记录*/
        $('.fubao_duijiangjilu').click(function(){
            var a= {code:1,data:{closePage:'views/fubao.html',isRefresh:'0',params:'',openPage:'views/fubao_duijiangjilu.html'}};           
                    controlPage(a);

        });
     
    
})