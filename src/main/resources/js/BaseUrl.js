(function (window) {
    //var baseUrl ="http://b20030.8fubao.com";
    var baseUrl = window.location.protocol + "//" + window.location.hostname + "/api-bless";
    var fbtxUrl = window.location.protocol + "//" + window.location.hostname + "/api-fbtxfacade";
    window.baseUrl = baseUrl;
    window.fbtxUrl = fbtxUrl;
})(window);