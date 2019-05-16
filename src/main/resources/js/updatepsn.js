/**
 * 修改个人信息
 */
/*更新昵称*/
function NickName(){

    $.ajax({
        url:url+"/bvi/auth/updateNickName",
        data:{AccessToken:access_token,UserId:userid,NickName:$('#nickname').val()},
        beforeSend:function(){
            if($('#nickname').val()==""){
                mui.toast('昵称不能为空!');
                return false;
            }
            var regName =/^[\u4e00-\u9fa5a-zA-Z0-9\-]{4,20}$/
            if(!regName.test($('#nickname').val())){
                mui.toast('格式不正确!');
                return false;
            }
            if($('#nickname').val()==RNickName){
                return false;
            }
        },
        success: function(data){
            //var data=eval("("+data+")");
            var data=data;
            if(data.code==1){
                RNickName = $("#nickname").val();
                $("#tip_name span").html(RNickName);
                mui.toast('昵称修改成功!');
            }else{

            }

        }
    });
}

/*更新邮箱*/
function UpdateEmail(){

    $.ajax({
        url:url+"/bvi/auth/updateEmail",
        data:{AccessToken:access_token,UserId:userid,Email:$('#newemail').val()},
        beforeSend:function(){
            if($('#newemail').val()==""){
                tips('邮箱不能为空!');
                return false;
            }
            if($('#newemail').val()==REmailVal){
                return false;
            }
            var filter  = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
            if(!filter.test($('#newemail').val())){
                tips('邮箱格式不正确!');
                return false;
            }
        },
        success: function(data){
            //var data=eval("("+data+")");
            var data=data;

            if(data.code==1){
                REmailVal = $("#newemail").val();
                $("#tip_email span").html(REmailVal);
                mui.toast("邮箱修改成功！");
            }else{
                tips("邮箱修改失败！");
            }
        }
    })
}

/*更新性别*/
$(function(){

    $('.upU_list li ').click(function(){
        var str=$(this).text();
        $.ajax({
            url:url+"/bvi/auth/updateGender",
            data:{AccessToken:access_token,UserId:userid,Gender:str},
            beforeSend:function(){
                if(RGender==str){
                    $(".upU").css("display","none");
                    $(".mark").css("display","none");
                    return false;
                }
            },
            success: function(data){
                var data=data;
                if(data.code==1){
                    $(function(){
                        $(".upU").css("display","none");
                        $(".mark").css("display","none");
                        $('.CGender').html(str);
                        RGender=str;
                    })
                    mui.toast("性别修改成功！");
                }else{
                    tips("性别修改失败，请稍后再试！");
                }
            }
        })
    })
})



/*更新手机号码*/
$("#UpdatePhone").click(function(){

    $.ajax({
        url:url+"/bvi/auth/updatePhone",
        data:{AccessToken:access_token,UserId:userid,Phone:$('.Phone').val(),SmsCode:$('.smsCode').val()},
        beforeSend:function(){
            if($('.Phone').val()==""){
                tips('请输入手机号');
                return false;
            }
            var patrn = /^1[3|4|5|7|8]\d{9}$/;
            if(!patrn.test($('.Phone').val())){
                tips('请输入正确的手机号');
                return false;
            }
            if($('.smsCode').val()==""){
                tips('请输入短信验证码！','');
                return false;
            }
        },
        success: function(data){
            var data=data;
            if(data.code==1){
                document.location.href="myPage_mymessage.html";
            }else{
                tips("更新手机号失败，请稍后再试！");
            }
        }
    })
})

//检测手机是否已经用过
$(".checkPhone").click(function(){
    $('.checkPhone').attr("disabled",true);
    $.ajax({
        url:url+"/bvi/auth/checkDuplicatedPhone",
        data:{AccessToken:access_token,Phone:$('.Phone').val()},
        beforeSend:function(){
            if($('.Phone').val()==""){
                $('.checkPhone').attr("disabled",false);
                tips('请输入手机号');
                return false;
            }
            var patrn = /^1[3|4|5|7|8]\d{9}$/;
            if(!patrn.test($('.Phone').val())){
                $('.checkPhone').attr("disabled",false);
                tips('请输入正确的手机号');
                return false;
            }
        },
        success: function(data){
            var data=data;
            if(data.code==1){

                if($(".checkPhone").attr("rel")==0){
                    $.ajax({
                        url:url+"/bvi/auth/sendSmsCode",
                        data:{Phone:$('.Phone').val()},
                        success: function(data){
                            //var data=eval("("+data+")");//转换为json对象
                            var data=data;
                            if(data.code==1){
                                $.cookie('codePhone',$('.Phone').val());
                                sendNews($('.Phone').val(),$('.checkPhone'));
                            }else{
                                tips("短信验证码发送失败！");
                                $('.checkPhone').attr("disabled",false);
                            }
                        }
                    })
                }
            }else{
                tips("手机号已被注册,请换个新手机号！");
                $('.checkPhone').attr("disabled",false);
            }
        }
    })

})

function sendNews(mobile,obj){
    var time = 60;
    var obj = $(obj);
    var rel = obj.attr("rel");

    if(rel==0){
        var clr = setInterval(function(){
            time--;
            if(time>0){
                //禁用按钮

                //obj.val(time+"秒后重新发送").attr("rel",1);
                obj.html(time+'后重新发送').removeClass('checkPhone').attr("rel",1);
            }else{

                obj.html("重新发送").addClass('checkPhone').attr("rel",0);
                $('.checkPhone').attr("disabled",false);
                clearInterval(clr);
            }
        },1000);
    }
}