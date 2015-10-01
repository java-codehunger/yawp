package io.yawp.driver.postgresql;

import io.yawp.repository.IdRef;
import io.yawp.repository.annotations.Endpoint;
import io.yawp.repository.annotations.Id;
import io.yawp.repository.annotations.Index;

@Endpoint(path = "/people")
public class Person {

	@Id
	private IdRef<Person> id;

	@Index
	private String name;

	@Index
	private Integer age;

	protected IdRef<Person> getId() {
		return id;
	}

	protected void setId(IdRef<Person> id) {
		this.id = id;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected Integer getAge() {
		return age;
	}

	protected void setAge(Integer age) {
		this.age = age;
	}
}
