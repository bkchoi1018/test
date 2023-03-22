//url의 get변수 GET["key"] = "value"; 형태로 사용.
// COOKIE["jwt"] 쿠키 읽기 가능.
// get("key") 사용시 매번 파싱시도.
var GET, COOKIE;
function getcookieparse(){
	GET = [], COOKIE = [];
	var kv;
	var temp = location.href.split("?",2);
	if(temp.length>1){
		temp = temp[1].split("#");
		temp = temp[0].split("&");
		for(var i=0; i<temp.length; i++){
		  kv = temp[i].split("=",2);
		  if(kv.length==2) GET[kv[0]] = kv[1];
		}
	}
	temp = document.cookie.replace(/ /g, "").split(";");
	for(var i=0; i<temp.length; i++){
	  kv = temp[i].split("=",2);
	  if(kv.length==2) COOKIE[kv[0]] = kv[1];
	}
}
getcookieparse();
var get = function(){
	getcookieparse();
	if(arguments.length>0) return GET[arguments[0]];
};
var cookie = function(){
	getcookieparse();
	if(arguments.length>0) return COOKIE[arguments[0]];
};

function SET_COOKIE(k,v,t){
	var n = arguments.length;
	var _FIRE = "expires=Thu, 01 Jan 1999 00:00:10 GMT;";
	if(n===0){
		getcookieparse();
		for(var k in COOKIE){
			document.cookie = k+"=;"+_FIRE;
			document.cookie = k+"=;"+_FIRE+"path=/";
		}
	}else if(n===1 && typeof k==="string"){
		document.cookie = k+"=;"+_FIRE;
		document.cookie = k+"=;"+_FIRE+"path=/";
	}else if(n===2){
		if(typeof v !== "string") v = JSON.stringify(v);
		document.cookie=k+"="+v+";path=/";
	}else if(n===3){
		var s = "";
		if(typeof t === "number"){
			s = new Date();
			s.setTime(s.getTime() + t*1000);
			s = s.toUTCString();
		}else if(t.__proto__===new Date().__proto__){
			s = t.toUTCString();
		}
		if(typeof v !== "string") v = JSON.stringify(v);
		document.cookie=k+"="+v+";expires="+s+";path=/";
	}
	getcookieparse();
}


function CHECK_LOCAL_STORAGE(){
	var storage;
	try {
		storage = window['localStorage'];
		var x = '__storage_test__';
		storage.setItem(x, x);
		storage.removeItem(x);
		return true;
	}catch(e) {
		return e instanceof DOMException && (
			// Firefox를 제외한 모든 브라우저
			e.code === 22 ||
			// Firefox
			e.code === 1014 ||
			// 코드가 존재하지 않을 수도 있기 떄문에 이름 필드도 확인합니다.
			// Firefox를 제외한 모든 브라우저
			e.name === 'QuotaExceededError' ||
			// Firefox
			e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
			// 이미 저장된 것이있는 경우에만 QuotaExceededError를 확인하십시오.
			(storage && storage.length !== 0);
	}
}

function GET_VALUE(k){
	try{ if(CHECK_LOCAL_STORAGE()===true) return localStorage.getItem(k);
	}catch(e){ console.log(e); }
	return COOKIE[k];
}

function SET_VALUE(k, v){
	try{
		if(CHECK_LOCAL_STORAGE()===true){
			var n = arguments.length;
			if(n===0){
				var arr = Object.keys(localStorage);
				for(var i=0; i<arr.length; i++)
					localStorage.removeItem(arr[i]);
			}else if(n===1 && typeof k==="string"){
				localStorage.removeItem(k);
			}else{
				localStorage.setItem(k, v);
			}
			return;
		}
	}catch(e){ console.log(e); }
	SET_COOKIE(k,v);
}


//템플릿 함수. 사용법.
// var s = TEMPLATE({ key : "value", key2 : 123}, function(){
// 	/*[CDATA[
//		<div>
//			${key}
//			<div>${key2}</div
//		</div>
// 	]]*/
// });
function TEMPLATE(arrData, fnTemplate){
	if(arguments.length===1){
		fnTemplate = arguments[0];
		arrData = {};
	}
	if(typeof fnTemplate !== "function") return "no template";
	sTemplate = fnTemplate.toString();
	var sKey=0;
	for (i = 0; i < sTemplate.length; i++) {
		sKey = ((sKey<<5)-sKey)+sTemplate.charCodeAt(i);
		sKey &= sKey;
	}
	if(typeof TEMPLATE["t"+sKey] == "undefined"){
		var sTemplate = fnTemplate.toString().replace(/[\r\n\t]/g," ").split(/\[CDATA\[|\]\]/g);
		if(sTemplate.length !== 3) return "no [CDATA[ ... ]]";
		sTemplate = "\""+sTemplate[1].replace(/"/g,"\\\"")+"\"";
		sTemplate = sTemplate.replace(/\$\{/g,"\"+o.").replace(/\}/g,"+\"");
		eval( "fnTemplate = function(o){ return "+sTemplate+"; };" );
		TEMPLATE["t"+sKey] = fnTemplate;
	}
	return TEMPLATE["t"+sKey](arrData);
}


function CHANGE_OBJECT(arrData, value){
	for(var k in arrData){
		if(k==="__proto__") continue;
		if(typeof arrData[k] === "undefined"
			|| arrData[k] === null)
			arrData[k]=value;
		else if(typeof arrData[k] === "object")
			CHANGE_OBJECT(arrData[k], value);
	}
}

function CHANGE_DATA(arrData){
	CHANGE_OBJECT(arrData, "");
	return arrData;
}

//for IE
if( typeof Object.assign==="undefined" ){
	Object.copy = function(obj){
		var s = "";
		function check_copy(v){
			var t = typeof v;
			if(t==="object" && v===null) return "null";
			else if(t==="object") return Object.copy(v, false);
			else if(t==="function") return v.toString();
			else if(t==="undefined") return t;
			else if(t==="string") return "'"+v+"'";
			else return v;
		}
		if(Array.isArray(obj)){
			s = "[";
			for(var i=0; i<obj.length; i++){
				if(i!==0) s += ",";
				s += check_copy(obj[i]);
			}
			s += "]";
		}else{
			s = "{";
			for(var k in obj){
				if(s==="{") s += k+":";
				else s += ","+k+":";
				s += check_copy(obj[k]);
			}
			s += "}";
		}
		if(arguments.length==1){
			console.log(s);
			return eval("("+s+")");
		}else return s;
	};
	Object.assign = function(){
		var l = arguments.length;
		if(l===0) return null;
		else if(l===1) return arguments[0];
		var oCopy;
		if(l>2){
			var arr = [];
			for(var i=1; i<l; i++)
				arr.push(arguments[i]);
			oCopy = Object.assign.apply(null, arr);
		}else oCopy = arguments[1];
		var o0 = arguments[0];
		var o1 = oCopy; // 이건 카피
		for(var k in o1)
			o0[k] = o1[k];
		return o0;
	};
}


//dateformat
function GET_DATE(a0,a1){
	var dow = "일월화수목금토";
	var date, str;
	if(arguments.length>1){
		if(typeof a0==="string"){
			str = a0;
			date = a1;
		}else{
			str = a1;
			date = a0;
		}
	}else if(arguments.length===1){
		if(typeof a0==="string"){
			str = a0;
			date = new Date();
		}else{
			str = "YYYY-MM-DD hh:mm:ss";
			date = a0;
		}
	}else{
		str = "YYYY-MM-DD hh:mm:ss";
		date = new Date();
	}
	var DOW = dow[date.getDay()]+"";

	var YYYY = date.getFullYear()+"";
	var YY = YYYY.substr(2,2);
	var M = (date.getMonth()+1)+"";
	var MM = ("0"+M).substr(-2,2);
	var D = date.getDate()+"";
	var DD = ("0"+D).substr(-2,2);

	var h = date.getHours()+"";
	var hh = ("0"+h).substr(-2,2);
	h = h*1 > 12 ? (h-12)+"" : h ;
	var m = date.getMinutes()+"";
	var mm = ("0"+m).substr(-2,2);
	var s = date.getSeconds()+"";
	var ss = ("0"+s).substr(-2,2);

	return str.replace(/DOW/g, DOW)
		.replace(/YYYY/g, YYYY).replace(/YY/g, YY)
		.replace(/MM/g, MM).replace(/M/g, M)
		.replace(/DD/g, DD).replace(/D/g, D)
		.replace(/hh/g, hh).replace(/h/g, h)
		.replace(/mm/g, mm).replace(/m/g, m)
		.replace(/ss/g, ss).replace(/s/g, s);

}


//HASH
function HASH(s){
	if(typeof s!=="string") return 0;
	if(typeof HASH[s]==="undefined"){
		var n = 0;
		for(var i=0; i<s.length; i++){
			n = ((n<<5)-n)+s.charCodeAt(i);
			n = n & n;
		}

		HASH[s] = n<0?(Number.MAX_SAFE_INTEGER-n)%n:n;
	}
	return HASH[s];
}


function stuff(s, arr, v){
	try{ if(s.length<1) return "";
	}catch(e){ return "";}
	try{ if(typeof arr==="number") arr = [arr];
	}catch(e){ arr = [0]; }
	var result = "";
	for(var i=0; i<s.length; i++){
		try{ if(arr.indexOf(i)>-1) result+=v;
		}catch(e){}
		result+=s[i];
	}
	return result;
}


