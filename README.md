#CalendarView

  一个Android日历控件库，包含年历，月历，滚动月历，以及一个实现了年历、月历过渡的控件。它们既可以单独使用，也可以组合使用。
  主要特点是包含丰富的自定义属性；年历月历可以为某一天添加各种样式的decorator。支持android api 11以上。

  Thanks for [ScrollerCalendar](https://github.com/guanchao/ScrollerCalendar), a material style calendar widget library, contains some widget, you
  can be use them alone, also you can use them in combination. For Android api level 11+.

---
  GIF预览：

  ![gif](https://raw.githubusercontent.com/missmess/CalendarView/master/raw/cd.gif)

---

  * [主要功能简介](#主要功能简介)
  * [添加到项目中](#添加到项目中)
  * [主要API](#api)
  * [接下来的工作](#接下来的工作)
  * [关于作者](#关于作者)

---

###主要功能简介

* 包含年历，月历。并且每个控件都有全方面的自定义属性。
* 为某一天添加自定义文字样式和背景。
* 包含滚动月历。支持显示其它月份。
* 支持多种监听。
* 增加了一个帮助实现年历到月历的过渡动画的viewgroup。

---

###添加到项目中

Android Studio用户，在项目的build.gradle中添加该dependencies：

  `
    compile "com.missmess.calendarview:calendarview:1.1.2"
  `

---

<h3 id='api'>主要API</h3>

介绍一下主要的控件和api，更详细的可以下载demo来了解~

######1、YearView

  年历，显示一年的所有日期。YearView提供了16个自定义属性，用于完全定义你想要的布局：
```xml
    <declare-styleable name="YearView">
        <attr name="showYearLabel" format="boolean"/>
        <attr name="showYearLunarLabel" format="boolean"/>
        <attr name="dividerColor" format="color"/>
        <attr name="yearHeaderTextColor" format="color" />
        <attr name="yearHeaderTextHeight" format="dimension"/>
        <attr name="yearHeaderTextSize" format="dimension"/>
        <attr name="yearHeaderLunarTextColor" format="color"/>
        <attr name="yearHeaderLunarTextSize" format="dimension"/>
        <attr name="yearHeaderDashColor" format="color"/>
        <attr name="monthLabelTextColor" format="color"/>
        <attr name="monthLabelTextSize" format="dimension"/>
        <attr name="monthLabelTextHeight" format="dimension"/>
        <attr name="dayLabelTextColor" format="color"/>
        <attr name="dayLabelTextSize" format="dimension"/>
        <attr name="dayLabelRowHeight" format="dimension"/>
        <attr name="dayLabelCircleRadius" format="dimension"/>
    </declare-styleable>
```

  可以通过setDecors方法为YearView设置decorators。setDecors方法对于其它的控件都可用。详细见 [这里](#decorDetail) 。
```java
	yearView.setToday(new CalendarDay(2017, 2, 12));
	DayDecor dayDecor = new DayDecor();
	dayDecor.putOne(new CalendarDay(2017, 1, 1), Color.GREEN);
	yearView.setDecors(dayDecor);
```

  截图：

  ![image1](https://raw.githubusercontent.com/missmess/CalendarView/master/raw/yv.jpg)

######2、MonthView

  月历。提供了13个自定义属性来控制MonthView布局。
  ```xml
    <declare-styleable name="MonthView">
        <attr name="dayCircleRadius" format="dimension" />
        <attr name="selectDayCircleBgColor" format="color" />
        <attr name="dayTextColor" format="color"/>
        <attr name="dayTextSize" format="dimension"/>
        <attr name="dayRowHeight" format="dimension" />
        <attr name="monthTitleColor" format="color" />
        <attr name="monthTextSize" format="dimension" />
        <attr name="monthHeaderHeight" format="dimension" />
        <attr name="weekLabelTextColor" format="color" />
        <attr name="weekLabelTextSize" format="dimension" />
        <attr name="showWeekLabel" format="boolean"/>
        <attr name="showWeekDivider" format="boolean"/>
        <attr name="showMonthTitle" format="boolean"/>
    </declare-styleable>
  ```

 截图：

 ![image2](https://raw.githubusercontent.com/missmess/CalendarView/master/raw/mv2.jpg)

######3、MonthViewPager

  可滚动的月历，左右滑动或点击indicator切换显示的月份。

  **使用MonthViewPager需要在xml中为它设置一个子view。这个子view只可以用来添加属性，但是不可以通过findViewById使用它。这个子view仅用作定义样式。**

  截图：

  ![gif2](https://raw.githubusercontent.com/missmess/CalendarView/master/raw/mvp.gif)

######4、TransitRootView

  这个viewgroup用来控制年历和月历之间的过渡。在xml中需要使用TransitRootView作为根布局，增加两个子view或viewgroup，它们分别包含你的YearView和MonthView（或者MonthViewPager）。
  顺序不能颠倒，否则达不到指定的效果。详见demo。

  提供了多个自定义属性用来控制过渡过程：
  ```xml
  <declare-styleable name="TransitRootView">
	  <attr name="y2m_interpolator" format="reference" />
	  <attr name="m2y_interpolator" format="reference" />
	  <attr name="y_anim_duration" format="integer" />
	  <attr name="transit_base_duration" format="integer" />
	  <attr name="m_anim_duration" format="integer" />
  </declare-styleable>
  ```

<h6 id='decorDetail'>5、DayDecor和Style</h6>

  DayDecor可以为某一个显示的天，添加多种样式的Decorator或定义文字样式。示例：
  ```java
	// add decorators
	DayDecor dayDecor = new DayDecor();
	// circle bg
	dayDecor.putOne(new CalendarDay(2017, 2, 1), 0xFFFF6600);
	// rectangle bg
	int color = 0xFFAAAAAA;
	dayDecor.putOne(new CalendarDay(2017, 2, 4), color, DayDecor.Style.RECTANGLE);
	dayDecor.putOne(new CalendarDay(2017, 2, 11), color, DayDecor.Style.RECTANGLE);
	dayDecor.putOne(new CalendarDay(2017, 2, 18), color, DayDecor.Style.RECTANGLE);
	dayDecor.putOne(new CalendarDay(2017, 2, 25), color, DayDecor.Style.RECTANGLE);
	// drawable bg
	dayDecor.putOne(new CalendarDay(2017, 2, 19), getResources().getDrawable(R.drawable.a_decor));
	// styled background and text
	DayDecor.Style style = new DayDecor.Style();
	style.setTextSize(getResources().getDimensionPixelSize(R.dimen.big_text));
	style.setTextColor(0xFF72E6BC);
	style.setBold(true);
	style.setItalic(true);
	style.setUnderline(true);
	style.setStrikeThrough(true);
	style.setPureColorBgShape(DayDecor.Style.CIRCLE);
	style.setPureColorBg(0xFF66AA76);
	dayDecor.putOne(new CalendarDay(2017, 2, 24), style);
	monthView.setDecors(dayDecor);
  ```

---

###接下来的工作
######还有一些工作需要完善，目前能想到的计划内的工作有以下两条：

  ~~1、 强化DayDecor的功能，包括对样式（加粗、斜体），任意背景，任意字体颜色的自定义。~~

  2、 增加月历上滑显示到周历的功能。类似于小米日历的效果。

---

###关于作者
有任何问题和BUG，欢迎反馈给我，可以用以下联系方式跟我交流：

* QQ: 478271233
* 邮箱：<478271233@qq.com>
* GitHub: [@missmess](https://github.com/missmess)

---
######CopyRight：`missmess`
