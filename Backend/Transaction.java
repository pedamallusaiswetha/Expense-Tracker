import java.math.BigDecimal;

public class Transaction {

    private int id;
    private String type;
    private BigDecimal amount;
    private String category;
    private String description;
    private String transactionDate;

    public Transaction(String type, double amount, String category,
                       String description, String transactionDate) {
        this(type, BigDecimal.valueOf(amount), category, description, transactionDate);
    }

    public Transaction(String type, BigDecimal amount, String category,
                       String description, String transactionDate) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public Transaction(int id, String type, double amount, String category,
                       String description, String transactionDate) {
        this(id, type, BigDecimal.valueOf(amount), category, description, transactionDate);
    }

    public Transaction(int id, String type, BigDecimal amount, String category,
                       String description, String transactionDate) {
        this(type, amount, category, description, transactionDate);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getTransactionDate() {
        return transactionDate;
    }
}