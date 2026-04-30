package com.functional.methodrefs;

/**
 * An immutable contact record.
 *
 * @param firstName first name
 * @param lastName  family name
 * @param email     email address
 * @param phone     phone number (may be empty)
 */
public record Contact(String firstName, String lastName, String email, String phone) {

    public String fullName() {
        return firstName + " " + lastName;
    }

    public boolean hasPhone() {
        return phone != null && !phone.isBlank();
    }

    /** Create a Contact from a comma-separated string "First,Last,Email,Phone". */
    public static Contact parse(String csv) {
        String[] parts = csv.split(",", -1);
        return new Contact(parts[0].trim(), parts[1].trim(),
                           parts[2].trim(), parts.length > 3 ? parts[3].trim() : "");
    }

    @Override
    public String toString() {
        return fullName() + " <" + email + ">";
    }
}
