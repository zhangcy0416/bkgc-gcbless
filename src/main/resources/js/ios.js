
 $(function(){
    function setupWebViewJavascriptBridge(callback) {
        if (window.WebViewJavascriptBridge) { return callback(WebViewJavascriptBridge); }
        if (window.WVJBCallbacks) { return window.WVJBCallbacks.push(callback); }
        window.WVJBCallbacks = [callback];
        var WVJBIframe = document.createElement('iframe');
        WVJBIframe.style.display = 'none';
        WVJBIframe.src = 'https://__bridge_loaded__';
        document.documentElement.appendChild(WVJBIframe);
        setTimeout(function() { document.documentElement.removeChild(WVJBIframe) }, 0)
    }

    setupWebViewJavascriptBridge(function(bridge) {
        var uniqueId = 1
        function log(message, data) {
            var log = document.getElementById('log')
            var el = document.createElement('div')
            el.className = 'logLine'
            el.innerHTML = uniqueId++ + '. ' + message + ':<br/>' + JSON.stringify(data)
            if (log.children.length) { log.insertBefore(el, log.children[0]) }
            else { log.appendChild(el) }
        }
            bridge.callHandler('getAccessToken', {'dsf': 'df'}, function(resdata){//获取token
                tips(resdata);
                  var data=eval("("+resdata+")");//转换为json对象
                   if (data.code==1) {
                    access_token=data.data.access_token;
                $.cookie("access_token",data.data.access_token);
                }
                                         //      access_token=resdata;
                  });
    
    }) 
        });