var main = { // 스코프 선언
    init : function() {
        var _this = this;
        $('#btn-signup').on('click', function() {
            _this.signup();
        });
        $('#btn-login').on('click', function() {
            _this.login();
        });
        $('#btn-rest').on('click', function() {
            _this.rest();
        });
        $('#btn-tokenget').on('click', function() {
            _this.tokenget();
        });
        $('#btn-order').on('click', function() {
            _this.order();
        });
    },
    signup : function() {
        var data = {
            email: $('#signup-email').val(),
            name: $('#signup-name').val(),
            pwd: $('#signup-password').val()
        };

        $.ajax({
            type: 'POST',
            url: '/user-service/users',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(data) {
            alert("회원가입 요청 성공" + JSON.stringify(data));
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },
    login : function() {
        var data = {
            email: $('#login-email').val(),
            password: $('#login-password').val()
        };

        $.ajax({
            type: 'POST',
            url: '/user-service/login',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function (data) {
            alert("로그인 요청 성공" + JSON.stringify(data));
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },
    rest : function() {
        var method = $('#http-method').val();
        var uri = $('#normal-uri').val();

        $.ajax({
            type: method,
            url: uri,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8'
        }).done(function (data){
            alert(method + ' 요청 성공' + JSON.stringify(data));
        }).fail(function (error){
            alert(JSON.stringify(error));
        });
    },
    tokenget : function() {
        var token = $('#token').val();
        var uri = $('#token-uri').val();

        $.ajax({
            type: 'GET',
            url: uri,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        }).done(function (data){
            alert('GET 요청 성공' + JSON.stringify(data));
        }).fail(function (error){
            alert(JSON.stringify(error));
        });
    },
    order : function() {
        var userId = $('#order-userid').val();
        var data = {
            productId: $('#order-productid').val(),
            qty: $('#order-qty').val(),
            unitPrice: $('#order-unitprice').val()
        };

        $.ajax({
            type: 'POST',
            url: '/order-service/' + userId + '/orders',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(data) {
            alert("주문 요청 성공" + JSON.stringify(data));
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    }
}

main.init();