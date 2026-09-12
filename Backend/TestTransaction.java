public class TestTransaction {

    public static void main(String[] args) {

        Transaction transaction = new Transaction(
            "EXPENSE",
            500,
            "Food",
            "Lunch",
            "2026-09-07"
        );

        TransactionDAO dao = new TransactionDAO();

        boolean result = dao.addTransaction(transaction);

        if (result) {
            System.out.println("Transaction added successfully!");
        } else {
            System.out.println("Failed to add transaction.");
        }
    }
}