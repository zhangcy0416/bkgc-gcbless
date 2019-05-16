

var inputs = docunment.getElementsByTagName("input");


for(var i=0;i<inputs.length;i++){

	inputs[i].oninput= function(){
		alert(1)
	}
}