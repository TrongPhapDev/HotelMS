package entity;

public class NhanVien {
    private String id;
    private String name;
    private String position;
    private double salary;
    private String email;

    // Constructor
    public NhanVien(String id, String name, String position, double salary, String email) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
        this.email = email;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}