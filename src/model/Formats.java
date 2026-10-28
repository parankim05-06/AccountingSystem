package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Small helpers for turning text into money/dates and back.
 *
 * MONEY RULE: money is stored and calculated as a whole number of CENTAVOS
 * (a Java long). PHP 1,500.50 is the number 150050. Whole numbers never have
 * the rounding errors that double has (0.1 + 0.2 is not exactly 0.3 in double).
 *
 * DATE RULE: the database stores dates as ISO text "2026-10-27" (it sorts
 * correctly). The screens show and accept "27-10-2026" because the original
 * app used that format.
 */
public class Formats {

    private static final String PESO_SIGN = "\u20B1"; // the peso symbol

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** 150050 becomes "₱1,500.50". */
    public static String money(long centavos) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        BigDecimal pesos = BigDecimal.valueOf(centavos, 2); // 150050 with 2 decimals = 1500.50
        String text = format.format(pesos.abs());
        return (centavos < 0 ? "-" : "") + PESO_SIGN + text;
    }

    /** Blank for zero, so empty Debit/Credit cells in tables stay empty. */
    public static String moneyOrBlank(long centavos) {
        return centavos == 0 ? "" : money(centavos);
    }

    /**
     * "1,500.50" becomes 150050. Throws IllegalArgumentException with a message
     * that is safe to show to the user.
     */
    public static long parseMoney(String text) {
        String cleaned = text == null ? "" : text.trim().replace(",", "").replace(PESO_SIGN, "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Please enter an amount.");
        }

        BigDecimal pesos;
        try {
            pesos = new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a number, for example 1500.50");
        }

        if (pesos.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (pesos.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount can have at most 2 decimal places (centavos).");
        }

        try {
            return pesos.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount is too large.");
        }
    }

    /** "2026-10-27" becomes "27-10-2026". */
    public static String isoToDisplayDate(String iso) {
        return LocalDate.parse(iso).format(DISPLAY_DATE);
    }

    /** "27-10-2026" becomes "2026-10-27". Throws IllegalArgumentException if invalid. */
    public static String displayToIsoDate(String text) {
        try {
            return LocalDate.parse(text == null ? "" : text.trim(), DISPLAY_DATE).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Please enter the date as DD-MM-YYYY, for example 27-10-2026.");
        }
    }

    public static String todayDisplay() {
        return LocalDate.now().format(DISPLAY_DATE);
    }
}
