package com.pageturner.util;

import org.springframework.stereotype.Component;
import java.text.DecimalFormat;

@Component("currencyFormatter")
public class CurrencyFormatter {

    private static final DecimalFormat formatter =
        new DecimalFormat("#,##0.00");

    public static String format(double amount) {
        return "Rs. " + formatter.format(amount);
    }

    public static String format(java.math.BigDecimal amount) {
        if (amount == null) return "Rs. 0.00";
        return "Rs. " + formatter.format(amount);
    }
}
