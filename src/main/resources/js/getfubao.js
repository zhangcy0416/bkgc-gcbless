function($, window) {
	/**
	 * 拿到用户信息。直接调用领福包接口。
	 */
	function init() {
		//拿到用户信息调用接口
		var amount = getQueryString("amount");
		var name = getQueryString("name");
		if(amount && name) {
			var delName = decodeURIComponent(name);
			$("#money").text(amount);
			$("#name").text(delName);
		} else {
			var data = getQueryJson();
			var userID = data.userId;
			var groupId = data.groupId;
			var accesstoken = data.accesstoken;
			if(userID && groupId && accesstoken) {
				getFuBaoAmount(userID, groupId, accesstoken);
			}
		}
	}

	function getQueryString(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if(r != null) return unescape(r[2]);
		return null;
	}

	function getQueryJson() {
		var a = location.href;
		var na1 = a.length;
		var na2 = a.indexOf('?');
		var obj = decodeURI(a.substr(na2 + 1, na1 - na2));
		var data = JSON.parse(obj);
		if(data != null) return data;
		return null;
	}

	function getFuBaoAmount(userId, groupId, accesstoken) {
		//调用接口
		$.ajax({
			url: url + "/bvi/blessenvelope/getReceivedBlessEnvelopeList",
			data: {
				AccessToken: accesstoken,
				userId: userid,
				groupId: groupId
			},
			success: function(data) {
				//console.log(data);
			},
			fail: function(status) {
				// 此处放失败后执行的代码
			}
		});
	}
	init();

}(jQuery, window)