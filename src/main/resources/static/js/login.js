$(function () {
    $('#loginBtn').click(function () {
        var userData = {
            id: $('#id').val(),
            password: $('#password').val()
        };

        $.ajax({
            url: `/login`,
            type: "post",
            contentType: "application/json",
            data: JSON.stringify(userData),
            dataType: "text",
            beforeSend: function () {
                console.dir(userData);
            },
            error: function () {
                console.log("失败");
            },
            complete: function () {
                console.log("完成");
            },
            success: function (data) {
                if (data == 'success') {
                    //设置页面头部信息
                    $('#userInfo').html(`
                        ${userData.id}&nbsp;&nbsp;<a id="loginOut">退出</a>
                    `);

                    //隐藏欢迎标语
                    $('#greetSection').css('display', 'none');
                    //显示弹幕发送框
                    $('#danmuSection').css('display', 'block');

                    //建立WebSocket链接
                    var ws = new WebSocket('ws:' + location.host + '/ws');

                    ws.onopen = function () {
                        console.log("onpen");
                    };

                    ws.onclose = function () {
                        console.log("onclose");
                    };

                    var count = 0;
                    ws.onmessage = function (msg) {
                        if (msg.data.startsWith("用户")) {
                            console.log(msg.data);
                            return;
                        }

                        //加载弹幕
                        var item = {
                            img: '/static/img/heisenberg.png', //图片
                            info: msg.data, //文字
                            close: true, //显示关闭按钮
                            speed: count++, //延迟,单位秒,默认0
                            color: '#fff', //颜色,默认白色
                            old_ie_color: '#000000', //ie低版兼容色,不能与网页背景相同,默认黑色
                        };
                        $('body').barrager(item);
                        $('#danmuInput').val('');
                        console.log(msg.data);
                    };


                    $('#sendDanmuBtn').click(function () {
                        //发送信息给服务端
                        ws.send($('#danmuInput').val());
                    });

                    //点击登出
                    $('#loginOut').click(function () {
                        ws.close();

                        //重制登录
                        $('#userInfo').html(`
                            <a data-toggle="modal" data-target="#loginModal">登录</a>
                        `);

                        //显示欢迎标语
                        $('#greetSection').css('display', 'block');
                        //隐藏弹幕发送框
                        $('#danmuSection').css('display', 'none');
                    });
                } else {
                    alert("用户名或者密码错误!");
                }
            }
        });
    });
});