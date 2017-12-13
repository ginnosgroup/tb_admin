//var url="https://47.91.42.245/admin/";
//var url2="https://47.91.42.245/statics";
//var url3="https://47.91.42.245/";
var url="/admin/";
var url2="/statics";
var url3="/";
//var url = "https://tb.rj36.com/admin/";
//var url2 = "https://tb.rj36.com/statics";
//var url3 = "https://tb.rj36.com/";
// var url = "http://10.1.2.24:8080/admin/";
// var url2 = "http://10.1.2.24:8080/statics";
// var url3 = "http://10.1.2.24:8080/tb/";
var tb = {
	out: function() {
		$.get(url + "admin_user/out", function(result) {
			console.log(result)
			if(result.code == 0 && result.data == true) {
				window.location.href = "login.htm";
			}
		});
	},
	isLogin: function(u) {
		$.get(url + "admin_user/isLogin", function(result) {
			console.log(result)
				if(result.code == 1) {
					sessionStorage.setItem("goUrl", u);
					window.location.href = "login.htm";
				}
		});
	},
	checkEmpty: function(val, message) {
		if(val) {
			return true;
		} else {
			v.eror = message;
			setTimeout(function() {
				v.eror = "";
			}, v.showTime);
			return false;
		};
	},
	checkLength: function(len, maxlen, message) {
		if(len < maxlen) {
			return true;
		} else {
			v.eror = message;
			setTimeout(function() {
				v.eror = "";
			}, v.showTime);
			return false;
		};
	}
}