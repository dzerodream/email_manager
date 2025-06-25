package com.emailmanager.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssUtil {

    /**
     * 定义一个安全的HTML白名单
     * 只允许常见的、安全的富文本标签和属性
     */
    private static final Safelist RICH_TEXT_SAFELIST = Safelist.relaxed()
            .addTags("font", "hr", "sub", "sup", "strike", "blockquote", "pre")
            .addAttributes(":all", "style", "class")
            .addAttributes("a", "target")
            .addProtocols("a", "href", "#", "http", "https,","mailto")
            .addProtocols("img", "src", "http", "https");

    /**
     * 清理HTML内容，移除不安全的标签和属性
     * @param unsafeHtml 可能包含XSS风险的HTML字符串
     * @return 清理后的安全HTML字符串
     */
    public static String clean(String unsafeHtml) {
        if (unsafeHtml == null || unsafeHtml.isEmpty()) {
            return unsafeHtml;
        }
        return Jsoup.clean(unsafeHtml, RICH_TEXT_SAFELIST);
    }
}