(function ($, window) {
    // 无缝滚动marquee效果借鉴:https://github.com/nikoohp/marquee
    var awardList0 = [];
    var speed = 0.4;
    var root = $("#record_container");
    // 通过transform属性的translateY实现无缝的marquee效果，为了浏览器兼容性，要检查transform属性在浏览器中的名称
    var animName = window.Utils.checkAnim("transform");
    // 该属性可能造成一帧之内多次调用的问题，所以弃用
    var requestAnimationFrame = window.Utils.checkAnim("requestAnimationFrame");
    var currentY = 0,
        middle = 0,
        timerId = -1;

    function anim() {
        currentY += speed;
        if (currentY >= 0) {
            currentY = middle;
        }
        animY(currentY);
        // if (requestAnimationFrame) {
        //     window[requestAnimationFrame](anim);
        // } else {
        //     timerId = setTimeout(anim, 16);
        // }

        timerId = setTimeout(anim, 16);
    }

    function animY(y) {
        root[0].style[animName] = "translateY(" + y + "px)"
    }

    function showAwardList(data) {

        if (window.Utils.isArray(data) && data.length > 0) {
            var awardList = data;
            if (awardList.length > 10) {
                awardList.length = 10;
            }
            if (!compare(awardList0, awardList)) {
                console.log("中奖列表有改变，刷新界面！");
                awardList0 = awardList;
                root.empty();

                var len = awardList.length;
                for (var i = 0; i < len; i++) {
                    var award = awardList[i];
                    var container = $("<div></div>");
                    container.css('display', 'flex');
                    container.css('flex-direction', 'row');
                    container.css('align-items', 'center');
                    var icon = $("<img></img>").attr("src", 'http://oss.8fubao.com/duobao/img/msg.png');
                    icon.css('width', 'auto');
                    var content = $("<div></div>").text(award).css('color', '#E3FFFF');
                    container.append(icon, content);
                    root.append(container);
                }
                for (var i = 0; i < len; i++) {
                    var award = awardList[i];
                    var container = $("<div></div>");
                    container.css('display', 'flex');
                    container.css('flex-direction', 'row');
                    container.css('align-items', 'center');
                    var icon = $("<img></img>").attr("src", 'http://oss.8fubao.com/duobao/img/msg.png');
                    icon.css('width', 'auto');
                    var content = $("<div></div>").text(award).css('color', '#E3FFFF'); //
                    container.append(icon, content);
                    root.append(container);
                }

                var total = root[0].getBoundingClientRect().height;
                if (timerId !== -1) {
                    // clearTimeout(timerId);
                } else {
                    // 系数是0.5的时候只有一半的内容会出现，所以改为1；但逻辑上讲应该是0.5
                    var coefficient = 1;
                    animY(-total * coefficient);
                    currentY = -total * coefficient;
                    middle = -total * coefficient;
                    anim();
                }
            }
        } else {
            var container = $("<div></div>");
            var icon = $("<img></img>").attr("src", 'http://oss.8fubao.com/duobao/img/msg.png');
            var content = $("<div></div>").text(data).css('color', '#E3FFFF'); //
            container.append(icon, content);
            root.prepend(container);
        }
    }

    function compare(currentList, newList) {
        var currentItem = currentList[0];
        var newItem = newList[0];
        return currentItem === newItem;
    }

    function requestAwardList() {
        $.ajax({
            url: window.baseUrl + "/game/getAwardList",
            method: 'GET',
            success: function (res) {
                console.log(res);
                var data = res;
                if (data && data.code == 1000) {
                    showAwardList(data.data);
                } else {
                    showAwardList(data.msg);
                }

            },
            error: function (err) {
                console.log("请求失败：", err);
                window.Modal.showToast("网络请求失败~~~~");
            }

        })
    }

    // 测试代码
    //	var index = 10;
    //	var data = ['This is item 1','This is item 2','This is item 3','This is item 4','This is item 5','This is item 6','This is item 7','This is item 8','This is item 9'];
    //	setInterval(function(){
    //		var newData = [];
    //		for(var i in data){
    //			newData[i] = data[i];
    //		}
    //		showAwardList(newData);
    //		data.shift();
    //		data.unshift(("This is item "+index));
    //		index++;
    //		console.log("change->"+index);
    //	},5000);

    requestAwardList();
    // setInterval(requestAwardList, 10 * 1000);
    window.requestAwardList = requestAwardList;
})($, window);