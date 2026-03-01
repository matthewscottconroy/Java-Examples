import java.util.*;

// ===== Capabilities (Strategies) =====
interface PricingStrategy {
    double priceFor(MenuItem item, int qty);
}

class RegularPricing implements PricingStrategy {
    @Override public double priceFor(MenuItem item, int qty) {
        return item.getBasePrice() * qty;
    }
}

class HappyHourPricing implements PricingStrategy {
    private final double discount; // e.g., 0.20 = 20% off
    public HappyHourPricing(double discount) { this.discount = discount; }
    @Override public double priceFor(MenuItem item, int qty) {
        return item.getBasePrice() * qty * (1.0 - discount);
    }
}

interface PaymentStrategy {
    String pay(double amount);
}

class CashPayment implements PaymentStrategy {
    @Override public String pay(double amount) {
        return String.format("Paid $%.2f in cash. Change handled if needed.", amount);
    }
}

class CardPayment implements PaymentStrategy {
    private final double feeRate; // e.g., 0.02 = 2% fee
    public CardPayment(double feeRate) { this.feeRate = feeRate; }
    @Override public String pay(double amount) {
        double total = amount * (1.0 + feeRate);
        return String.format("Charged $%.2f to card (includes %.1f%% fee).", total, feeRate * 100);
    }
}

// ===== Domain Model =====
class MenuItem {
    private final String name;
    private final double basePrice;
    public MenuItem(String name, double basePrice) { this.name = name; this.basePrice = basePrice; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
}

class OrderLine {
    private final MenuItem item;
    private final int qty;
    public OrderLine(MenuItem item, int qty) { this.item = item; this.qty = qty; }
    public MenuItem getItem() { return item; }
    public int getQty() { return qty; }
}

class Order {
    private final List<OrderLine> lines = new ArrayList<>();
    // Composition + Strategy: Order uses a PricingStrategy to compute totals
    private PricingStrategy pricingStrategy;

    public Order(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public void add(MenuItem item, int qty) {
        lines.add(new OrderLine(item, qty));
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
    }

    public double total() {
        return lines.stream()
            .mapToDouble(l -> pricingStrategy.priceFor(l.getItem(), l.getQty()))
            .sum();
    }

    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
}

// Composed parts the Restaurant owns/coordinates
class Menu {
    private final Map<String, MenuItem> items = new HashMap<>();
    public void addItem(MenuItem item) { items.put(item.getName(), item); }
    public MenuItem get(String name) { return items.get(name); }
}

class PaymentProcessor {
    private PaymentStrategy paymentStrategy; // Strategy for how to pay
    public PaymentProcessor(PaymentStrategy paymentStrategy) { this.paymentStrategy = paymentStrategy; }
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) { this.paymentStrategy = paymentStrategy; }
    public String process(double amount) { return paymentStrategy.pay(amount); }
}

class Restaurant {
    // Composition: Restaurant has a Menu, takes Orders, and uses a PaymentProcessor
    private final String name;
    private final Menu menu;
    private final PaymentProcessor payments;

    public Restaurant(String name, Menu menu, PaymentProcessor payments) {
        this.name = name;
        this.menu = menu;
        this.payments = payments;
    }

    public Order startOrder(PricingStrategy pricing) {
        return new Order(pricing);
    }

    public String checkout(Order order) {
        double total = order.total();
        return String.format("[%s] %s", name, payments.process(total));
    }

    public Menu getMenu() { return menu; }
    public PaymentProcessor getPayments() { return payments; }
}

// ===== Demo =====
public class RestaurantDemo {
    public static void main(String[] args) {
        // Build the restaurant via composition
        Menu menu = new Menu();
        menu.addItem(new MenuItem("Burger", 9.50));
        menu.addItem(new MenuItem("Fries", 3.50));
        menu.addItem(new MenuItem("Soda", 2.00));

        PaymentProcessor payments = new PaymentProcessor(new CashPayment());
        Restaurant r = new Restaurant("Orbit Diner", menu, payments);

        // Customer #1 — Regular pricing, pay cash
        Order o1 = r.startOrder(new RegularPricing());
        o1.add(menu.get("Burger"), 1);
        o1.add(menu.get("Fries"), 1);
        System.out.printf("Order 1 total (regular): $%.2f%n", o1.total());
        System.out.println(r.checkout(o1));
        System.out.println();

        // Customer #2 — Happy hour pricing, then switch to card payment at checkout
        Order o2 = r.startOrder(new HappyHourPricing(0.20)); // 20% off
        o2.add(menu.get("Burger"), 2);
        o2.add(menu.get("Soda"), 2);
        System.out.printf("Order 2 total (happy hour): $%.2f%n", o2.total());

        r.getPayments().setPaymentStrategy(new CardPayment(0.02)); // swap strategy at runtime
        System.out.println(r.checkout(o2));

        // Mid-service: switch pricing strategy on an open order (e.g., happy hour just started)
        Order o3 = r.startOrder(new RegularPricing());
        o3.add(menu.get("Fries"), 2);
        System.out.printf("%nOrder 3 before switch: $%.2f%n", o3.total());
        o3.setPricingStrategy(new HappyHourPricing(0.20));
        System.out.printf("Order 3 after switch to happy hour: $%.2f%n", o3.total());
    }
}

