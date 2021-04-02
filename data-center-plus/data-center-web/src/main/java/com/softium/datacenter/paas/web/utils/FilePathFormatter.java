package com.softium.datacenter.paas.web.utils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Fanfan.Gong
 **/
public class FilePathFormatter {
    /**
     * @example #{#projectName}/#{#yyyyMM}/#{#dd}
     * @param filePathRule
     * @return
     */
    public static String format(String filePathRule, String projectName, String institutionName, LocalDateTime date) {
        String result = "";
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("projectName", projectName);
        context.setVariable("institutionName", institutionName);
        context.setVariable("yyyy", date.format(DateTimeFormatter.ofPattern("yyyy")));
        context.setVariable("yyyyMM", date.format(DateTimeFormatter.ofPattern("yyyyMM")));
        context.setVariable("yyyyMMdd", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        context.setVariable("dd", date.format(DateTimeFormatter.ofPattern("dd")));
        result = parser.parseExpression(filePathRule, new TemplateParserContext()).getValue(context, String.class);
        return result;
    }
}
