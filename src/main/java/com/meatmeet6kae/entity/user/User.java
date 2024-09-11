package com.meatmeet6kae.entity.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    private String loginId;
    @Column(name = "use_yn", nullable = false, insertable = false) //insert할 때 포함하지 않는다. default=Y
    private String useYn;
    private String password;
    private String name;
    private String role;
    private String gender;
    private String addr;
    private String email;
    private String emailYn;
    @Column(name = "create_date", updatable = false) //값 설정 이후 변경 금지
    private LocalDateTime createDate;
    @Column(name = "withdraw_date", nullable = true) // default=NULL
    private LocalDateTime withdrawDate;

    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now(); // 엔티티가 처음 생성될 때 현재 시간 자동 저장
    }

    // Getters and Setters
    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
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

    public String getEmailYn() {
        return emailYn;
    }

    public void setEmailYn(String emailYn) {
        this.emailYn = emailYn;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getWithdrawDate() {
        return withdrawDate;
    }

    public void setWithdrawDate(LocalDateTime withdrawDate) {
        this.withdrawDate = withdrawDate;
    }
}
