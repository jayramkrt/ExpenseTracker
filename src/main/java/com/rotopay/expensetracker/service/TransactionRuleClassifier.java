package com.rotopay.expensetracker.service;

import com.rotopay.expensetracker.api.common.dto.TransactionClassificationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Rule-based transaction classifier and recurring detector.
 *
 * <p>Strategy: keyword / brand matching first — covers the vast majority of
 * typical Indian bank-statement transactions without a single LLM call.
 * Returns {@code null} from both public methods when no rule matches,
 * which tells the caller to fall back to Ollama.
 *
 * <p>Category keyword lists are intentionally broad and India-context aware.
 * Add more entries here over time to improve rule coverage and reduce LLM spend.
 */
@Service
@Slf4j
public class TransactionRuleClassifier {

    // =========================================================================
    // CATEGORY KEYWORD MAPS
    // Each entry: category name → list of lowercase substrings to match against
    // (description + merchant combined, lowercased).
    // Order matters: first full-scan wins; put more specific entries first if
    // two categories might overlap (e.g. "food" could hit Dining before Groceries).
    // =========================================================================

    /** High-confidence brand / exact-match keywords — score 1.0 */
    private static final Map<String, List<String>> BRAND_KEYWORDS = Map.ofEntries(

        Map.entry("Dining & Restaurants", List.of(
            "swiggy", "zomato", "dunzo food", "eatsure", "box8",
            "mcdonalds", "kfc", "burger king", "dominos", "pizza hut",
            "subway", "starbucks", "cafe coffee day", "ccd", "barista",
            "barbeque nation", "haldiram", "biryani blues"
        )),

        Map.entry("Groceries", List.of(
            "bigbasket", "blinkit", "grofers", "zepto", "dmart",
            "reliance fresh", "more supermarket", "spencers", "spar",
            "jiomart", "dunzo grocery", "swiggy instamart", "licious",
            "country delight", "milkbasket"
        )),

        Map.entry("Transportation", List.of(
            "uber", "ola cabs", "ola auto", "rapido", "meru",
            "irctc", "redbus", "makemytrip train", "yatri", "abhibus",
            "namma metro", "delhi metro", "bmtc", "best bus"
        )),

        Map.entry("Shopping", List.of(
            "amazon", "flipkart", "myntra", "meesho", "snapdeal",
            "ajio", "nykaa", "tatacliq", "reliance digital", "croma",
            "ikea", "decathlon", "shoppers stop", "lifestyle", "westside",
            "firstcry", "lenskart", "mamaearth", "boat "
        )),

        Map.entry("Subscriptions", List.of(
            "netflix", "amazon prime", "hotstar", "disney+",
            "spotify", "youtube premium", "zee5", "sonyliv", "aha",
            "apple music", "apple tv", "apple one", "apple subscr",
            "microsoft 365", "microsoft office", "adobe", "dropbox",
            "notion", "canva", "slack", "zoom", "github"
        )),

        Map.entry("Utilities", List.of(
            "airtel", "jio recharge", "vi recharge", "vodafone recharge",
            "bsnl", "act fibernet", "hathway", "tata sky", "dish tv",
            "bescom", "tneb", "mseb", "bses", "torrent power",
            "mahanagar gas", "indraprastha gas"
        )),

        Map.entry("Healthcare", List.of(
            "apollo pharmacy", "medplus", "netmeds", "pharmeasy",
            "1mg", "practo", "tata 1mg", "healthkart",
            "apollo hospital", "fortis", "narayana health", "max hospital",
            "columbia asia", "manipal hospital"
        )),

        Map.entry("Insurance", List.of(
            "lic ", "sbi life", "hdfc life", "icici prudential",
            "bajaj allianz", "hdfc ergo", "new india assurance",
            "star health", "care health", "niva bupa", "policybazaar"
        )),

        Map.entry("Education", List.of(
            "udemy", "coursera", "byjus", "unacademy", "vedantu",
            "duolingo", "khan academy", "simplilearn", "upgrad",
            "whitehat jr", "coding ninjas", "scaler", "leetcode"
        )),

        Map.entry("Travel", List.of(
            "makemytrip", "goibibo", "cleartrip", "yatra",
            "booking.com", "airbnb", "oyo", "treebo",
            "indigo", "spicejet", "air india", "akasa air", "vistara"
        ))
    );

    /** Generic keyword matches — score 0.75 */
    private static final Map<String, List<String>> GENERIC_KEYWORDS = Map.ofEntries(

        Map.entry("Dining & Restaurants", List.of(
            "restaurant", "cafe", "dhaba", "mess", "canteen",
            "food court", "biryani", "pizza", "burger", "sushi", "bakery"
        )),

        Map.entry("Groceries", List.of(
            "grocery", "supermarket", "hypermarket", "vegetables",
            "fruits", "provison", "kirana"
        )),

        Map.entry("Transportation", List.of(
            "petrol", "fuel", "diesel", "cng", "parking", "toll",
            "highway", "cab ", "taxi", "auto fare", "bus ticket",
            "train ticket", "flight ticket"
        )),

        Map.entry("Utilities", List.of(
            "electricity", "water bill", "gas bill", "internet bill",
            "mobile bill", "postpaid", "prepaid recharge", "broadband",
            "dth recharge", "cable tv"
        )),

        Map.entry("Subscriptions", List.of(
            "subscription", "subscr", "membership", "renewal",
            "annual plan", "monthly plan", "streaming"
        )),

        Map.entry("Healthcare", List.of(
            "hospital", "clinic", "medical", "pharmacy", "medicines",
            "doctor", "consultation", "diagnostic", "pathology", "lab test",
            "gym", "fitness"
        )),

        Map.entry("Insurance", List.of(
            "insurance premium", "insurance policy", "life insurance",
            "health insurance", "motor insurance", "vehicle insurance"
        )),

        Map.entry("Education", List.of(
            "tuition", "school fees", "college fees", "exam fees",
            "course fees", "training", "certification"
        )),

        Map.entry("Salary/Income", List.of(
            "salary", "payroll", "wage", "stipend", "bonus",
            "incentive", "commission paid", "neft cr", "salary credit"
        )),

        Map.entry("Transfer", List.of(
            "upi-", "upi/", "upi ", "neft-", "neft/", "neft to",
            "neft from", "imps-", "imps/", "rtgs", "fund transfer",
            "transfer to", "transfer from", "sent to", "received from",
            "self transfer"
        )),

        Map.entry("Travel", List.of(
            "hotel", "resort", "lodge", "hostel", "travel",
            "tour package", "vacation"
        )),

        Map.entry("Shopping", List.of(
            "mall", "store", "retail", "fashion", "clothing",
            "electronics", "gadget", "furniture", "home decor"
        ))
    );

    // =========================================================================
    // RECURRING DETECTION RULES
    // =========================================================================

    /** Known subscription/recurring brands — deterministically recurring */
    private static final List<String> RECURRING_BRANDS = List.of(
        "netflix", "spotify", "amazon prime", "hotstar", "disney+",
        "youtube premium", "apple music", "apple one", "apple tv",
        "zee5", "sonyliv", "aha", "jiocinema",
        "microsoft 365", "microsoft office", "adobe", "dropbox",
        "notion", "canva", "github", "slack", "zoom",
        "airtel postpaid", "jio postpaid", "vi postpaid",
        "bsnl broadband", "act fibernet", "hathway broadband",
        "lic ", "sbi life", "hdfc life", "icici prudential"
    );

    /** Generic recurring signal keywords */
    private static final List<String> RECURRING_KEYWORDS = List.of(
        "subscription", "subscr", "monthly", "quarterly", "annually",
        "annual", "renewal", "auto-debit", "auto debit", "standing instruction",
        "si debit", "mandate", "emi", "installment", "recurring",
        "membership fee", "plan renewal"
    );

    /** Keywords that strongly indicate a one-time (non-recurring) transaction */
    private static final List<String> NON_RECURRING_SIGNALS = List.of(
        "one time", "one-time", "single payment", "refund",
        "cash withdrawal", "atm ", "pos "
    );

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Attempt to classify a transaction by rules alone.
     *
     * @param description raw description text
     * @param merchant    merchant name (may be blank)
     * @param amount      raw amount string (used for salary/income direction hint)
     * @return Classification result with category + confidence,
     *         or {@code null} if no rule matched (caller should use LLM fallback)
     */
    public TransactionClassificationResult classifyByRules(
            String description, String merchant, String amount) {

        String haystack = buildHaystack(description, merchant);

        // 1. Check high-confidence brand keywords (score = 1.0)
        for (Map.Entry<String, List<String>> entry : BRAND_KEYWORDS.entrySet()) {
            String category = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (haystack.contains(keyword)) {
                    // Salary/Income only applies to credits
                    if ("Salary/Income".equals(category) && !isCredit(amount)) {
                        continue;
                    }
                    log.debug("Rule match [brand] '{}' → {} (keyword: '{}')",
                            haystack.substring(0, Math.min(40, haystack.length())), category, keyword);
                    return TransactionClassificationResult.builder()
                            .category(category)
                            .confidenceScore(1.0f)
                            .reasoning("Rule-based: matched brand keyword '" + keyword + "'")
                            .build();
                }
            }
        }

        // 2. Check generic keywords (score = 0.75)
        for (Map.Entry<String, List<String>> entry : GENERIC_KEYWORDS.entrySet()) {
            String category = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (haystack.contains(keyword)) {
                    if ("Salary/Income".equals(category) && !isCredit(amount)) {
                        continue;
                    }
                    log.debug("Rule match [generic] '{}' → {} (keyword: '{}')",
                            haystack.substring(0, Math.min(40, haystack.length())), category, keyword);
                    return TransactionClassificationResult.builder()
                            .category(category)
                            .confidenceScore(0.75f)
                            .reasoning("Rule-based: matched generic keyword '" + keyword + "'")
                            .build();
                }
            }
        }

        // No rule matched
        return null;
    }

    /**
     * Attempt to determine if a transaction is recurring by rules alone.
     *
     * @param description raw description text
     * @param merchant    merchant name (may be blank)
     * @return {@code Boolean.TRUE} / {@code Boolean.FALSE} if conclusive,
     *         {@code null} if inconclusive (caller should use LLM fallback)
     */
    public Boolean detectRecurringByRules(String description, String merchant) {
        String haystack = buildHaystack(description, merchant);

        // Strong non-recurring signals first (avoid false positives)
        for (String signal : NON_RECURRING_SIGNALS) {
            if (haystack.contains(signal)) {
                log.debug("Recurring=false (non-recurring signal '{}') for: {}", signal,
                        haystack.substring(0, Math.min(40, haystack.length())));
                return Boolean.FALSE;
            }
        }

        // Known recurring brands
        for (String brand : RECURRING_BRANDS) {
            if (haystack.contains(brand)) {
                log.debug("Recurring=true (brand '{}') for: {}", brand,
                        haystack.substring(0, Math.min(40, haystack.length())));
                return Boolean.TRUE;
            }
        }

        // Generic recurring keywords
        for (String keyword : RECURRING_KEYWORDS) {
            if (haystack.contains(keyword)) {
                log.debug("Recurring=true (keyword '{}') for: {}", keyword,
                        haystack.substring(0, Math.min(40, haystack.length())));
                return Boolean.TRUE;
            }
        }

        // No conclusive signal
        return null;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /** Combine description + merchant into a single lowercase search string. */
    private String buildHaystack(String description, String merchant) {
        String desc = description != null ? description.toLowerCase() : "";
        String merc = merchant != null ? merchant.toLowerCase() : "";
        return (desc + " " + merc).trim();
    }

    /** A positive or credit-signalled amount string indicates income. */
    private boolean isCredit(String amount) {
        if (amount == null || amount.isBlank()) return false;
        String upper = amount.toUpperCase();
        return upper.contains("CR") || (!upper.contains("DR") && !amount.startsWith("-"));
    }
}
