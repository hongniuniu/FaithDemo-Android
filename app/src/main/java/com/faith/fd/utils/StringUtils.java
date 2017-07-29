package com.faith.fd.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * string处理
 * Created by guchenkai on 2015/11/24.
 */
public final class StringUtils {
	static String regEx = "[\\u4e00-\\u9fa5]"; // unicode编码，判断是否为汉字

	private final static Pattern emailer = Pattern
			.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	/**
	 * 将字符串转位日期类型
	 *
	 * @param sdate
	 * @return
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 以友好的方式显示时间
	 *
	 * @param sdate
	 * @return
	 */
	public static String friendly_time(String sdate) {

		Date time = toDate(sdate);

		if (time == null) {
			return "Unknown";
		}
		String ftime = "";
		Calendar cal = Calendar.getInstance();

		// 判断是否是同一天
		String curDate = dateFormater2.get().format(cal.getTime());
		String paramDate = dateFormater2.get().format(time);
		if (curDate.equals(paramDate)) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max(
						(cal.getTimeInMillis() - time.getTime()) / 60000, 1)
						+ "分钟前";
			else
				ftime = hour + "小时前";
			return ftime;
		}

		long lt = time.getTime() / 86400000;
		long ct = cal.getTimeInMillis() / 86400000;
		int days = (int) (ct - lt);
		if (days == 0) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max(
						(cal.getTimeInMillis() - time.getTime()) / 60000, 1)
						+ "分钟前";
			else
				ftime = hour + "小时前";
		} else if (days == 1) {
			ftime = "昨天";
		} else if (days == 2) {
			ftime = "前天";
		} else if (days > 2 && days < 31) {
			ftime = days + "天前";
		} else if (days >= 31 && days <= 2 * 31) {
			ftime = "一个月前";
		} else if (days > 2 * 31 && days <= 3 * 31) {
			ftime = "2个月前";
		} else if (days > 3 * 31 && days <= 4 * 31) {
			ftime = "3个月前";
		} else {
			ftime = dateFormater2.get().format(time);
		}
		return ftime;
	}

	/**
	 * 以友好的方式显示时间
	 *
	 * @param date
	 * @return
	 */
	public static String friendly_time(Date date) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return friendly_time(f.format(date));
	}

	/**
	 * 判断给定字符串时间是否为今日
	 *
	 * @param sdate
	 * @return boolean
	 */
	public static boolean isToday(String sdate) {
		boolean b = false;
		Date time = toDate(sdate);
		Date today = new Date();
		if (time != null) {
			String nowDate = dateFormater2.get().format(today);
			String timeDate = dateFormater2.get().format(time);
			if (nowDate.equals(timeDate)) {
				b = true;
			}
		}
		return b;
	}

	/**
	 * 返回long类型的今天的日期
	 *
	 * @return
	 */
	public static long getToday() {
		Calendar cal = Calendar.getInstance();
		String curDate = dateFormater2.get().format(cal.getTime());
		curDate = curDate.replace("-", "");
		return Long.parseLong(curDate);
	}

	/**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 *
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input) {
		if (input == null || "".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是不是一个合法的电子邮件地址
	 *
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (email == null || email.trim().length() == 0)
			return false;
		return emailer.matcher(email).matches();
	}

	/**
	 * 字符串转整数
	 *
	 * @param str
	 * @param defValue
	 * @return
	 */
	public static int toInt(String str, int defValue) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
		}
		return defValue;
	}

	/**
	 * 对象转整数
	 *
	 * @param obj
	 * @return 转换异常返回 0
	 */
	public static int toInt(Object obj) {
		if (obj == null)
			return 0;
		return toInt(obj.toString(), 0);
	}

	/**
	 * 对象转整数
	 *
	 * @param obj
	 * @return 转换异常返回 0
	 */
	public static long toLong(String obj) {
		try {
			return Long.parseLong(obj);
		} catch (Exception e) {
		}
		return 0;
	}

	/**
	 * 字符串转布尔值
	 *
	 * @param b
	 * @return 转换异常返回 false
	 */
	public static boolean toBool(String b) {
		try {
			return Boolean.parseBoolean(b);
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 将一个InputStream流转换成字符串
	 *
	 * @param is
	 * @return
	 */
	public static String toConvertString(InputStream is) {
		StringBuffer res = new StringBuffer();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader read = new BufferedReader(isr);
		try {
			String line;
			line = read.readLine();
			while (line != null) {
				res.append(line);
				line = read.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != isr) {
					isr.close();
					isr.close();
				}
				if (null != read) {
					read.close();
					read = null;
				}
				if (null != is) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
			}
		}
		return res.toString();
	}

	/**
	 * 获取字符串的md5值
	 *
	 * @param info
	 * @return
	 */
	public static String getMD5(String info) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(info.getBytes("UTF-8"));
			byte[] encryption = md5.digest();

			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i < encryption.length; i++) {
				if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
					strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
				} else {
					strBuf.append(Integer.toHexString(0xff & encryption[i]));
				}
			}
			return strBuf.toString();
		} catch (NoSuchAlgorithmException e) {
			return "";
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 判断两个字符串是否相同
	 *
	 * @param target1 目标1
	 * @param target2 目标2
	 * @return 是否相同
	 */
	public static boolean equals(String target1, String target2) {
		return TextUtils.equals(target1, target2);
	}

	/**
	 * 获得UUID
	 *
	 * @return UUID
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 验证邮箱
	 *
	 * @param email 电子邮箱地址
	 * @return 是否匹配
	 */
	public static boolean checkEmailFormat(String email) {
		return checkRegex(email,
				"^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
	}

	/**
	 * 匹配正则表达式
	 *
	 * @param text  被匹配文本
	 * @param regex 正则表达式
	 * @return 是否匹配
	 */
	public static boolean checkRegex(final String text, final String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		return matcher.matches();
	}

	/**
	 * 验证手机号码
	 *
	 * @param mobile 手机号码
	 * @return 是否匹配
	 */
	public static boolean checkMobileFormat(String mobile) {
		return checkRegex(mobile, "[1][34578]\\d{9}");
	}

	/**
	 * 验证密码(6到16位)
	 *
	 * @param password 密码文本
	 * @return 是否匹配
	 */
	public static boolean checkPasswordFormat(String password) {
		return checkRegex(password, "[A-Z0-9a-z]{6,16}");
	}

	/**
	 * 查字符串是否为数字型字符串
	 *
	 * @param target 目标
	 * @return 是否匹配
	 */
	public static boolean isNumeric(String target) {
		if (StringUtils.isEmpty(target)) return false;
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern != null ? pattern.matcher(target) : null;
		return !(isNum != null && !isNum.matches());
	}

	public static int getChineseCount(String str) {
		int count = 0;
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				count = count + 1;
			}
		}
		return count;
	}

	public static String getCutStringByByteCount(String str, int count, String endString) {
		String cutStringByByteCount = getCutStringByByteCount(str, count);
		if (cutStringByByteCount.equals(str))
			return cutStringByByteCount;
		else return cutStringByByteCount + endString;
	}

	public static String getCutStringByByteCount(String str, int count) {
		String newStr = "";
		int addCount = 0;
		for (int i = 0; i < str.length(); i++) {
			addCount++;
			String substring = str.substring(i, i + 1);
			if (isContainChinese(substring))
				addCount++;
			if (addCount > count)
				break;
			newStr = newStr + substring;
		}
		if (newStr.length() <= str.length())
			return newStr;
		else
			return str;
	}

	public static boolean isContainChinese(String str) {
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.find();
	}

	/**
	 * length表示生成字符串的长度
	 *
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * 复制文字
	 *
	 * @param context
	 * @param string  被复制的文字内容
	 */
//	public static void copyText(Context context, String string) {
//		try {
//			ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//			ClipData mClipData = ClipData.newPlainText("Label", string);
//			cm.setPrimaryClip(mClipData);
//			ToastUtils.showToastShort(context, "复制成功");
//		} catch (Exception e) {
//			ToastUtils.showToastShort(context, "Sorry, 复制失败了...");
//		}
//	}
}
