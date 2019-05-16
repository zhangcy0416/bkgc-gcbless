+function ($, window) {
    'use strict';

    // 判断animationend事件在各个浏览器中的兼容性
    var prefixes = ["webkit", "moz", "ms", "o", ""];
    // 中将奖品对应的中奖弹窗标题的文案
    var awardList = [
        '恭喜您获得奖金翻倍卡',
        '',
        '恭喜您获得奖金3倍卡',
        '',
        '恭喜您获得充电宝',
        '恭喜您获得购彩10%津贴卡',
        '恭喜您获得华为P20 Pro',
        '',
        '恭喜您获得购彩20%津贴卡',
        ''
    ];
    // 中将奖品对应的中奖弹窗提示的文案
    var awardTips = [
        '彩票兑奖时使用翻倍卡，可将中奖金额 <br />翻倍哦！快去兑奖吧！',
        '',
        '彩票兑奖时使用翻倍卡，可将中奖金额 <br />翻倍哦！快去兑奖吧！',
        '',
        '',
        '购买彩票时，使用此卡，平台将补贴金额的10%， <br />快去购彩吧！',
        '',
        '',
        '购买彩票时，使用此卡，平台将补贴金额的20%， <br />快去购彩吧！',
        ''
    ];
    // 中奖弹窗最底部文案，福包不显示任何东西，其他奖品有效期均为一周
    var awardBottomTexts = [
        '请在一周内使用哦',
        '',
        '请在一周内使用哦',
        '',
        '请在一周内使用哦',
        '请在一周内使用哦',
        '请在一周内使用哦',
        '',
        '请在一周内使用哦',
        ''
    ];
    // 中将奖品对应的中奖弹窗左按钮的文案
    var btnTexts = ['立即兑奖', '立即购彩', '查看宝贝'];
    var base_url = window.baseUrl;
    var config = {
        url_init: base_url + '/wx/getInfo4Game',
        url_start: base_url + '/game/startGame',
        env: Utils.isMobile.Android() ? 'Android' : 'ios'
    };
    var lottery = {};
    var userInfo = {};
    var awardInfo = {};
    // 音频播放对象
    var lucky, coins, bubble;

    // 初始化抽奖相关
    lottery.initialIndex = 0; // 开奖前默认位置；左上角的位置为0，顺时针增大
    lottery.direction = 0; // 旋转方向；0为顺时针，1为逆时针
    lottery.speed = 500; // 旋转速度；其实为切换背景图的时间间隔，越大，速度越慢
    lottery.upCircleCount = 4; // 加速阶段旋转的圈数
    lottery.downCircleCount = 4; //减速阶段旋转的圈数
    lottery.upItemCount = 15;
    lottery.downItemCount = 10;
    lottery.upAccelerationSpeed = -30; // 加速阶段加速度
    lottery.downAccelerationSpeed = 30; // 减速阶段加速度
    lottery.awardIndex = -1; // 中奖的位置，默认是-1
    lottery.running = false; // 抽奖程序是否在运行
    lottery.currentIndex = 0; // 当前的index
    lottery.currentCount = 0; // 已经经过的总个数
    lottery.timerId = -1;

    // 初始化用户信息
    userInfo.luckyPoint = 0; // 幸运值
    userInfo.fubaoAmount = 0; // 福包余额


    function awardListNoScroll() {
        $("#record_container").on("touchmove", function (event) {
            event.preventDefault();
        });
    }


    /**
     * 请求服务器，获取福包余额、幸运值等值
     */
    function init() {
        var guid = window.Utils.getQueryString("guid");
        // var guid = "25ca4b4298eb4fd09f9ff2dc0ce225dd";
        var token = window.Utils.getQueryString("token");
        // userInfo.from = window.Utils.getQueryString("fromWhere");
        if (!guid) {
            window.Modal.showToast("网络请求错误！请重新加载本页面！");
            return;
        }

        userInfo.guid = guid;
        userInfo.token = token;
        var param = {
            "guid": guid
        };
        if (token) {
            param["access_token"] = token;
        }

        $.ajax({
            url: config.url_init,
            data: param,
            method: 'GET',
            success: function (res) {
                console.log("请求成功->", res);
                var data = parseResult(res);
                if (!data) {
                    window.Modal.showToast("网络请求失败：未获取到数据！");
                    return;
                }
                if (data.code == 1000) {
                    console.log("请求成功：", res);
                    userInfo.fubaoAmount = data.data.userAccountInfo.blessAmount;
                    var luckyPoint = parseInt(data.data.luckyValue);
                    userInfo.luckyPoint = luckyPoint > 100 ? 100 : luckyPoint;
                    updateUserInfo(userInfo.fubaoAmount, userInfo.luckyPoint);
                    initLottery();
                } else if (data.code == 2013) {
                    window.Modal.showToast("用户登录状态已失效！");
                    jsToNative("logout");
                } else {
                    window.Modal.showToast("网络请求失败：" + (data.msg));
                }

            },
            error: function (err) {
                console.log("请求失败：", err);
                window.Modal.showToast("网络请求失败:" + err);
            }

        });
        awardListNoScroll();
    }

    /**
     * 根据加载该页面的端的不同，选择不同的初始化方案，因为android与ios在jsToNative的交互方式上存在差异
     */
    function initDevice() {
        userInfo.from = window.Utils.getQueryString("fromWhere");
        if (userInfo.from && userInfo.from == "android") {
            // 解决JsBridge尚未初始化导致的jsToNative交互失败的问题
            if (window.WebViewJavascriptBridge) {
                //do your work here
                init();
            } else {
                document.addEventListener(
                    'WebViewJavascriptBridgeReady'
                    , function () {
                        //do your work here
                        init();
                    },
                    false
                );
            }
            return;
        }
        // ios端以及微信端过来时直接调用初始化方法即可
        init();
    }

    /**
     * 判断互动是否在线，如果不在线直接显示不在线的页面
     */
    function isOnline() {
        var params = {
            'activityId': 101,
            'version': 'js'
        };
        $.ajax({
            url: window.baseUrl + "/activity/getActivityInfo",
            method: "POST",
            data: params,
            timeout: 5 * 1000,
            success: function (res) {
                window.onLineStatus = true;
                if (window.windowLoaded) {
                    removeCover();
                }
                if (!res) {
                    window.Utils.showToast('活动信息错误！');
                    return;
                }
                if (res.code == '1000' && res.data.state == 1) {
                    activityOnline();
                    return;
                }
                activityOffLine();

            },
            error: function (e) {
                window.onLineStatus = true;
                if (window.windowLoaded) {
                    removeCover();
                }
                window.Utils.showToast('网络请求失败！');
            }
        })
    }

    function removeCover() {
        var $cover = $('#cover');
        if ($cover) {
            $cover.remove();
        }
    }

    function activityOnline() {
        console.log('活动在线！');
        initDevice();
    }

    function activityOffLine() {
        console.log('活动下线！');
        $("#activity_offline").css('display', 'block');
        document.documentElement.style.overflow = 'hidden';
        var item = document.getElementById('activity_offline');
        item.addEventListener('touchmove', function (e) {
            e.preventDefault();
        }, false);
    }

    isOnline();


    function initAudio() {
        bubble = new Howl({
            src: ['assets/numjump.mp3']
        });

        lucky = new Howl({
            src: ['assets/lucky.mp3']
        });

        coins = new Howl({
            src: ['assets/mega_win_coins.mp3']
        });
    }

    initAudio();

    function initLottery() {

        // 初始化默认选中item
        var initialItem = $('#item-' + lottery.initialIndex)
        if (initialItem) {
            initialItem.addClass('selected');
        }
        // 初始化抽奖点击事件
        var startBtn = $('#start');
        if (startBtn) {
            startBtn.on("touchend", function () {
                console.log("开始抽奖................");
                if (lottery.running) {
                    console.log("抽奖程序正在运行...");
                    return;
                }
                if (userInfo.fubaoAmount >= 5) {
                    lottery.running = true;
                    startGame();
                    $("#start").toggleClass('btn_disabled');

                } else {
                    paymentDialog();
                }
            });
        } else {
            console.log("startBtn is null!");
        }
    }

    function updateUserInfo(fubaoAmount, luckyPoint) {
        // 初始化福包金额
        if (fubaoAmount >= 0) {
            fubaoAmount = window.Utils.formatNumber(fubaoAmount);
            var fubaoAmountView = $('#fubao_amount');
            fubaoAmountView.text(fubaoAmount);
            var fubaoText = fubaoAmount + "";
            if (fubaoText.length >= 6) {
                fubaoAmountView.css({
                    "font-size": "0.3rem",
                    "top": "52%",
                    "left": "38%"
                });
            } else {
                fubaoAmountView.css({
                    "font-size": "0.4rem",
                    "top": "48%",
                    "left": "44%"
                });
            }
        }

        // 初始化幸运值
        if (typeof luckyPoint !== 'undefined' && luckyPoint !== -1 && luckyPoint <= 100) {
            var pro = luckyPoint + "%";
            $('#progress').width(pro);
            $('#lucky_point').text(luckyPoint);
            if (luckyPoint === 100) {
                $(".progress_value").css("right", "-0.2rem");
            }
        }
    }

    // 请求网络接口
    function startGame() {
        var id = setInterval(changeItem, 500);
        var param = {
            "guid": userInfo.guid
        };
        var token = userInfo.token;
        if (token) {
            param["access_token"] = token;
        }
        $.ajax({
            url: config.url_start,
            method: "POST",
            data: param,
            timeout: 5 * 1000,
            success: function (res) {
                clearInterval(id);
                console.log(res);
                if (res) {
                    var data = parseResult(res);
                    if (!data) {
                        onComplete("网络请求失败，返回数据为空！");
                        return;
                    }
                    awardInfo = data.data;
                    if (data.code == 1000) {
                        updateUserInfo(userInfo.fubaoAmount - 5, userInfo.luckyPoint);
                        // 稍后更新幸运值，避免中了稀有奖品后幸运值立即清空的问题
                        userInfo.luckyPoint = data.data.luckyValue;
                        // 在界面更新用户信息之后再更新福包余额，避免用户中了福包类奖品后，提前知道所中奖品的尴尬
                        userInfo.fubaoAmount = data.data.blessamount;
                        lottery.awardIndex = parseInt(data.data.position);
                        if (lottery.awardIndex >= 0 && lottery.awardIndex <= 9) {
                            doStartGame();
                        } else {
                            onComplete("中奖位置返回错误：" + lottery.awardIndex);
                        }
                    } else if (data.code == 2013) {
                        onComplete("用户登录状态已失效！");
                        jsToNative("logout");
                    } else {
                        onComplete("网络请求返回错误:" + (data.msg));
                    }
                } else {
                    onComplete("网络请求返回数据错误！");
                }
            },
            error: function (e) {
                clearInterval(id);
                onComplete("网络请求失败!");
            }
        })

    }

    function parseResult(data) {
        var res;
        if (typeof data === 'string') {
            res = JSON.parse(data);
        } else {
            res = data;
        }
        return res;
    }

    function changeItem() {
        try {
            var item = $('#item-' + lottery.currentIndex);
            item.removeClass('selected');
            lottery.currentIndex += 1;
            if (lottery.currentIndex === 10) {
                lottery.currentIndex = 0;
            }
            item = $('#item-' + lottery.currentIndex);
            item.addClass('selected');
            bubble.play();

        } catch (e) {
            window.alert(e);
        }
    }

    function doStartGame() {
        changeItem();
        lottery.timerId = setTimeout(doStartGame, lottery.speed);
        lottery.currentCount += 1;
        if (lottery.currentCount > lottery.upItemCount + 20) { // 高速、匀速运转20格，即2圈
            if (lottery.speed >= 500) {
                lottery.speed += 20; //防止速度过慢，减速的加速度从30->20
            } else {
                lottery.speed += lottery.downAccelerationSpeed;
            }

        } else if (lottery.currentCount < lottery.upItemCount) {
            lottery.speed += lottery.upAccelerationSpeed;
        } else {
            // 保持高速、匀速运转20格
        }
        if (lottery.currentCount > lottery.upItemCount + 20 &&
            lottery.currentIndex === lottery.awardIndex) {
            // 避免速度很快的时候游戏中止
            if (lottery.speed <= 400) return;
            clearTimeout(lottery.timerId);
            var index = lottery.awardIndex;
            var music, blinkCount;
            if (index === 1 || index === 3 || index === 7 || index === 9) {
                music = coins;
                blinkCount = 4;
            } else {
                music = lucky;
                blinkCount = 8;
            }
            try {
                music.play();
            } catch (e) {
                window.alert(e);
            }
            updateUserInfo(userInfo.fubaoAmount, userInfo.luckyPoint);
            doorBlink(blinkCount);
            // 在用户抽奖结束后更新中奖列表
            window.requestAwardList();
            console.log("抽奖结束：", lottery.awardIndex);
        }
    }

    // 灯光闪烁效果
    function doorBlink(blinkCount) {
        var index = 0;
        var pageBody = $("#body");
        var intervalId = setInterval(function () {
            if (index % 2 === 0) {
                pageBody.removeClass("body_nor");
                pageBody.addClass("body_blink");
            } else {
                pageBody.removeClass("body_blink");
                pageBody.addClass("body_nor");
            }
            index++;
            if (index === blinkCount) {
                clearInterval(intervalId);
                zhongJiangDialog();
                onComplete();

            }
        }, 350);
    }

    /**
     * 请求结束时数据初始化处理，包括正常结束和错误情况，根据errMsg是否为空判断
     * @param {Object} errMsg
     */
    function onComplete(errMsg) {
        if (errMsg) {
            window.Modal.showToast(errMsg);
        }
        lottery.running = false;
        lottery.currentCount = 0;
        lottery.speed = 500;
        lottery.awardIndex = -1;
        $("#start").toggleClass('btn_disabled');
    }

    function zhongJiangDialog() {
        var html = '<div class="modal_bg">';
        html += '<div class="modal_container">';
        html += '<div class="zhongjiang_title">' + awardList[lottery.awardIndex] + '</div>';
        html += '<img src="http://oss.8fubao.com/duobao/img/awards/award_' + (lottery.awardIndex === 7 ? 3 : lottery.awardIndex) + '.png" class="award" />';
        html += '<div class="zhongjiang_tip">' + awardTips[lottery.awardIndex] + '</div>';
        html += '<div class="btns">';
        var btnText = getBtnText(lottery.awardIndex);
        if (btnText != "") {
            html += '<button type="button" class="btn_left">' + btnText + '</button>';
            html += '<button type="button" class="btn_right" id="btn_right">继续夺宝</button>';
        } else {
            html += '<button type="button" class="btn_one" id="btn_right">继续夺宝</button>';
        }

        html += '</div>';
        html += '<div style="color:white;margin-top:5px;">' + awardBottomTexts[lottery.awardIndex] + '</div>';
        html += '</div></div>';
        $(html).appendTo($('body'));
        $('.modal_container').addClass('dialogZoomOut');
        bindZhongJiangEvents();

    }

    function getBtnText(position) {
        if (position < 0 || position > 9) {
            console.log("getBtnText error:" + position);
            return '';
        }
        if (position == 0 || position == 2) {
            return btnTexts[0];
        }
        if (position == 5 || position == 8) {
            return btnTexts[1];
        }
        if (position == 4 || position == 6) {
            return btnTexts[2];
        }
        return '';

    }

    function bindZhongJiangEvents() {
        $('html').addClass('noscroll');
        $(".modal_bg").on('touchmove', function (event) {
            event.preventDefault();
        });
        var length = prefixes.length;
        for (var index = 0; index < length; index++) {
            var item = prefixes[index];
            var event;
            if (index === length - 1) {
                event = "animationend";
            } else {
                event = item + "AnimationEnd";
            }
            $(".modal_container").on(event, function (event) {
                bindZhongJiangBtnEvents();
            });
        }
    }

    function bindZhongJiangBtnEvents() {
        // 设置领奖界面两个按钮的点击事件
        var btnLeft = $('.btn_left');
        var rightBtn = $("#btn_right");
        rightBtn.on('click', function () {
            dismissZhongJiangDialog();
        });
        if (!btnLeft) return;
        btnLeft.on('click', function () {
            // 来自app端
            if (userInfo.from) {
                jsToNative('awardsJump');
            } else {
                if (window.Utils.isMobile.Android()) {
                    location.href = "https://arexdq.mlinks.cc/AdxJ?bvapp=" + JSON.stringify(awardInfo);
                } else if (window.Utils.isMobile.iOS()) {
                    // IOS传递信息的编码方式更改，并添加GET请求的key，防止safari在末尾自动添加=的问题
                    location.href = base_url + '/envelope/alertSafari.html?bvapp=' + (encodeURIComponent(JSON.stringify(awardInfo)));
                } else {
                    console.log("not ios or android.............");
                }
            }
            dismissZhongJiangDialog();
        });
    }

    // 从js中向native传递数据
    function jsToNative(content) {
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
            if (!content) return;
            if (content == 'awardsJump') {
                bridge.callHandler('awardsJump', awardInfo,
                    function responseCallback(responseData) {
                        console.log("JS received response:", responseData)
                    })
            } else if (content == 'payment') {
                bridge.callHandler('payment', 'payment',
                    function responseCallback(responseData) {
                        console.log("JS received response:", responseData)
                    })
            } else if (content == 'logout') {
                bridge.callHandler('logout', 'logout',
                    function responseCallback(responseData) {
                        console.log("JS received response:", responseData)
                    })
            } else {
                console.log("unknown event type:" + content);
            }

        })
    }

    function unbindZhongJiangEvents() {
        $('html').removeClass('noscroll');
        $('.modal_bg').off('touchmove');
        $('.btn_left').off('click');
        $('#btn_right').off('click');
    }

    function dismissZhongJiangDialog() {
        $('.modal_bg').remove();
        unbindZhongJiangEvents();
    }

    function paymentDialog() {
        var html = '<div class="modal_bg">';
        html += '<div class="payment_container">';
        html += '<img src="http://oss.8fubao.com/duobao/img/payment.png" onclick="return false"/>';
        html += '<div class="payment_text">抱歉，福包余额不足5元</div>';
        html += '<div class="payment_btns">';
        html += '<div class="payment_btn_left">充值</div>';
        html += '<div class="payment_btn_right">取消</div>';
        html += '</div></div></div>';
        $(html).appendTo($('body'));
        $('.payment_container').addClass('dialogZoomOut');

        bindPaymentDialogEvents();
    }

    /**
     * 该方法用于dialog跟随窗口滚动
     */
    function scrollWithWindow() {
        var item = window.document.getElementsByClassName('payment_container')[0];
        var height = item.offsetTop;
        window.onscroll = function () {
            console.log("hello world");
            moveAlong(item, height)
        }
    }

    function bindPaymentDialogEvents() {
        $(".modal_bg").on('touchmove', function (event) {
            event.preventDefault();
        });

        var length = prefixes.length;
        for (var index = 0; index < length; index++) {
            var item = prefixes[index];
            var event;
            if (index == length - 1) {
                event = "animationend";
            } else {
                event = item + "AnimationEnd";
            }
            $(".payment_container").on(event, function (event) {
                bindPaymentBtnEvents();
            });
        }
    }

    function bindPaymentBtnEvents() {
        $(".payment_btn_left").on('click', function () {
            dismissPaymentDialog();
            if (userInfo.from) {
                jsToNative('payment');
            } else {
                if (window.Utils.isMobile.Android()) {
                    location.href = "https://arexdq.mlinks.cc/AdxJ?bvapp=payment";
                } else if (window.Utils.isMobile.iOS()) {
                    location.href = base_url + '/envelope/alertSafari.html?bvapp=payment';
                } else {
                    console.log("not ios or android.............");
                }
            }
        });
        $(".payment_btn_right").on('click', function () {
            dismissPaymentDialog();
        });
    }

    function dismissPaymentDialog() {
        var modalBg = $('.modal_bg');
        modalBg.remove();
        modalBg.off("touchmove");
        $(".payment_btn_left").off("click");
        $(".payment_btn_right").off("click");
    }

    function moveAlong(obj, top) {
        var h_scrollTop = window.document.documentElement.scrollTop || window.document.body.scrollTop; //滚动的距离
        var h_buchang = 20;
        console.log(obj.offsetTop + ";" + h_scrollTop + ";" + top);
        clearTimeout(scrollTimerId);
        if (obj.offsetTop < h_scrollTop + top - h_buchang) {
            obj.style.top = obj.offsetTop + h_buchang + "px";
            setTimeout(function () {
                moveAlong(obj, top);
            }, 80);
        } else if (obj.offsetTop > h_scrollTop + top + h_buchang) {
            obj.style.top = (obj.offsetTop - h_buchang) + "px";
            setTimeout(function () {
                moveAlong(obj, top);
            }, 80);
        } else {
            obj.style.top = h_scrollTop + top + "px";
        }
    }

    window.UserInfo = userInfo;
}(jQuery, window);