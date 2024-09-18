package com.meatmeet6kae.entity.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity //JPA 엔티티
@Table(name = "user") //이 엔티티가 매핑될 데이터베이스 테이블 이름을 지정
public class User {

    @Id //기본 키(primary key)로 사용할 필드
    private String loginId;
    @Column(name = "use_yn", nullable = false, insertable = false) //INSERT할 때 포함하지 않는다. default=Y
    private String useYn;
    private String password;
    private String name;
    private String role;
    private String gender;
    private String addr;
    private String email;
    private String emailYn;
    @Column(name = "create_date", updatable = false, insertable = false) //엔티티가 생성될 때 설정되고 이후 변경 금지
    private LocalDateTime createDate;
    @Column(name = "withdraw_date", nullable = true) // 사용자가 탈퇴한 날짜, default=NULL
    private LocalDateTime withdrawDate;


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
