cookies();
/*兑奖记录*/
$(function() {

	$('.duijiang_fubao').click(function() {
		var a = {
			code: 1,
			data: {
				closePage: 'views/fubao_duijiangjilu.html',
				openPage: 'back',
				isRefresh: '0',
				params: ''
			}
		};
		controlPage(a);

	});
})
load();
//upload(0);		
var num = 0;

function upload(num) {
	$.ajax({
		url: url + "/bvi/lotto/getLottoExchangeOrderList",
		data: {
			AccessToken: access_token,
			UserId: userid,
			PageIndex: num,
			PageSize: 5
		},
		success: function(data) { //console.log(data);
			//alert(11)
			//var data=eval("("+data+")");//转换为json对象
			var data = data;
			if(data.code == 1) {
				str = "";
				var TicketNum = "";
				$.each(data.data.List, function(index, el) {
					
					if(el.Status == 2) {
						el.Status = "等待兑奖";
						el.Amount = "--";
					} else if(el.Status == 4) {
						el.Status = el.Remark;
						el.Amount = "--";
		
					} else if(el.Status == 5 && el.Amount==null) {
						el.Status = el.Remark;
						el.Amount = "--";
					}else if(el.Status == 5 && el.Amount!=null) {
						el.Status = "中奖(已兑付)";
					}
					str += '<li><div class="dj_record"> <ul>';
					/*str+='	<li>彩票名称<span>'+el.TicketName+'</span></li>'*/		

					str += '	<li>兑奖票号<span>' + el.TicketNumber + '</span></li>';
					str += '	<li>兑奖金额<span>￥' + el.Amount + '</span></li>';
					str += '<li class="dj_record_t">兑奖状态<span>' + el.Status + '</span></li>';
					str += '<li class="dj_record_b">兑奖时间<span>' + el.CreateTime + '</span></li>';

					str += '</ul> </div></li>';
				});
				$('#list').append(str);

			} else {

				mui.alert("数据读取失败，请稍后再试！");
			}
		}
	});
}

/*刷新  加载*/
function load() {
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
			num = 1;
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
	if(mui.os.plus) {
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
