+function ($, window) {
    //发送验证码
    var sendeCodeUrl = window.baseUrl + '/message/sendSMS';
    //发送语音验证码
    var sendVoiceCodeUrl = window.baseUrl + '/message/sendVoiceMsg';
    //注册
    var registUrl = window.baseUrl + '/wx/newRegister';
    //订阅
    var bookUrl = window.fbtxUrl + '/space/book';

    //
    var spaceId = ''
    var userId = ''

    //提示框显示时间
    var duration = 2000;

    $(document).ready(function () {

        $('body').css({
            'height': $(window).height()
        });
        spaceId = decodeURIComponent(getQueryString('spaceId'));
        //立即领取
        registerAndGet();
        //发送验证码
        sendeCode();
        //发送语音验证码
        sendVoiceCode();

        $('.cancle').click(function () {

            $('.phone').val('');

        })
    })

    //判断手机号是否正确
    function judgePhone() {

        var phone = $('.phone').val();
        if (phone.length == 0) {
            Utils.customToast('手机号不能为空', duration);
            return '';
        }

        if (!(/^1[34578]\d{9}$/.test(phone))) {
            Utils.customToast('手机号格式不正确', duration);
            return '';
        }

        phone = $('.phone').val();

        return phone;
    }

    //进行网络请求
    function askNetworking(url, params, successed, fail) {
        $.post(url, params, function (data, status) {

            if ('success' == status) {

                if (data && data.code == 1000) {
                    successed(data);
                } else {
                    Utils.customToast(data.msg, duration);
                    fail();
                }

            } else {

                Utils.customToast('服务器异常,请稍后重试', duration);
                fail();
            }

        }, 'json')

    }

    //获取短信验证码
    function sendeCode() {

        $('.getcode').click(function () { //获取验证码

            var phone = judgePhone()

            if (phone.length == 0) {
                return;
            }

            askNetworking(sendeCodeUrl, {
                phone: phone,
                clientID: "31"
            }, function (data) {

                Utils.customToast('验证码发送成功', duration);

                $('.getcode').attr('disabled', true);

                var y = 60;

                $('.getcode').val(y)

                var tim = setInterval(function () {

                    y -= 1;

                    if (y > 0) {

                        $('.getcode').val(y)

                    } else {

                        clearTimeout(tim)

                        $('.getcode').val('获取验证码');

                        $('.getcode').removeAttr('disabled', false);
                    }

                }, 1000)

            }, function () {

            })

        })

    }

    //发送语音验证码
    function sendVoiceCode() {

        $('.voiceCode').click(function () {

            var phone = judgePhone()

            if (phone.length == 0) {
                return;
            }

            askNetworking(sendVoiceCodeUrl, {
                phone: phone
            }, function (data) {
                Utils.customToast('验证码将通过电话通知到您，请注意接听', duration);

            }, function () {

            })

        })

    }

    //订阅
    function bookSpace() {

        askNetworking(bookUrl, {
            spaceId: spaceId,
            userId: userId
        }, function (data) {

            console.log('订阅空间', data);

            leaveForApp();

        }, function () {

        })
    }

    //前往APP
    function leaveForApp() {
        if (window.Utils.isMobile.Android()) {
            //安卓系统
            //window.location.href = "https://www.pgyer.com/PSxp";
            window.location.href = "http://a.app.qq.com/o/simple.jsp?pkgname=com.bkgc.fbtx";
        } else if (window.Utils.isMobile.iOS()) {
            //iOS
            //location.href = 'alertSafari.html';
            location.href = 'http://bkgc.oss-cn-beijing.aliyuncs.com/help/InstallApp.html';
        }
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return (r[2]);
        return null;
    }

    //立即领取
    function registerAndGet() {

        $('.commit').click(function () { //立即领取

            var phone = judgePhone()

            var code = $('.code').val();

            if (phone.length == 0) {
                return;
            }

            if (code.length == 0) {
                Utils.customToast('验证码不能为空', duration);
                return;
            }

            if (code.length < 6) {
                Utils.customToast('验证码格式不正确', duration);
                return;
            }

            registerAccount();

        })

    }

    function registerAccount() {

        var facePhotoPath = decodeURIComponent(getQueryString('facePhotoPath'));

        var nickName = decodeURIComponent(getQueryString('nickName'));

        var openId = decodeURIComponent(getQueryString('openId'));

        var sex = decodeURIComponent(getQueryString('sex'));

        var unionId = decodeURIComponent(getQueryString('unionId'));

        var random = decodeURIComponent(getQueryString('random'));

        $('.commit').attr('disabled', "true");

//		var facePhotoPath = "http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83ep0ibKhnByslu4458ibBBMjOFz9ic1G9Ml8rvbxdogE4qSFoWuBhv9tv36P0Bom6Mtywod8bLF3h9E4A/132";
//
//		var nickName = 'Maybe@辉';
//
//		var openId = 'oG_jRv_dMcwniK9FXkNiqcgcEgnY';
//
//		var sex = '1';
//
//		var unionId = 'oB0GFw8_Hvc2_gfTO1wxqehGnJIM';

        var phone = judgePhone()

        var code = $('.code').val();

        askNetworking(registUrl, {
            iconUrl: facePhotoPath,
            nickName: nickName,
            phone: phone,
            sex: sex,
            smgCode: code,
            unionId: unionId,
            openId: openId

        }, function (data) {

            console.log('网络数据', data);

            userId = data['data']['guid'];
            bookSpace();

        }, function () {
            $('.commit').removeAttr("disabled");
        });

    }

    //前往下一页
    function getUrlInfo() {

        var isBinded = decodeURIComponent(getQueryString('isBinded'));

        if (isBinded) { //绑定过
            var userId = decodeURIComponent(getQueryString('userId'));
            var accesstoken = decodeURIComponent(getQueryString('accesstoken'));
            var random = decodeURIComponent(getQueryString('random'));
            var spaceId = decodeURIComponent(getQueryString('spaceId'));
            var activityId = decodeURIComponent(getQueryString('activityId'));

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

        }

    }

}(jQuery, window)