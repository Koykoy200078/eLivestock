package app.elivestock.models;

public class Product {

    private final long product_id;
    private final String product_name;
    private final String category_id;
    private final String category_name;
    private final double product_price;
    private final String product_status;
    private final String product_image;
    private final String product_description;
    private final String currency_code;
    private String product_owner;
    private String product_owner_contact;
    private String product_owner_address;
    private final double tax;
    private final int product_quantity;

    public Product(long product_id, String product_name, String category_id, String category_name, double product_price, String product_status, String product_image, String product_description, String currency_code, String product_owner, String product_owner_contact, String product_owner_address, double tax, int product_quantity) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.category_id = category_id;
        this.category_name = category_name;
        this.product_price = product_price;
        this.product_status = product_status;
        this.product_image = product_image;
        this.product_description = product_description;
        this.currency_code = currency_code;
        this.product_owner = product_owner;
        this.product_owner_contact = product_owner_contact;
        this.product_owner_address = product_owner_address;
        this.tax = tax;
        this.product_quantity = product_quantity;
    }


    public long getProduct_id() {
        return product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public double getProduct_price() {
        return product_price;
    }

    public String getProduct_status() {
        return product_status;
    }

    public String getProduct_image() {
        return product_image;
    }

    public String getProduct_description() {
        return product_description;
    }

    public String getCurrency_code() {
        return currency_code;
    }

    public String getProduct_owner() {
        return product_owner;
    }

    public String getProduct_owner_contact() {
        return product_owner_contact;
    }

    public void setProduct_owner_contact(String product_owner_contact) {
        this.product_owner_contact = product_owner_contact;
    }

    public String getProduct_owner_address() {
        return product_owner_address;
    }

    public void setProduct_owner_address(String product_owner_address) {
        this.product_owner_address = product_owner_address;
    }

    public double getTax() {
        return tax;
    }

    public int getProduct_quantity() {
        return product_quantity;
    }
}