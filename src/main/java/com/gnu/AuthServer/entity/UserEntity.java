package com.gnu.AuthServer.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * 
 * 회원정보 엔티티<br/>
 * 하이버네이트는 일반적으로 복합키를 허용하지 않으므로 Join은 tuple의 pk를 sequence로 하고, 외래키로 삼는다<br/>
 * (개선을 위해 @IdClass 나 @EmbeddedId 등을 참고 중)
 * 
 * @author Geunwoo Shim(gflhsin@gmail.com)
 *
 */
@Entity
public class UserEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@Column(name="user_name",unique=true)
	private String username;
	@Column(name="password")
	private String password;
	@Column(name="locale")
	private String locale;
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="userEntity") // userEntity를 참고한다는 것을 나타냄. mappedBy는 Owner를 결정
	private List<AuthoritiesEntity> authorities = new ArrayList<AuthoritiesEntity>();
	
	/**
	 * 
	 * Join에 필요한 객체는 외부의 Helper 함수를 작성하는 것이 좋음
	 * 
	 * @param roles
	 * @return
	 */
	public boolean createAuthority(String... roles){
		boolean bool = true;
		if(null == authorities){
			authorities = new ArrayList<AuthoritiesEntity>();
		}
		for(String role : roles){
			bool &= authorities.add(new AuthoritiesEntity(role, this));
		}
		return bool;
	}
	
	public UserEntity() {
	}



	public UserEntity(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<AuthoritiesEntity> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<AuthoritiesEntity> authorities) {
		this.authorities = authorities;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		return "UserEntity [id=" + id + ", username=" + username + ", password=" + password + ", locale=" + locale
				+ ", authorities=" + authorities + "]";
	}
}
