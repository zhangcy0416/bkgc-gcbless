+ function($, window) {
	//url
	var activityUrl = window.fbtxUrl + '/activity/getActivityPrizeListByActivityId';

	var noBindUrl =window.baseUrl +'/FBShare/FBShareLogain.html?';
	var alreadBindUrl =window.baseUrl+ '/FBShare/FBShareAlreadBind.html?';
	var activityId = ''
	//提示框显示时间
	var duration = 2000;
	//活动列表数据
	var dataArr = [];
	//XML 加载完成
	$(function() {
		//获取列表
		acquireActivityList();
		
		var txt = decodeURIComponent(getQueryString('title'));

		$('title').text(txt);

        $('.header').click(function () {
            leavForNext();
        })


	})

	function acquireActivityList() {
		
		var activityId = decodeURIComponent(getQueryString('activityId'));
		askNetworking(activityUrl, {

			activityId: activityId

		}, function(data) {

			console.log('网络数据', data);
			
			var arr =  data['data']['list'];
			
			jointXml(arr);
			
			itemClick();

		}, function() {

			//fail 失败
			
			console.log('失败....');

		})

	}

	//拼接数据
	function jointXml(dataArr) {

		for(var i = 0; i < dataArr.length; i++) {
			
			var item = dataArr[i];

			var str = '<li class="item">' +
				'<img class="game" src="../images/img/fbgameurl.png" />' +
				'<span class="title">'+item.activityFormName+'</span>' +
				'<span class="redNum text-size-15 text-color-66 font-bold">'+item.currentPrizeRed+'</span>' +
				'<span class="alert">已有</span>' +
				'<span class="takeNum text-color-red font-bold">'+item.partakePersonSum+'</span>' +
				'<div class="line2"></div>' +
				'<span class="begin text-color-white font-bold text-size-12">开始</span>' +
				'<span class="time text-size-10 text-color-c7">'+item.prizeTime+'</span>' +
				'<div class="line1"></div>' +
				'<span class="takeRndNum text-size-12 text-color-66">'+item.partakeRed+'红豆参与</span>' +
				'<span class="endRule text-size-12 text-color-bf">'+item.endLevel+'小时开奖1次</span>' +
				'</li>'
			$('#ulitem').append(str);

		}

	}
	//点击事件
	function itemClick () {
		
		$('#ulitem li').click(function  () {
			
			leavForNext();
			
		});
		
		$('#bag').click(function  () {
				leavForNext();		
		});
		
	}
	

	//进行网络请求
	function askNetworking(url, params, successed, fail) {

		$.post(url, params, function(data, status) {

			if('success' == status) {

				if(data && data.code == 1000) {
					successed(data);
				} else {
					Utils.customToast(data.msg, duration);

					fail();
				}

			} else {

				Utils.customToast('服务器异常,请稍后重试', duration);
			}

		}, 'json')

	}

	function getQueryString(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if(r != null) return(r[2]);
		return null;
	}

	//前往下一页
	function leavForNext() {

		var isBinded = decodeURIComponent(getQueryString('isBinded'));
		
		//var isBinded = true;

		if(isBinded=="true") { //绑定过
			var userId = decodeURIComponent(getQueryString('userId'));
			var accesstoken = decodeURIComponent(getQueryString('accesstoken'));
			var random = decodeURIComponent(getQueryString('random'));
			var spaceId = decodeURIComponent(getQueryString('spaceId'));
			var activityId = decodeURIComponent(getQueryString('activityId'));
			window.location.href = alreadBindUrl +
				"userId=" + encodeURIComponent(userId) +
				"&accesstoken=" + encodeURIComponent(accesstoken) +
				"&random=" + encodeURIComponent(random) +
				"&type=wechat" +
				"&spaceId=" + encodeURIComponent(spaceId) +
				"&activityId=" + encodeURIComponent(activityId) +
				"&isBinded=" + encodeURIComponent(isBinded);
		} else {
			//未绑定
			var facePhotoPath = decodeURIComponent(getQueryString('facePhotoPath'));
			var nickName = decodeURIComponent(getQueryString('nickName'));
			var openId = decodeURIComponent(getQueryString('openId'));
			var sex = decodeURIComponent(getQueryString('sex'));
			var unionId = decodeURIComponent(getQueryString('unionId'));
			var random = decodeURIComponent(getQueryString('random'));
			var spaceId = decodeURIComponent(getQueryString('spaceId'));
			var activityId = decodeURIComponent(getQueryString('activityId'));

			window.location.href = noBindUrl +
				"facePhotoPath=" + encodeURIComponent(facePhotoPath) +
				"&nickName=" + encodeURIComponent(nickName) +
				"&openId=" + encodeURIComponent(openId) +
				"&sex=" + encodeURIComponent(sex) +
				"&unionId=" + encodeURIComponent(unionId) +
				"&random=" + encodeURIComponent(random) +
				"&spaceId=" + encodeURIComponent(spaceId) +
				"&activityId=" + encodeURIComponent(activityId) +
				"&isBinded=" + encodeURIComponent(isBinded);
		}

	}

}(jQuery, window)