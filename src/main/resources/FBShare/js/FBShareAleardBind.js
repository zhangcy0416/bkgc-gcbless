+function ($, window) {

    //订阅
    var bookUrl = window.fbtxUrl + '/space/book';
    //
    var spaceId = ''
    var userId = ''

    //提示框显示时间
    var duration = 2000;

    $(function () {

        commitHandle();

    });

    function commitHandle() {

        userId = decodeURIComponent(getQueryString('userId'));
        spaceId = decodeURIComponent(getQueryString('spaceId'));

        bookSpace();

        $('.commit').click(function () {

            leaveForApp();
        })

    }

    //订阅
    function bookSpace() {
        askNetworking(bookUrl, {
            spaceId: spaceId,
            userId: userId
        }, function (data) {

            console.log('订阅空间', data);

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

}(jQuery, window)