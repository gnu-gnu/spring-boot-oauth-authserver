package com.gnu.AuthServer.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class AuthoritiesEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	@Column(name="role_name", nullable=false)
	private String roleName;
	/**
	 * 회원(1) -> 권한(N) 및 권한(N) -> 회원(1)의 양방향 관계가 성립함을 보임
	 * FetchType -> Lazy로딩 여부 결정
	 * Cascade는 자세한 정보는 인터넷 참고
	 * 
	 */
	@ManyToOne(fetch=FetchType.EAGER,cascade=CascadeType.ALL)
	@JoinColumn(name="user_seq")
	private UserEntity userEntity;
	
	public AuthoritiesEntity() {
	}

	public AuthoritiesEntity(String roleName, UserEntity userEntity) {
		setRoleName(roleName);
		setUserEntity(userEntity);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public void setUserEntity(UserEntity userEntity) {
		this.userEntity = userEntity;
	}

	@Override
	public String toString() {
		return "AuthoritiesEntity [id=" + id + ", roleName=" + roleName+"]";
	}

	



}
