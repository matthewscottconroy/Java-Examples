class Book {
    private String title;
    private String author;
    private double price;

    // Constructor 1: No arguments
    public Book() {
        // Call the three-argument constructor with default values
        this("Unknown", "Unknown", 0.0);
    }

    // Constructor 2: Title and Author
    public Book(String title, String author) {
        // Call the three-argument constructor with a default price
        this(title, author, 10.0);
    }

    // Constructor 3: Title, Author, and Price
    public Book(String title, String author, double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public void displayInfo() {
        System.out.println("Title: " + title + ", Author: " + author + ", Price: $" + price);
    }

    public static void main(String[] args) {
        Book b1 = new Book();
        Book b2 = new Book("1984", "George Orwell");
        Book b3 = new Book("Brave New World", "Aldous Huxley", 14.99);

        b1.displayInfo();
        b2.displayInfo();
        b3.displayInfo();
    }
}

