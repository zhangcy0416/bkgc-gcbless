$.cookie('codePhone',"");//每次进入清除存储的发短信的手机号
$(function(){
    /*注册*/
    $('.registerBtn').click(function(){
        $.ajax({
            url:url+"/bvi/auth/register",
            data:{AccessToken:access_token,Phone:$('.Phone').val(),Password:$('.passWord').val(),SmsCode:$('.smsCode').val(),NickName:$('.Phone').val()},
            beforeSend:function(){
                
                if($('.Phone').val()==""){
                    tips('请输入手机号!','');
                    return false;
                }
                var patrn = /^1[3|4|5|7|8]\d{9}$/;
                if(!patrn.test($('.Phone').val())){
                    tips('请输入正确的手机号!','');
                    return false;
                }
                if($.cookie('codePhone')!=""&&$('.Phone').val()!=$.cookie('codePhone')){
                    tips('请输入正确手机号！','');
                    return false;
                }
                if($('.smsCode').val()==""){
                    tips('请输入短信验证码！','');
                    return false;
                }
                var reg = /^[a-zA-Z\d]{6,100}$/;
                if(!reg.test($('.passWord').val())){
                    tips('密码为六位以上数字或字母！','');
                    return false;
                }
                
            },
            success: function(data){
                //var data=eval("("+data+")");//转换为json对象
                var data=data;

                if(data.code==1){
                    
                    $.cookie("userid",data.data.UserId,{expires:24*30});
                    getDeviceInfo();
                    saveUserInfo(data);
                     var a= {code:1,data:{closePage:'views/login.html',openPage:'views/home.html',params:'',isRefresh:'0'}};
                    controlPage(a);
                }else{
                    tips(data.error);
                }
            }
        })
    })
    
    /*登录*/
    $('#loginBtn').click(function(){
        $.ajax({
            url:url+"/bvi/auth/login",
            data:{AccessToken:access_token,UserName:$('.Phone').val(),Password:$('.passWord').val()},
            beforeSend:function(){
                if($('.Phone').val()==""){
                    tips('请输入手机号!','');
                    return false;
                }
                var patrn = /^1[3|4|5|7|8]\d{9}$/;
                if(!patrn.test($('.Phone').val())){
                    tips('请输入正确的手机号!','');
                    return false;
                }
                var reg = /^[a-zA-Z\d]{6,100}$/;
                if(!reg.test($('.passWord').val())){
                    tips('密码为六位以上数字或字母！','');
                    return false;
                }
            },
            success: function(data){
                //var data=eval("("+data+")");//转换为json对象
                var data=data;
                if(data.code==1){
                    $.cookie("userid",data.data.UserId,{expires:24*365*5});
                    getDeviceInfo();
                    saveUserInfo(data);
                    var a= {code:1,data:{closePage:'views/login.html',openPage:'views/home.html',params:'',isRefresh:'0'}};
                    controlPage(a);
                }else{
                    tips("用户名或密码错误！");
                }
            }
            
        })
    })

  $('.login_home').click(function(){
            var a= {code:1,data:{closePage:'views/login.html',params:'',isRefresh:'0',openPage:'views/home.html'}};
                    controlPage(a);

        });
  $('.regist_home').click(function(){
            var a= {code:1,data:{closePage:'views/register.html',params:'',isRefresh:'0',openPage:'views/home.html'}};
                    controlPage(a);
        });
  $('#qq').click(function(){
            var a= {code:1,data:{type:'QQ'}};
                    thirdLogin(a);
        });

  $('#weixin').click(function(){
            var a= {code:1,data:{type:'WeChat'}};
                    thirdLogin(a);
            $('#weixin').attr("disabled",true);
                 
        });
  $('#weibo').click(function(){
            var a= {code:1,data:{type:'SinaWeibo'}};
                    thirdLogin(a);
        });

})




 var ua = navigator.userAgent.toLowerCase();  
if (/android/.test(ua)){
        function saveUserInfo(data){//保存用户userID
              window.WebViewJavascriptBridge.callHandler(
                        'saveUserInfo'
                        , data
                        , function(responseData) {                  
                           // tips(data);
                        }
                    );
        }
        //第三方登陆
        function thirdLogin(data){//登陆类型
              window.WebViewJavascriptBridge.callHandler(
                        'thirdLogin'
                        , data
                        , function(responseData) {

                            /*console.log(responseData);*/
                            //json字符串转json对象
                            var val= eval("("+responseData+")");
                            if(val.cdoe == 0){
                                return false;   
                            }

                            /*console.log(val);*/
                                $.ajax({
                                    url:url+"/bvi/auth/loginByWeiXin",
                                    data:{
                                        OpenId: val.data.openid,
                                        NickName: val.data.screen_name,
                                        FacePhotoPath: val.data.profile_image_url,
                                        Sex: val.data.gender,
                                        Country: val.data.country,
                                        Province:val.data.province,
                                        City:val.data.city,
                                        UnionId:val.data.unionid,
                                        AccessToken: access_token
                                    },
                                    success:function( el ){
                                       var el=el;
                                        if( el.code == 1 ){
                                            $('#weixin').attr("disabled",false); 
                                             $.cookie("userid",el.data.UserId,{expires:24*365*5});
                                                if(el.data.Phone == null  || el.data.Phone == ""  ){
                                                     
                                                     window.location.href = "../views/bangdingshouji.html?openid="+val.data.openid;
                                                }else{
                                                      
                                                    saveUserInfo(el);
                                                    var a= {code:1,data:{closePage:'views/login.html',openPage:'views/home.html',params:'',isRefresh:'0'}};
                                                    controlPage(a);
                                                    
                                                }
                                        }else{
                                              tips(el.error);
                                        }
                                    },
                                    error:function(mag){
                                        tips(mag);
                                    }
                                })
                            
                        });
        }        
    } else if (/iphone|ipad|ipod/.test(ua)){
         
        }

/*登陆日志*/
function loginLog(data){
    var userid = $.cookie('userid');
    $.ajax({
        url:url+"/bvi/auth/loginLog",
        data:{AccessToken:access_token,UserId:userid,Location:data.location,Way:1,PhoneModel:data.phoneModel,Manufacturer:data.manufacturer,PhoneUuid:data.honeUuid,Platform:data.platform},
        success: function(data){
           // var data=eval("("+data+")");
            if (data.code==1) {
          // tips('good');
        }
}    });


}