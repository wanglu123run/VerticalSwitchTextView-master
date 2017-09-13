# VerticalSwitchTextView
本文使用继承TextView实现垂直切换Text的效果。效果类似于很多app首页上滚动的广告位。

实现中使用了属性动画ValueAnimator来进行Text绘制过程的控制。

可以通过自定义属性来定制切换时间，间隔时间和切换方向。

并且定义了回调接口用于调用者处理点击事件和获得切换的通知。

2016.8.16更新：

增加gravity属性，水平方向的gravity属性可以设置为left、right、center，分别表示文字左对齐、右对齐和居中对齐。

增加ellipsis属性，可以设置为start、end、middle，分别表示当文字长度超出View长度时，在头部、尾部、中间显示省略号。

博客链接：http://blog.csdn.net/goodlixueyong/article/details/50785032

具体的效果如下

带有gravity属性和ellipsis属性的效果：

![](https://github.com/viclee2014/VerticalSwitchTextView/blob/master/app/src/main/res/raw/vertical_switch_textview2.gif)

普通效果：

![](https://github.com/viclee2014/VerticalSwitchTextView/blob/master/app/src/main/res/raw/vertical_switch_textview.gif)
