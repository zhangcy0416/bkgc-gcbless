   

    $(function(){

        function connectWebViewJavascriptBridge(callback) {
            if (window.WebViewJavascriptBridge) {
                callback(WebViewJavascriptBridge)
            } else {
                document.addEventListener(
                    'WebViewJavascriptBridgeReady'
                    , function() {
                        callback(WebViewJavascriptBridge)
                    },
                    false
                );
            }
        }

        connectWebViewJavascriptBridge(function(bridge) { 
            console.log('进入javabridge','1');
            bridge.init(function(message, responseCallback) {
                   console.log('进入bridgeinit','2');
                window.WebViewJavascriptBridge.callHandler(
                'getAccessToken'
                , {'param': '中文测试'}
                , function(responseData) {
                   
                    var data=eval('('+responseData+')');//转换为json对象
                if(data.code==1){
                    access_token=data.data.access_token;
         $.cookie("access_token",data.data.access_token);
         tips(access_token);
                   }else{
                    tips(2);
                }

                }
            );
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
    })
