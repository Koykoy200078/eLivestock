package app.elivestock.models;

public class UsersData {
    private String userId;
    private String email;
    private String fullname;
    private String birthdate;
    private String address;
    private String contactNumber;
    private String gender;
    private String imageUrl;

    public UsersData() {
    }

    public UsersData(String userId, String email, String fullname, String birthdate, String address, String contactNumber, String gender, String imageUrl) {
        this.userId = userId;
        this.email = email;
        this.fullname = fullname;
        this.birthdate = birthdate;
        this.address = address;
        this.contactNumber = contactNumber;
        this.gender = gender;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}