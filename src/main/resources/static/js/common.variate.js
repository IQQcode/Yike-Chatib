//定义一些变量

var websocket = null;
var shakeList = ["","shake-hard","shake-slow","shake-little","shake-horizontal","shake-vertical","shake-rotate","shake-opacity","shake-crazy"];
var shakeChinese = ["","可劲儿摇","雪花飘","瑟瑟发抖","左右摇摆","上下跳动","跷跷板","飘忽不定","放弃治疗"];
var aa = '<div class="botui-message-left"><div class="botui-message-content-img" onclick="originalImage(this)">';
var b = '</div></div>';
var cc = '<div class="botui-message-right"><div class="botui-message-content2-img" onclick="originalImage(this)">';
var host = location.host;
var wsHost = "ws://"+host+"/websocket";
var focus = false;
var mute = 2;
var shieldMap = new Map();
var timer;
var shakeNum = 0;
var msgSwitchTips = '点击可开启/关闭消息通知';
var emojiTips = '万(wu)众(ren)期(wen)待(jin)的表情包功能终于来了';
var pictureTips = '点击发送图片(最大支持1M的图片)';
var shakeTips = '试着发一条抖动的消息引起别人的注意吧，一共有7种抖动效果呦(“Esc”键快速关闭该功能，双击抖动的消息可以让他停下来)';
var clearTips = '清屏，聊天记录不会保存呦！！！';
var sendTips = '点击发送消息(回车也可发送消息)';
var onerrorMsg = "与服务器连接发生错误，请刷新页面重新进入！";
var oncloseMsg = '已与服务器断开连接！';
var unSupportWsMsg = "当前浏览器不支持WebSocket";
var firstTips = "<b>感谢您尝试这个简陋的聊天室，说几个隐藏功能：</b><br>1.侧边栏会显示成员列表，点击成员左边的小圆形可以屏蔽这个人，使发出去的消息不会被他收到，但您仍然可以收到他的消息<br>2.鼠标悬停在各个按钮上都会弹出使用说明<br>3.当浏览器不在前台时，会有提示音和桌面通知，嫌烦的话可以点击左上角的小喇叭进行关闭";
var emojiPath = 'dist/img/';
var emojiHead = '<img class="emoji_icon" src="'+emojiPath;
var textHead = '⇤';
var emojiFoot = '">';
var textFoot = '⇥';

