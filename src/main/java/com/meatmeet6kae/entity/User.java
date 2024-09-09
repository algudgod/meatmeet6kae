package com.meatmeet6kae.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    private String login_id;
    private String use_yn;
    private String password;
    private String name;
    private String role;
    private int gender;
    private String addr;
    private String email;
    private String email_yn;
    @Column(name = "create_date", updatable = false) //값 설정 이후 변경 금지
    private LocalDateTime create_date;
    @Column(name = "withdraw_date", nullable = true) // default=NULL
    private LocalDateTime withdraw_date;

    @PrePersist
    protected void onCreate() {
        this.create_date = LocalDateTime.now(); // 엔티티가 처음 생성될 때 현재 시간 자동 저장
    }

    // Getters and Setters
    public String getLogin_id() {
        return login_id;
    }

    public void setLogin_id(String login_id) {
        this.login_id = login_id;
    }

    public String getUse_yn() {
        return use_yn;
    }

    public void setUse_yn(String use_yn) {
        this.use_yn = use_yn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail_yn() {
        return email_yn;
    }

    public void setEmail_yn(String email_yn) {
        this.email_yn = email_yn;
    }

    public LocalDateTime getCreate_date() {
        return create_date;
    }

    public void setCreate_date(LocalDateTime create_date) {
        this.create_date = create_date;
    }

    public LocalDateTime getWithdraw_date() {
        return withdraw_date;
    }

    public void setWithdraw_date(LocalDateTime withdraw_date) {
        this.withdraw_date = withdraw_date;
    }
}
