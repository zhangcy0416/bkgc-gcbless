(function (window) {
    var Utils = {
        getQueryString: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        },
        isArray: function (o) {
            return Object.prototype.toString.call(o) == '[object Array]';
        }
    };

    Utils.isMobile = {
        Android: function () {
            return navigator.userAgent.match(/Android/i) ? true : false;
        },
        BlackBerry: function () {
            return navigator.userAgent.match(/BlackBerry/i) ? true : false;
        },
        iOS: function () {
            return navigator.userAgent.match(/iPhone|iPad|iPod/i) ? true : false;
        },
        Windows: function () {
            return navigator.userAgent.match(/IEMobile/i) ? true : false;
        },
        any: function () {
            return (Utils.isMobile.Android() || Utils.isMobile.BlackBerry() || Utils.isMobile.iOS() || Utils.isMobile.Windows());
        }
    };

    Utils.checkAnim = function (k) {
        var h = "",
            f = document.createElement("div").style,
            j = ["webkit", "moz", "ms", "o"];
        // item,index
        j.forEach(function (m, l) {
            j[l] = m + k.replace(/^\S/, function (i) {
                return i.toUpperCase()
            })
        });
        j.unshift(k);
        for (var g = 0, e = j.length; g < e; g++) {
            if (j[g] in f || j[g] in window) {
                h = j[g];
                break
            }
        }
        return h
    };

    Utils.getStyle = function (e, f) {
        return window.getComputedStyle(e, false)[f]
    };

    Utils.formatNumber = function(x) {
        var f = parseFloat(x);
        if (isNaN(f)) {
            return;
        }
        f = Math.round(x*100)/100;
        return f;
    };

    window.Utils = Utils;
})(window);