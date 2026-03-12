import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Flower. — Console Marketplace
 * Java port of the Flower. web app (flower__1_.html)
 * Run: javac FlowerMarketplace.java && java FlowerMarketplace
 */
public class FlowerMarketplace {

    // ── Data Models ────────────────────────────────────────────────────────────

    static class User {
        String uid;
        String displayName;
        String email;
        String password;
        LocalDateTime createdAt;

        User(String uid, String displayName, String email, String password) {
            this.uid = uid;
            this.displayName = displayName;
            this.email = email;
            this.password = password;
            this.createdAt = LocalDateTime.now();
        }
    }

    static class Product {
        String id;
        String name;
        String price;
        String description;
        String category;
        String imageUrl;   // path/URL placeholder (console: just stored as text)
        String sellerId;
        String sellerName;
        LocalDateTime createdAt;

        Product(String id, String name, String price, String description,
                String category, String imageUrl, String sellerId, String sellerName) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.description = description;
            this.category = category;
            this.imageUrl = imageUrl;
            this.sellerId = sellerId;
            this.sellerName = sellerName;
            this.createdAt = LocalDateTime.now();
        }
    }

    // ── In-memory "database" (replaces Firebase Firestore) ────────────────────

    static Map<String, User>    users    = new LinkedHashMap<>();
    static List<Product>        products = new ArrayList<>();
    static User                 currentUser = null;
    static Product              selectedProduct = null;
    static int                  uidCounter = 1;
    static int                  pidCounter = 1;

    static Scanner sc = new Scanner(System.in);

    // ── Entry Point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printBanner();
        seedSampleData();

        while (true) {
            printMainMenu();
            String choice = prompt("> ").trim();
            switch (choice) {
                case "1": pageBrowse();   break;
                case "2": pageAbout();    break;
                case "3": pageReviews();  break;
                case "4": pageContact();  break;
                case "5":
                    if (currentUser == null) openAuth();
                    else                     pageMyListings();
                    break;
                case "6":
                    if (currentUser == null) openAuth();
                    else                     openSellModal();
                    break;
                case "7":
                    if (currentUser != null) doLogout();
                    else                     openAuth();
                    break;
                case "0": System.out.println("\n🌸 Thanks for visiting Flower. Goodbye!"); return;
                default:  toast("Invalid option — please try again.", "error");
            }
        }
    }

    // ── Banner / Menus ────────────────────────────────────────────────────────

    static void printBanner() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║   🌸  Flower. — Where Blooms Meet Emotions  🌸   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    static void printMainMenu() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.printf( "│  %s%-37s│%n",
            currentUser != null ? "👤 " + currentUser.displayName : "🔒 Guest", " ");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  1. 🛍  Browse Flowers               │");
        System.out.println("│  2. ℹ   About                        │");
        System.out.println("│  3. ⭐  Reviews                      │");
        System.out.println("│  4. ✉   Contact                      │");
        if (currentUser != null) {
        System.out.println("│  5. 📦  My Listings                  │");
        System.out.println("│  6. ➕  List a Flower                 │");
        System.out.println("│  7. 🚪  Sign Out                     │");
        } else {
        System.out.println("│  5. 🔑  Sign In / Register           │");
        System.out.println("│  6. ➕  List a Flower (login needed)  │");
        System.out.println("│  7. 🔑  Sign In / Register           │");
        }
        System.out.println("│  0. 🚪  Exit                         │");
        System.out.println("└─────────────────────────────────────┘");
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    static void openAuth() {
        System.out.println("\n── Authentication ─────────────────────");
        System.out.println("  1. Sign In");
        System.out.println("  2. Create Account");
        System.out.println("  0. Back");
        String choice = prompt("> ").trim();
        if      (choice.equals("1")) doLogin();
        else if (choice.equals("2")) doRegister();
    }

    static void doRegister() {
        System.out.println("\n🌺 Create Your Flower. Account");
        System.out.println("─────────────────────────────────");
        String name  = prompt("Your Name       : ").trim();
        String email = prompt("Email           : ").trim().toLowerCase();
        String pass  = prompt("Password (≥6)   : ").trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            toast("Please fill in all fields.", "error"); return;
        }
        if (pass.length() < 6) {
            toast("Password must be at least 6 characters.", "error"); return;
        }
        if (users.values().stream().anyMatch(u -> u.email.equals(email))) {
            toast("This email is already registered.", "error"); return;
        }

        String uid = "uid_" + (uidCounter++);
        User user = new User(uid, name, email, pass);
        users.put(uid, user);
        currentUser = user;
        toast("Welcome to Flower., " + name + "! 🌸", "success");
    }

    static void doLogin() {
        System.out.println("\n🌸 Sign In to Flower.");
        System.out.println("─────────────────────────────────");
        String email = prompt("Email    : ").trim().toLowerCase();
        String pass  = prompt("Password : ").trim();

        if (email.isEmpty() || pass.isEmpty()) {
            toast("Please fill in all fields.", "error"); return;
        }

        Optional<User> found = users.values().stream()
            .filter(u -> u.email.equals(email) && u.password.equals(pass))
            .findFirst();

        if (found.isPresent()) {
            currentUser = found.get();
            toast("Welcome back, " + currentUser.displayName + "! 🌸", "success");
        } else {
            toast("Invalid email or password. Please try again.", "error");
        }
    }

    static void doLogout() {
        String name = currentUser.displayName;
        currentUser = null;
        selectedProduct = null;
        toast("Signed out. See you soon, " + name + "! 🌷", "info");
    }

    // ── BROWSE PRODUCTS ───────────────────────────────────────────────────────

    static void pageBrowse() {
        while (true) {
            System.out.println("\n── 🛍  Browse Flowers (" + products.size() + " listings) ──");
            if (products.isEmpty()) {
                System.out.println("  🌸 No flowers listed yet.");
                if (currentUser == null) System.out.println("  Sign in and be the first to list!");
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                boolean isOwner = currentUser != null && currentUser.uid.equals(p.sellerId);
                System.out.printf("  [%d] %-28s %-12s  🏪 %-15s  📅 %s%s%n",
                    i + 1, p.name, p.price, p.sellerName,
                    p.createdAt.format(fmt),
                    isOwner ? "  ✏ YOURS" : "");
            }

            System.out.println("\n  [#] View product detail");
            System.out.println("  [0] Back");
            String choice = prompt("> ").trim();
            if (choice.equals("0")) return;

            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx >= 0 && idx < products.size()) {
                    openProductDetail(products.get(idx));
                } else {
                    toast("Invalid number.", "error");
                }
            } catch (NumberFormatException e) {
                toast("Please enter a number.", "error");
            }
        }
    }

    static void openProductDetail(Product p) {
        selectedProduct = p;
        boolean isOwner = currentUser != null && currentUser.uid.equals(p.sellerId);

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf( "║  %-38s║%n", "🌸 " + p.name);
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf( "║  Category  : %-26s║%n", p.category);
        System.out.printf( "║  Price     : %-26s║%n", p.price);
        System.out.printf( "║  Seller    : %-26s║%n", p.sellerName);
        System.out.printf( "║  Image     : %-26s║%n", truncate(p.imageUrl, 26));
        System.out.println("╠══════════════════════════════════════╣");
        // word-wrap description
        String desc = p.description;
        while (desc.length() > 38) {
            System.out.printf("║  %-38s║%n", desc.substring(0, 38));
            desc = desc.substring(38);
        }
        if (!desc.isEmpty()) System.out.printf("║  %-38s║%n", desc);
        System.out.println("╚══════════════════════════════════════╝");

        System.out.println("\n  1. 🛒 Buy Now" + (currentUser == null ? " (login required)" : ""));
        if (isOwner) {
            System.out.println("  2. ✏  Edit this listing");
            System.out.println("  3. 🗑  Delete this listing");
        }
        System.out.println("  0. Back");

        String choice = prompt("> ").trim();
        if      (choice.equals("1")) handleBuyClick(p);
        else if (choice.equals("2") && isOwner) openEditModal(p);
        else if (choice.equals("3") && isOwner) doDeleteProduct(p);
    }

    // ── BUY / PAYMENT ─────────────────────────────────────────────────────────

    static void handleBuyClick(Product p) {
        if (currentUser == null) {
            toast("Please sign in to buy 🌸", "info");
            openAuth();
            return;
        }
        selectedProduct = p;
        pagePayment();
    }

    static void pagePayment() {
        if (currentUser == null) { openAuth(); return; }
        System.out.println("\n🛡  Secure Checkout");
        System.out.println("─────────────────────────────────────");
        System.out.println("  Item  : " + selectedProduct.name);
        System.out.println("  Total : " + selectedProduct.price);
        System.out.println("\n  Choose Payment Method:");
        System.out.println("  1. 📱  UPI (GPay, PhonePe, Paytm)");
        System.out.println("  2. 💳  Debit / Credit Card");
        System.out.println("  3. 🚚  Cash on Delivery");
        System.out.println("  0. Cancel");

        String method = prompt("> ").trim();
        if (method.equals("0")) return;

        String[] labels = { "", "UPI", "Debit/Credit Card", "Cash on Delivery" };
        String chosen;
        try { chosen = labels[Integer.parseInt(method)]; }
        catch (Exception e) { toast("Invalid choice.", "error"); return; }

        System.out.print("\n  ⚡ Processing payment via " + chosen + " ...");
        sleep(1500);
        System.out.println(" Done!");
        toast("Order placed! Your flowers are on the way 🌸", "success");
        selectedProduct = null;
    }

    // ── SELL / EDIT / DELETE ──────────────────────────────────────────────────

    static void openSellModal() {
        if (currentUser == null) {
            toast("Please sign in to sell 🌸", "info");
            openAuth();
            return;
        }
        System.out.println("\n🌸 List a New Flower");
        System.out.println("─────────────────────────────────────");
        String name     = prompt("Flower Title *       : ").trim();
        String category = prompt("Category (Bouquet…)  : ").trim();
        String price    = prompt("Price *              : ").trim();
        String desc     = prompt("Description *        : ").trim();
        String imgUrl   = prompt("Image path/URL       : ").trim();

        if (name.isEmpty() || price.isEmpty() || desc.isEmpty()) {
            toast("Please fill in the title, price and description.", "error"); return;
        }
        if (imgUrl.isEmpty()) {
            toast("Please provide an image path or URL.", "error"); return;
        }

        String pid = "pid_" + (pidCounter++);
        if (category.isEmpty()) category = "Bouquet";
        Product p = new Product(pid, name, price, desc, category, imgUrl,
                                currentUser.uid, currentUser.displayName);
        products.add(0, p); // newest first
        toast("Your flower is now listed! 🌸", "success");
    }

    static void openEditModal(Product p) {
        System.out.println("\n✏  Edit Listing: " + p.name);
        System.out.println("  (Leave blank to keep current value)");
        System.out.println("─────────────────────────────────────");

        String name     = prompt("Title    [" + p.name     + "] : ").trim();
        String category = prompt("Category [" + p.category + "] : ").trim();
        String price    = prompt("Price    [" + p.price    + "] : ").trim();
        String desc     = prompt("Desc     [" + truncate(p.description, 20) + "] : ").trim();
        String imgUrl   = prompt("Image    [" + truncate(p.imageUrl, 20)    + "] : ").trim();

        if (!name.isEmpty())     p.name        = name;
        if (!category.isEmpty()) p.category    = category;
        if (!price.isEmpty())    p.price       = price;
        if (!desc.isEmpty())     p.description = desc;
        if (!imgUrl.isEmpty())   p.imageUrl    = imgUrl;

        toast("Listing updated! 🌸", "success");
    }

    static void doDeleteProduct(Product p) {
        System.out.print("  ⚠  Remove \"" + p.name + "\"? This cannot be undone. [y/N] : ");
        String confirm = sc.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) { System.out.println("  Cancelled."); return; }
        products.remove(p);
        selectedProduct = null;
        toast("Listing removed.", "info");
    }

    // ── MY LISTINGS ───────────────────────────────────────────────────────────

    static void pageMyListings() {
        List<Product> mine = new ArrayList<>();
        for (Product p : products)
            if (p.sellerId.equals(currentUser.uid)) mine.add(p);

        System.out.println("\n── 📦 My Listings ──────────────────────────────────");
        if (mine.isEmpty()) {
            System.out.println("  You have no listings yet. Use option 6 to add one!");
            return;
        }
        for (int i = 0; i < mine.size(); i++) {
            Product p = mine.get(i);
            System.out.printf("  [%d] %-28s %-12s  %s%n", i+1, p.name, p.price, p.category);
        }
        System.out.println("\n  [#] Edit / Delete  |  [0] Back");
        String choice = prompt("> ").trim();
        if (choice.equals("0")) return;
        try {
            int idx = Integer.parseInt(choice) - 1;
            if (idx >= 0 && idx < mine.size()) openProductDetail(mine.get(idx));
        } catch (NumberFormatException ignore) {}
    }

    // ── ABOUT ─────────────────────────────────────────────────────────────────

    static void pageAbout() {
        System.out.println("\n── ℹ  About Flower. ────────────────────────────────");
        System.out.println("  Flower. is a community marketplace where flower lovers");
        System.out.println("  buy and sell beautiful arrangements, bouquets, and more.");
        System.out.println();
        System.out.println("  ✦  Curated Blooms      — Only quality flowers");
        System.out.println("  ✦  Easy Selling         — List in under 2 minutes");
        System.out.println("  ✦  Secure Payments      — SSL-encrypted checkout");
        System.out.println("  ✦  India-wide Delivery  — We ship everywhere");
        System.out.println("\n  📍 Hyderabad, Telangana, India");
        System.out.println("  📧 hello@flower.in");
        prompt("\n  Press Enter to go back...");
    }

    // ── REVIEWS ───────────────────────────────────────────────────────────────

    static void pageReviews() {
        System.out.println("\n── ⭐ Customer Reviews ─────────────────────────────");
        String[][] reviews = {
            {"Ananya S.",  "★★★★★", "Absolutely stunning bouquet! Arrived fresh and perfectly arranged."},
            {"Rohan M.",   "★★★★★", "Ordered for my wife's birthday — she loved every petal!"},
            {"Priya K.",   "★★★★☆", "Great quality and fast delivery. Will definitely order again."},
            {"Kavya R.",   "★★★★★", "The rose basket was gorgeous. Seller was super responsive."},
            {"Arjun T.",   "★★★★★", "Best flower marketplace I've used. Highly recommend Flower.!"},
            {"Meera N.",   "★★★★☆", "Lovely arrangements and very reasonable prices."},
        };
        for (String[] r : reviews) {
            System.out.println();
            System.out.println("  " + r[1] + "  " + r[0]);
            System.out.println("  \"" + r[2] + "\"");
        }
        prompt("\n  Press Enter to go back...");
    }

    // ── CONTACT ───────────────────────────────────────────────────────────────

    static void pageContact() {
        System.out.println("\n── ✉  Contact Us ───────────────────────────────────");
        System.out.println("  📧 hello@flower.in  |  📞 +91 98765 43210");
        System.out.println("  ⏰ Mon–Sat, 9 AM – 8 PM IST");
        System.out.println();
        String name  = prompt("Your Name    * : ").trim();
        String email = prompt("Your Email   * : ").trim();
        String subj  = prompt("Subject        : ").trim();
        String msg   = prompt("Message      * : ").trim();

        if (name.isEmpty() || email.isEmpty() || msg.isEmpty()) {
            toast("Please fill in all required fields.", "error"); return;
        }
        toast("Message sent! We'll get back to you within 24 hours 🌸", "success");
    }

    // ── SEED DATA ─────────────────────────────────────────────────────────────

    static void seedSampleData() {
        // Create sample users
        User seller1 = new User("uid_s1", "Ananya Sharma", "ananya@example.com", "pass123");
        User seller2 = new User("uid_s2", "Rohan Mehta",   "rohan@example.com",  "pass123");
        users.put(seller1.uid, seller1);
        users.put(seller2.uid, seller2);
        uidCounter = 3;

        // Create sample products
        products.add(new Product("pid_1", "Pink Rose Bouquet",   "₹499",  "A gorgeous bouquet of 12 fresh pink roses, perfect for any occasion.", "Bouquet", "https://images.unsplash.com/photo-1490750967868-88df5691cc7a", seller1.uid, seller1.displayName));
        products.add(new Product("pid_2", "Sunflower Basket",    "₹649",  "Bright yellow sunflowers arranged in a rustic wicker basket.", "Basket",  "https://images.unsplash.com/photo-1597848212624-a19eb35e2651", seller2.uid, seller2.displayName));
        products.add(new Product("pid_3", "Mixed Spring Wreath", "₹849",  "Colorful spring flowers woven into a beautiful door wreath.", "Wreath",  "https://images.unsplash.com/photo-1561181286-d3fee7d55364", seller1.uid, seller1.displayName));
        products.add(new Product("pid_4", "White Lily Vase",     "₹599",  "Elegant white lilies in a clear glass vase, ideal for gifting.", "Vase",    "https://images.unsplash.com/photo-1490750967868-88df5691cc7a", seller2.uid, seller2.displayName));
        pidCounter = 5;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static void toast(String msg, String type) {
        String icon = type.equals("success") ? "✅" : type.equals("error") ? "❌" : "ℹ️";
        System.out.println("\n  " + icon + "  " + msg);
    }

    static String prompt(String label) {
        System.out.print(label);
        return sc.nextLine();
    }

    static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
