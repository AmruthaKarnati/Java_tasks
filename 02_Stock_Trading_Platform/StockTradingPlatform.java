import java.io.*;
import java.util.*;

// ==========================================
// 1. STOCK CLASS
// ==========================================
class Stock {
    private final String symbol;
    private final String name;
    private double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol.toUpperCase();
        this.name = name;
        this.price = price;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    // Simulate minor market price fluctuations
    public void updatePrice() {
        double changePercent = (Math.random() * 6) - 3; // Random change between -3% and +3%
        this.price = Math.max(1.0, this.price * (1 + changePercent / 100));
    }
}

// ==========================================
// 2. TRANSACTION CLASS
// ==========================================
class Transaction {
    private final String type; // BUY or SELL
    private final String stockSymbol;
    private final int quantity;
    private final double pricePerShare;
    private final Date date;

    public Transaction(String type, String stockSymbol, int quantity, double pricePerShare) {
        this.type = type;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.date = new Date();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %d shares of %s @ $%.2f", 
                date.toString(), type, quantity, stockSymbol, pricePerShare);
    }
}

// ==========================================
// 3. USER CLASS
// ==========================================
class User {
    private final String username;
    private double balance;
    private final Map<String, Integer> portfolio; // Maps StockSymbol -> Quantity Owned
    private final List<Transaction> transactionHistory;

    public User(String username, double initialBalance) {
        this.username = username;
        this.balance = initialBalance;
        this.portfolio = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public double getBalance() { return balance; }
    public Map<String, Integer> getPortfolio() { return portfolio; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }

    public void setBalance(double balance) { this.balance = balance; }

    public boolean buyStock(Stock stock, int quantity) {
        double cost = stock.getPrice() * quantity;
        if (balance >= cost) {
            balance -= cost;
            portfolio.put(stock.getSymbol(), portfolio.getOrDefault(stock.getSymbol(), 0) + quantity);
            transactionHistory.add(new Transaction("BUY", stock.getSymbol(), quantity, stock.getPrice()));
            System.out.printf("Successfully bought %d shares of %s.%n", quantity, stock.getSymbol());
            return true;
        } else {
            System.out.println("Error: Insufficient funds to complete transaction.");
            return false;
        }
    }

    public boolean sellStock(Stock stock, int quantity) {
        int ownedQuantity = portfolio.getOrDefault(stock.getSymbol(), 0);
        if (ownedQuantity >= quantity) {
            double revenue = stock.getPrice() * quantity;
            balance += revenue;
            
            if (ownedQuantity == quantity) {
                portfolio.remove(stock.getSymbol());
            } else {
                portfolio.put(stock.getSymbol(), ownedQuantity - quantity);
            }
            
            transactionHistory.add(new Transaction("SELL", stock.getSymbol(), quantity, stock.getPrice()));
            System.out.printf("Successfully sold %d shares of %s.%n", quantity, stock.getSymbol());
            return true;
        } else {
            System.out.println("Error: You don't own enough shares of this stock.");
            return false;
        }
    }

    public void displayPortfolio(Map<String, Stock> marketStocks) {
        System.out.println("\n--- PORTFOLIO PERFORMANCE ---");
        System.out.printf("Cash Balance: $%.2f%n", balance);
        if (portfolio.isEmpty()) {
            System.out.println("You hold no assets currently.");
            return;
        }

        System.out.printf("%-10s %-10s %-15s %-15s%n", "Symbol", "Shares", "Current Price", "Total Value");
        double totalAssetValue = 0;
        
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            Stock stock = marketStocks.get(symbol);
            double currentPrice = (stock != null) ? stock.getPrice() : 0.0;
            double value = shares * currentPrice;
            totalAssetValue += value;

            System.out.printf("%-10s %-10d $%-14.2f $%-14.2f%n", symbol, shares, currentPrice, value);
        }
        System.out.println("-----------------------------");
        System.out.printf("Total Portfolio Net Worth: $%.2f%n", (balance + totalAssetValue));
    }
}

// ==========================================
// 4. DATA MANAGER (FILE I/O)
// ==========================================
class DataManager {
    private static final String FILE_NAME = "portfolio_data.txt";

    public static void saveUserData(User user) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println(user.getUsername());
            writer.println(user.getBalance());
            for (Map.Entry<String, Integer> entry : user.getPortfolio().entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
            System.out.println("[System] Portfolio data auto-saved successfully.");
        } catch (IOException e) {
            System.out.println("[Error] Could not save portfolio data: " + e.getMessage());
        }
    }

    public static User loadUserData(String defaultUsername) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("[System] No profile found. Creating a new simulation profile...");
            return new User(defaultUsername, 10000.00); // Initial seed money
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String username = reader.readLine();
            double balance = Double.parseDouble(reader.readLine());
            User user = new User(username, balance);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(",")) {
                    String[] tokens = line.split(",");
                    String symbol = tokens[0];
                    int qty = Integer.parseInt(tokens[1]);
                    user.getPortfolio().put(symbol, qty);
                }
            }
            System.out.println("[System] Existing profile loaded successfully.");
            return user;
        } catch (Exception e) {
            System.out.println("[Warning] Failed to load data. Resetting profile.");
            return new User(defaultUsername, 10000.00);
        }
    }
}

// ==========================================
// 5. MAIN ENVIRONMENT ENGINE
// ==========================================
class StockTradingPlatform {
    private final Map<String, Stock> market;
    private final User trader;
    private final Scanner scanner;

    public StockTradingPlatform() {
        market = new HashMap<>();
        scanner = new Scanner(java.util.Objects.requireNonNull(System.in));
        initializeMarket();
        trader = DataManager.loadUserData("Trader_Alpha");
    }

    private void initializeMarket() {
        market.put("AAPL", new Stock("AAPL", "Apple Inc.", 175.50));
        market.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 150.25));
        market.put("TSLA", new Stock("TSLA", "Tesla Inc.", 180.10));
        market.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 178.00));
        market.put("NVDA", new Stock("NVDA", "NVIDIA Corp.", 850.75));
    }

    private void updateMarketPrices() {
        for (Stock stock : market.values()) {
            stock.updatePrice();
        }
    }

    private void displayMarket() {
        System.out.println("\n--- LIVE MARKET DATA (Simulated) ---");
        System.out.printf("%-10s %-20s %-10s%n", "Ticker", "Company Name", "Price");
        System.out.println("------------------------------------");
        for (Stock stock : market.values()) {
            System.out.printf("%-10s %-20s $%-10.2f%n", stock.getSymbol(), stock.getName(), stock.getPrice());
        }
        System.out.println("------------------------------------");
    }

    public void startLoop() {
        boolean active = true;
        while (active) {
            // Tick prices forward slightly on every transaction menu action loop
            updateMarketPrices();

            System.out.println("\n===== STOCK TRADING SIMULATOR =====");
            System.out.println("1. View Market Data");
            System.out.println("2. View My Portfolio Performance");
            System.out.println("3. Buy Stock");
            System.out.println("4. Sell Stock");
            System.out.println("5. View Transaction History");
            System.out.println("6. Save & Exit");
            System.out.print("Select an option (1-6): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> displayMarket();
                case "2" -> trader.displayPortfolio(market);
                case "3" -> handleTransaction(true);
                case "4" -> handleTransaction(false);
                case "5" -> {
                    System.out.println("\n--- TRANSACTION HISTORY ---");
                    if (trader.getTransactionHistory().isEmpty()) {
                        System.out.println("No transactions recorded yet.");
                    } else {
                        for (Transaction t : trader.getTransactionHistory()) {
                            System.out.println(t);
                        }
                    }
                }
                case "6" -> {
                    DataManager.saveUserData(trader);
                    active = false;
                    System.out.println("Thank you for using the Simulator. Goodbye!");
                }
                default -> System.out.println("Invalid option selected. Please try again.");
            }
        }
    }

    private void handleTransaction(boolean isBuy) {
        System.out.print("Enter stock ticker symbol (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.get(symbol);

        if (stock == null) {
            System.out.println("Stock symbol not found in our market database.");
            return;
        }

        System.out.printf("Current price of %s is $%.2f.%n", stock.getSymbol(), stock.getPrice());
        System.out.print("Enter quantity: ");
        try {
            int qty = Integer.parseInt(scanner.nextLine());
            if (qty <= 0) {
                System.out.println("Quantity must be greater than zero.");
                return;
            }

            if (isBuy) {
                trader.buyStock(stock, qty);
            } else {
                trader.sellStock(stock, qty);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Transaction aborted.");
        }
    }

    public static void main(String[] args) {
        StockTradingPlatform app = new StockTradingPlatform();
        app.startLoop();
    }
}