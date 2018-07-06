package com.yaoyao.online.entity;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;


@Entity
@Table(name = "user")
@Data
public class User implements UserDetails {
	
	/** serialVersionUID*/  
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private String password;
	
	private String email;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	private Integer status;
	
	@Column(name = "create_time")
	private Date createTime;
	
	@Column(name = "last_login_time")
	private Date lastLoginTime;
	
	@Column(name = "last_update_time")
	private Date lastUpdateTime;
	
	private String avatar;
	
	@Transient
	private List<GrantedAuthority> authortyList;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authortyList;
	}

	@Override
	public String getUsername() {
		return name;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
