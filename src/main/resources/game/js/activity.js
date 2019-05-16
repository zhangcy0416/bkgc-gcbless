var startflag = true;
$("#start").click(function(){
    if(startflag){
        $.ajax({
            type: 'GET',
            url:"/activity/activityStart",
            //url:  "http://123.56.82.85:20030/activity/activityStart",
            // url:  "http://localhost:20030/activity/activityStart",
            cache: false,
            async: false,
            success: function(data) {
                if(data == "ok"){
                    alert("年会启动成功");
                }

            },
            error: function(err) {
            }
        })

        $("#start").text("年会结束");
    }else{
        $("#start").text("年会启动");
    }
    startflag = !startflag;
})


var pauseflag = true;
$("#pause").click(function(){
    if(pauseflag){
        send("{\"type\":\"3\",\"command\":\"stop\"}");
        $("#pause").text("弹幕继续");
    }else{
        send("{\"type\":\"3\",\"command\":\"continue\"}");
        $("#pause").text("弹幕暂停");
    }
    pauseflag = !pauseflag;
})



$("#machineIdsSub").click(function(){

    var str = "";
    for (var i=1;i<=vtCount;i++) {
        var s = $("#vt"+i).val();
        str = str +","+s;
    }
    var machineIDs = str.substr(1);
    $.ajax({
        type: 'GET',
        url:"/activity/saveMachineIds?machineIds="+machineIDs,
        //url:  "http://123.56.82.85:20030/activity/saveMachineIds?machineIds="+machineIDs,
        // url:  "http://localhost:20030/activity/saveMachineIds?machineIds="+machineIDs,
        cache: false,
        async: false,
        success: function(data) {
            if(data == "ok"){
                alert("自助机添加成功");
                location.reload();
            }

        },
        error: function(err) {
        }
    })
})


//调用二维码
$("#qrCode").click(function(){
    var timeout = $('#timeout').find("option:selected").val();

    $.ajax({
        type: 'GET',
        url:"/activity/sendQrCode?timeout="+timeout,
        //url:  "http://123.56.82.85:20030/activity/sendQrCode?timeout="+timeout,
        // url:  "http://localhost:20030/activity/sendQrCode?timeout="+timeout,
        cache: false,
        async: false,
        success: function(data) {
            if(data == "ok"){
                location.reload();
            }

        },
        error: function(err) {
        }
    })
})








var vtCount = 1;
$("#addMachineId").click(function(){
    vtCount++;
    var tr = '<tr><td><input type="text"  id="vt'+vtCount+'"  placeholder="填写自助机"></td><td>&nbsp;&nbsp;<button class="delMachineId" id="button'+vtCount+'" onclick="oncli(id)">删除</button></td></tr>';
    $("#validTable").append(tr);
});

function oncli(id){
    $("#"+id).parents("tr").remove();
}

var websocket = null;


$("#wsopen").click(function () {
    var wsUrl = $('#websocket').find("option:selected").val();
    openWebsocket(wsUrl);
});


//判断当前浏览器是否支持WebSocket
function openWebsocket(wsUrl){
    if('WebSocket' in window){
        var random = Math.random();
        websocket = new WebSocket(wsUrl);
        // websocket = new WebSocket("ws://localhost:30000/getBlessAndAwad/manager");
    }
    else{
        alert('Not support websocket')
    }
}

if(websocket != null){


    //连接发生错误的回调方法
        websocket.onerror = function(){
            setMessageInnerHTML("error");
        };

    //连接成功建立的回调方法
        websocket.onopen = function(event){
            setMessageInnerHTML("open");
        }

    //接收到消息的回调方法
        websocket.onmessage = function(event){
            setMessageInnerHTML(event.data);
        }

    //连接关闭的回调方法
        websocket.onclose = function(){
            setMessageInnerHTML("close");
        }

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
        window.onbeforeunload = function(){
            websocket.close();
        }
}

//将消息显示在网页上
function setMessageInnerHTML(innerHTML){
    document.getElementById('message').innerHTML += innerHTML + '<br/>';
}

//关闭连接
function closeWebSocket(){
    websocket.close();
}


//发送消息
function send(message){

    websocket.send(message);
}