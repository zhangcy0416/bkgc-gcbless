
var hammer = '';
var currentIndex = 0;
var body_width = $('body').width();
var body_height = $('body').height();

$("#clipArea").photoClip({
	width: body_width * 0.8125,
	height: body_width * 0.8125,
	file: "#upload2",
	view: "#hit",
	ok: "#clipBtn",
	loadStart: function () {
		//console.log("照片读取中");
		$('.lazy_tip span').text('');
		
	},
	loadComplete: function () {
		//console.log("照片读取完成");
		
	},
	clipFinish: function (dataURL) {
		$('#hit').attr('src', dataURL);
		console.log(dataURL)
		imgup(dataURL)
	}
});
function imgup(imgMess){
				console.log(imgMess)
		$.ajax({
			url:url+"/bvi/auth/updateFacePhotoPath",
			type:"post",
			data:{UserId:userid,AccessToken:access_token,FacePhotoPath:imgMess},
			success:function(data){
				if(data.code ==1 ){
					window.location.href = "myPage_mymessage.html";
				}
			}
		})
}
//图片上传
function saveImageInfo() {
	var filename = $('#hit').attr('fileName');
	var img_data = $('#hit').attr('src');
	if(img_data==""){alert('null');}
	//alert(filename+'|'+img_data);
   	render(img_data); 
	
	$.post("这里填写图片获取的网址", {image: img_data}, function (data) {
		if (data != '') {
//			console.info(data);
			//data 为返回文件名；
			alert('提交成功');
		}
	});
}

/*获取文件拓展名*/
function getFileExt(str) {
	var d = /\.[^\.]+$/.exec(str);
	return d;
}

//图片上传结束



function Close(){$('#plan').hide();}

// 渲染 Image 缩放尺寸  
function render(src){  
	 var MAX_HEIGHT = 256;  //Image 缩放尺寸 
    // 创建一个 Image 对象  
    var image = new Image();  
	
    // 绑定 load 事件处理器，加载完成后执行  
    image.onload = function(){  
		// 获取 canvas DOM 对象  
  		var canvas = document.getElementById("myCanvas"); 
        // 如果高度超标  
        if(image.height > MAX_HEIGHT) {  
            // 宽度等比例缩放 *=  
            image.width *= MAX_HEIGHT / image.height;  
            image.height = MAX_HEIGHT;  
        }  
        // 获取 canvas的 2d 环境对象,  
        // 可以理解Context是管理员，canvas是房子  
        var ctx = canvas.getContext("2d");  
        // canvas清屏  
        ctx.clearRect(0, 0, canvas.width, canvas.height); 
        canvas.width = image.width;        // 重置canvas宽高  
        canvas.height = image.height;  
        // 将图像绘制到canvas上  
        ctx.drawImage(image, 0, 0, image.width, image.height);  
        // !!! 注意，image 没有加入到 dom之中  
		
	 var dataurl = canvas.toDataURL("image/jpeg");  
	 var imagedata =  encodeURIComponent(dataurl); 
	 	$('#plan').attr('data-src',dataurl); 
	  $('#plan').show();
    };  
    // 设置src属性，浏览器会自动加载。  
    // 记住必须先绑定render()事件，才能设置src属性，否则会出同步问题。  
    image.src = src;	
};  
