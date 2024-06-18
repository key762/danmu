function showPassword() {
    var passwordInput = document.querySelector('input[name="password"]');
    var showButton = document.querySelector('button[class="show-password"]');
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        showButton.textContent = '隐藏密码';
    } else {
        passwordInput.type = 'password';
        showButton.textContent = '显示密码';
    }
}

infos = ['腾讯弹幕来袭', '终于有弹幕了', '爱奇艺弹幕来啦', '前排前排', '第一']
setInterval(function () {
    var infoNum = Math.floor(Math.random() * infos.length);
    var randomNum = Math.floor(Math.random() * 4);
    var styleNum = Math.floor(Math.random() * 4);
    var info = $('<span></span>');
    info.text(infos[infoNum]);
    info.addClass('danmu' + styleNum);
    info.css({
        'margin-right': '0px',
        'margin-top': (randomNum * 40) + 'px'
    });
    setTimeout(function (info) {
        $('#main').append(info);
    }, 0, info);
}, 3000);