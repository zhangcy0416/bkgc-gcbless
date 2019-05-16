var url = "http://bvi2.8fubao.com/";
var blessURL;
var AppId = "ae0p0x79c77kccacqasmkpjjp44hii0p";
var AppSecret = "5pixhqhcq7gmdhrfhra7bw1g6nsjx9ur";
var client_id ="8719535d7c0c4b55af124264939fdb99";
var client_secret ="nV0y1daB6mcFGReV1y2HrIVwuu6NKcSX";
var picture_url = "https://s20130.8fubao.com";
var openid;

/**
 * [description]cookie
 * @param  {[type]} name [存储的名字]
 * @param  {[type]} value [存储的内容]
 * @param  {[type]} options [保存时间]
 */
//var userid=$.cookie("id");将名为id的cookie赋值给userid   读取
//$.cookie('userid','id'，24*30);将id存储到cookie名为userid       存储
jQuery.cookie = function(name, value, options) {
    if(typeof value != 'undefined') { // name and value given, set cookie
        options = options || {};
        if(value === null) {
            value = '';
            options.expires = -1;
        }
        var expires = '';
        if(options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if(typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
        }
        var path = options.path ? '; path=' + options.path : '';
        var domain = options.domain ? '; domain=' + options.domain : '';
        var secure = options.secure ? '; secure' : '';
        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
    } else { // only name given, get cookie
        var cookieValue = null;
        if(document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for(var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if(cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};

var userid = $.cookie('userid'); //获取用户id
var access_token = $.cookie('access_token'); //获取tokend

token();
/*获取token*/
function token() {
    /*	if(access_token == null || access_token == '' || typeof(access_token) == "undefined") {
            $.ajax({
                url: url + "/bvi/auth/getAccessToken",
                data: {
                    AppId: AppId,
                    AppSecret: AppSecret
                },
                datatype: "jsonp",
                success: function(data) {
                    var el = data; //转换为json对象
                    if(data.code == 1) {
                        $.cookie("access_token", el.data.access_token, {
                            expires: 24 * 365 * 5
                        });
                        access_token = el.data.access_token;
                    } else {
                        access_token = $.cookie('access_token');
                    }
                }
            });
        }*/
}

//判断是否登录
function cookies() {
    /*	if(userid != null) {

        } else {
            // $.cookie("returnHref",window.location.href);
            tips("您尚未登录，请先登录！", "login.html");
            // throw SyntaxError();
        }*/
}
/**
 * [description]报错提示
 * @param  {[type]} val [报错提示的内容]
 * @param {[type]} url    [跳转地址]
 */
function tips(val, url) {
    if(url == '' || typeof(url) == "undefined") {
        url = "javascript:void(0)"
    }
    var str = "";
    str += "<div class='tips-bgc'></div>";
    str += "  <div class='publicTip'>";
    str += "      <div class='publicTip-cont'>" + val + "</div>";
    str += "      <div class='publicTip-close'>";
    str += "          <a href=" + url + " class='publicTip-s'>确定</a>";
    str += "      </div>";
    str += "  </div>";
    $('body').append(str);
    $('.publicTip').css('margin-top', '-' + $('.publicTip').outerHeight() / 2 + 'px');
}
$(function() { //双按钮时区分点击确实或取消
    $(document).on('click', '.publicTip-close a', function() {
        /*if ($('.publicTip-s').href=='') {


        };*/
        $('.tips-bgc,.publicTip').remove();

    });
})

var ua = navigator.userAgent.toLowerCase();
if(/android/.test(ua)) {
    function exit() { //安卓退出

        window.WebViewJavascriptBridge.callHandler(
            'exit', {
                'param': '中文测试'
            },
            function(responseData) {
                tips('已退出')
            }
        );
    }

    function controlPage(data) { //跳转页面
        window.WebViewJavascriptBridge.callHandler(
            'controlPage', data,
            function(responseData) {

            }
        );

    }

    function recharge(data) { //充值
        window.WebViewJavascriptBridge.callHandler(
            'recharge', data,
            function(responseData) {

            }
        );

    }

    function saveUserInfo(data) { //充值
        window.WebViewJavascriptBridge.callHandler(
            'saveUserInfo', data,
            function(responseData) {

            }
        );

    }

    function getDeviceInfo() { //登录日志
        window.WebViewJavascriptBridge.callHandler(
            'getDeviceInfo', {
                'param': '中文测试'
            },
            function(responseData) {
                var data = eval('(' + responseData + ')'); //转换为json对象
                if(data.code == 1) {
                    loginLog(data);
                }
            }
        );
    }

    function connectWebViewJavascriptBridge(callback) {
        if(window.WebViewJavascriptBridge) {
            callback(WebViewJavascriptBridge)
        } else {
            document.addEventListener(
                'WebViewJavascriptBridgeReady',
                function() {
                    callback(WebViewJavascriptBridge)
                },
                false
            );
        }
    }

    connectWebViewJavascriptBridge(function(bridge) {
        console.log('进入javabridge', '1');
        bridge.init(function(message, responseCallback) {
            console.log('进入bridgeinit', '2');

            console.log('JS got a message', message);
            var data = {
                'Javascript Responds': '测试中文!'
            };
            console.log('JS responding with', data);
            responseCallback(data);
        });

        bridge.registerHandler("s", function(data, responseCallback) {
            document.getElementById("show").innerHTML = ("data from Java: = " + data);
            var responseData = "Javascript Says Right back aka!";
            responseCallback(responseData);
        });
    })
} else if(/iphone|ipad|ipod/.test(ua)) {

    function setupWebViewJavascriptBridge(callback) {
        if(window.WebViewJavascriptBridge) {
            return callback(WebViewJavascriptBridge);
        }
        if(window.WVJBCallbacks) {
            return window.WVJBCallbacks.push(callback);
        }
        window.WVJBCallbacks = [callback];
        var WVJBIframe = document.createElement('iframe');
        WVJBIframe.style.display = 'none';
        WVJBIframe.src = 'https://__bridge_loaded__';
        document.documentElement.appendChild(WVJBIframe);
        setTimeout(function() {
            document.documentElement.removeChild(WVJBIframe)
        }, 0)
    }

    setupWebViewJavascriptBridge(function(bridge) {

        var uniqueId = 1;

        function log(message, data) {
            var log = document.getElementById('log')
            var el = document.createElement('div')
            el.className = 'logLine'
            el.innerHTML = uniqueId++ + '. ' + message + ':<br/>' + JSON.stringify(data)
            if(log.children.length) {
                log.insertBefore(el, log.children[0])
            } else {
                log.appendChild(el)
            }
        }

        bridge.callHandler('isTrueInstallWX', function(aa) {

            //alert(999)

            //alert(aa)
        });
        bridge.registerHandler('isTrueInstallTest', function(data, responseCallback) {

            //alert(data)
            if (data == 0){
                $(".other_login").css("display","none");
                responseCallback(data)
            } else {
                responseCallback(data)
            }

        });

        function getDeviceInfo(e) {
            bridge.callHandler('getDeviceInfo', {
                'dsf': 'df'
            }, function(aa) {
                var data = eval('(' + responseData + ')'); //转换为json对象
                if(data.code == 1) {
                    loginLog(data);
                }
            });
        }

        function controlPage(data) { //跳转页面
            bridge.callHandler('controlPage', data, function(aa) {});
        }

        function saveUserInfo(data) { //传递userID
            bridge.callHandler('saveUserInfo', data, function(aa) {
                //  tips(data);
            });
        }

        function exit() { //安卓退出

            bridge.callHandler('exit', data, function(aa) {
                //  tips(data);
            });
        }

    })
}
