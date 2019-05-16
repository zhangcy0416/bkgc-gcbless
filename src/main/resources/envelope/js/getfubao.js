+function ($, window) {
    var type;
    var guid;
    var userID;
    var randNum;
    var amount;
    var name;
    //将数据回传到页面
    //	var data ={};
    //	data.name = "ss";
    //	window.data = data;
    /**
     * 拿到用户信息。直接调用领福包接口。
     */
    function init() {
        //拿到用户信息调用接口
        //先拿到type值，判断type值
        type = getQueryString("type");
        if (type == "wechat") {
            //如果是微信过来
            userID = getQueryString("userId");
            randNum = getQueryString("random");
            if (userID && randNum) {
                getFuBaoAmount(userID, randNum);
            }
        } else {
            amount = getQueryString("amount");
            name = getQueryString("name");
            userID = getQueryString("userId");
            token = getQueryString("token");
            fromWhere = getQueryString("fromWhere");
            if (amount && name) {
                $("#gubao_desc").show().text("送出的福包");
                $("#name").show().text(name);
                $("#money").show().text("￥" + amount);
                $("#lottery").show();
                $("#fubao_account").show();
                $("#getfubao_failed").hide();
            } else {
                randNum = getQueryString("randNum");
                if (userID && randNum) {
                    getFuBaoAmount(userID, randNum);
                }
            }
        }
        //判断是否下线
        getActivityInfo();
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]);
        return null;
    }

    function getFuBaoAmount(userId, randNum) {
        //调用接口
        $.ajax({
            url: window.baseUrl + "/envelope/getBlessEnvelope",
            type: 'POST',
            dataType: "json",
            data: {
                randNum: randNum,
                userId: userId,
            },
            success: function (data) {
                var code = data.code;
                var msg = data.msg;
                switch (code) {
                    case "1000":
                        var data = data.data;
                        name = data.beName;
                        amount = data.amount;
                        $("#gubao_desc").show().text("送出的福包");
                        $("#name").show().text(name);
                        $("#money").show().text("￥" + amount);
                        $("#lottery").show();
                        $("#fubao_account").show();
                        $("#getfubao_failed").hide();
                        break;
                    case "41027": //福包派发中,敬请期待
                        name = data.beName;
                        $("#name").show().text(name);
                        $("#gubao_desc").show().text("送出的福包");
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    case "41029": //领福包出现系统异常，请稍后重试
                        $("#gubao_desc").hide();
                        $("#name").hide();
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    case "41030": //领福包超时！
                        $("#gubao_desc").hide();
                        $("#name").hide();
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    case "41032": //同一个福包每天只能领取一次
                        name = data.beName;
                        $("#name").show().text(name);
                        $("#gubao_desc").show().text("送出的福包");
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    case "41033": //来晚啦,该等级福包已被领完！
                        name = data.beName;
                        $("#name").show().text(name);
                        $("#gubao_desc").show().text("送出的福包");
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    case "41041": //福包不在有效时间段内
                        name = data.beName;
                        $("#name").show().text(name);
                        $("#gubao_desc").show().text("送出的福包");
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break
                    case "41042": //领福包二维码不合法
                        $("#gubao_desc").hide();
                        $("#name").hide();
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    case "41043": //推广福包不能重复领取
                        name = data.beName;
                        $("#name").show().text(name);
                        $("#gubao_desc").show().text("送出的福包");
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text(msg);
                        break;
                    default:
                        $("#gubao_desc").hide();
                        $("#name").hide();
                        $("#money").hide();
                        $("#lottery").hide();
                        $("#fubao_account").hide();
                        $("#getfubao_failed").show().text("领取失败，请稍后重试");
                        break;
                }
            },
            fail: function (status) {
                // 此处放失败后执行的代码
                $("#gubao_desc").hide();
                $("#name").hide();
                $("#money").hide();
                $("#lottery").hide();
                $("#fubao_account").hide();
                $("#getfubao_failed").show().text("网络异常，请稍后重试");
            }
        });
    }

    //是否下线
    function getActivityInfo() {
        $.ajax({
            url: window.baseUrl + "/activity/getActivityInfo",
            type: 'POST',
            dataType: "json",
            data: {
                activityId: "101",
                version:"js",
            },
            success: function (data) {
                var code = data.code;
                console.log("调用判断内测用户接口成功", data);
                if (code === 1000||code==="1000") {
                    console.log("调用判断是否下线接口成功  code ===1000", code);
                    var data = data.data;
                    if(data.state===1){
                    	//没下线
                        console.log("调用判断是否下线接口成功 data.state===1", data);
                        $("#startGame").show();
                        $(".up").show();
                    } else { //下线
                        console.log("调用判断是否下线接口成功  data.state ！=1", data);
                        $("#startGame").hide();
                        $(".up").hide();
                    }
                } else {
                    console.log("调用判断是否下线接口成功  code ！=1000", code);
                    $("#startGame").hide();
                    $(".up").hide();
                }
            },
            fail: function (status) {
                console.log("调用判断是否下线接口失败 ", status);
                $("#startGame").hide();
                $(".up").hide();
            }
        });
    }

    init();
    $(document).ready(function () {
        var css = {
            bottom: '0px'
        };
        $('#startGame').animate(css, 600, rowBack);

        function rowBack() {
            if (css.bottom === '0px')
                css.bottom = '8px';
            else if (css.bottom === '8px')
                css.bottom = '0px';
            $('#startGame').animate(css, 600, rowBack);
        }

        $("#startGame").click(function () {
            //跳转到夺宝游戏页面
            if (type == "wechat") {
                location.href = window.baseUrl + "/duobaogame/index.html?guid=" + decodeURIComponent(userID);
            } else {
                jsToNativeToDuoBao();
            }
        });
        $("#exchange").click(function () {
            if (type == "wechat") {
                openApp();
            } else {
                //js交互，跳转到扫描兑换彩票界面
                jsToNative();
            }
        });
    });

    //打开app
    function openApp() {
        if (window.Utils.isMobile.Android()) {
            //安卓系统
            location.href = "https://arexdq.mlinks.cc/AdxJ";
        } else if (window.Utils.isMobile.iOS()) {
            location.href = window.baseUrl + "/envelope/alertSafari.html";
        } else {
            console.log("not IOS and android");
        }
    }

    //js交互
    function jsToNative() {
        function setupWebViewJavascriptBridge(callback) {
            if (window.WebViewJavascriptBridge) {
                return callback(WebViewJavascriptBridge);
            }
            if (window.WVJBCallbacks) {
                return window.WVJBCallbacks.push(callback);
            }
            window.WVJBCallbacks = [callback];
            var WVJBIframe = document.createElement('iframe');
            WVJBIframe.style.display = 'none';
            WVJBIframe.src = 'https://__bridge_loaded__';
            document.documentElement.appendChild(WVJBIframe);
            setTimeout(function () {
                document.documentElement.removeChild(WVJBIframe)
            }, 0)
        }

        setupWebViewJavascriptBridge(function (bridge) {
            bridge.callHandler('exchangeLottery', null,
                function responseCallback(responseData) {
                    console.log("JS received response:", responseData)
                })
        })
    }

    function jsToNativeToDuoBao() {
        function setupWebViewJavascriptBridge(callback) {
            if (window.WebViewJavascriptBridge) {
                return callback(WebViewJavascriptBridge);
            }
            if (window.WVJBCallbacks) {
                return window.WVJBCallbacks.push(callback);
            }
            window.WVJBCallbacks = [callback];
            var WVJBIframe = document.createElement('iframe');
            WVJBIframe.style.display = 'none';
            WVJBIframe.src = 'https://__bridge_loaded__';
            document.documentElement.appendChild(WVJBIframe);
            setTimeout(function () {
                document.documentElement.removeChild(WVJBIframe)
            }, 0)
        }

        setupWebViewJavascriptBridge(function (bridge) {
            bridge.callHandler('nativeToDuoBao', null,
                function responseCallback(responseData) {
                    console.log("JS received response:", responseData)
                })
        })
    }

}(jQuery, window)