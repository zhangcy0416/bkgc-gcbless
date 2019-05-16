+function ($, window) {

    var isBinded = false;
    var facePhotoPath = "";
    var nickName = "";
    var openId = "";
    var sex = 1;
    var unionId = "";
    var random = "";
    var spaceId = "";
    var activityId = "";
    var userId = "";
    var accesstoken = "";
    var type = "";

    init();

    function init() {
        isBinded = getQueryString("isBinded");
        facePhotoPath = getQueryString("facePhotoPath");
        nickName = getQueryString("nickName");
        openId = getQueryString("openId");
        sex = getQueryString("sex");
        unionId = getQueryString("unionId");
        random = getQueryString("random");
        spaceId = getQueryString("spaceId");
        activityId = getQueryString("activityId");
        userId = getQueryString("userId");
        accesstoken = getQueryString("accesstoken");
        type = getQueryString("type");
        title = getQueryString("title");
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]);
        return null;
    }

    $(document).ready(function () {
        $("#look_other_activity").click(function () {
            if (isBinded=="true") {
                window.location.href = window.baseUrl + "/FBShare/FBShareAlreadBind.html?" +
                    "userId=" + encodeURIComponent(userId) +
                    "&accesstoken=" + encodeURIComponent(accesstoken) +
                    "&random=" + encodeURIComponent(random) +
                    "&type=wechat" + "&spaceId=" +
                    encodeURIComponent(spaceId) +
                    "&activityId=" + encodeURIComponent(activityId) +
                    "&isBinded=" + encodeURIComponent(isBinded) +
                    "&title=" + encodeURIComponent(title);
            } else {
                window.location.href = window.baseUrl +'/FBShare/FBShareLogain.html?' +
                    "facePhotoPath=" + encodeURIComponent(facePhotoPath) +
                    "&nickName=" + encodeURIComponent(nickName) +
                    "&openId=" + encodeURIComponent(openId) +
                    "&sex=" + encodeURIComponent(sex) +
                    "&unionId=" + encodeURIComponent(unionId) +
                    "&random=" + encodeURIComponent(random) +
                    "&spaceId=" + encodeURIComponent(spaceId) +
                    "&activityId=" + encodeURIComponent(activityId) +
                    "&isBinded=" + encodeURIComponent(isBinded) +
                    "&title=" + encodeURIComponent(title);
            }
        });
    });
}(jQuery, window);